package com.sm.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sm.auth.entity.UserCredentials;
import com.sm.auth.entity.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {

	private AuthService authService;

	@PostMapping("/register")
	public String registerUser(@RequestBody UserCredentials userCredentials) {
		return authService.saveUser(userCredentials);
	}

	@PostMapping("/token")
	public String getToken(@RequestBody UserCredentials userCredentials) {
		return authService.generateToken(userCredentials.getName());

	}

	@GetMapping("/validate")
	public String validateToken(@RequestParam("token") String token) {
		authService.validateToken(token);
		return "Token is valid";
	}

}
