package com.sm.auth.controller;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sm.auth.dto.AuthRequestDto;
import com.sm.auth.entity.UserCredentials;
import com.sm.auth.entity.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	
	private final AuthenticationManager authenticationManager;

	@PostMapping("/register")
	public String registerUser(@RequestBody UserCredentials userCredentials) {
		return authService.saveUser(userCredentials);
	}

	@PostMapping("/token")
	public String getToken(@RequestBody AuthRequestDto userCredentials) {
		
		Authentication auth= authenticationManager.authenticate
		(new UsernamePasswordAuthenticationToken(userCredentials.getUserName(), userCredentials.getPassword()));
		
		if(auth.isAuthenticated()) {
			return authService.generateToken(userCredentials.getUserName());
		}else {
			return "Invalid User credentials";
		}
		
		

	}

	@GetMapping("/validate")
	public String validateToken(@RequestParam("token") String token) {
		authService.validateToken(token);
		return "Token is valid";
	}

}
