package com.epam.controller;

import java.io.IOException;
import java.util.Arrays;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import com.epam.model.Employee;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class EmployeeController {

	@RequestMapping(value = "/getEmployees", method = RequestMethod.GET)
	public ModelAndView getEmployeeList() {
		return new ModelAndView("getEmployees");
	}

	@RequestMapping(value = "/showEmployees", method = RequestMethod.GET)
	public ModelAndView showEmployees(@RequestParam("code") String code) throws JsonProcessingException, IOException {

		ResponseEntity<String> response = null;
		System.out.println("Authodization Code : " + code);

		RestTemplate restTemplate = new RestTemplate();
		
		
		// first we have to encode the  credentials 
		String credentials = "EPAM_INDIA:EPAM";
		String encodedCredentionals = new String(Base64.encodeBase64(credentials.getBytes()));
		
		// we have to set the encoded credentials to header for request and authorization   
		HttpHeaders header = new HttpHeaders();
		header.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		header.add("authorization", "Basic " + encodedCredentionals);

		HttpEntity<String> request = new HttpEntity<String>(header);
		
		String access_token_url = "http://localhost:8080/oauth/token";
		
		// below we are preparing a request as per O'Auth documentation // https://tools.ietf.org/html/rfc6749#section-4.2.1
		access_token_url += "?code=" + code;
		access_token_url += "&grant_type=authorization_code";
		access_token_url += "&redirect_uri=http://localhost:8090/showEmployees";
		
		
		response = restTemplate.exchange(access_token_url, HttpMethod.POST, request, String.class);
		System.out.println("Access Token Response ----------- " + response.getBody());
		
		// Get the Access Token From the received JSON response in plain String format
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(response.getBody());
		String token = node.path("access_token").asText();
		
		
		String url = "http://localhost:8080/user/getEmployeesList";
		
		// Use the access token for authentication 
		HttpHeaders headers1 = new HttpHeaders();
		headers1.add("Authorization", "Bearer " + token);
		HttpEntity<String> entity = new HttpEntity<>(headers1);
		
		ResponseEntity<Employee[]> employees = restTemplate.exchange(url, HttpMethod.GET, entity, Employee[].class);
		System.out.println(employees);
		Employee[] employeeArray = employees.getBody();

		ModelAndView model = new ModelAndView("showEmployees");
		model.addObject("employees", Arrays.asList(employeeArray));
		return model;
	}
}
