package com.sm.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sm.auth.dto.AuthRequestDto;
import com.sm.auth.dto.RegisterRequestDto;
import com.sm.auth.dto.response.ApiResponse;
import com.sm.auth.dto.response.AuthResponseDto;
import com.sm.auth.entity.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	private final AuthenticationManager authenticationManager;

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<String>> registerUser(@Valid @RequestBody RegisterRequestDto request) {

		String response = authService.saveUser(request);

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("User registered successfully", response));
	}

	@PostMapping("/token")
	public ResponseEntity<ApiResponse<AuthResponseDto>> getToken(@Valid @RequestBody AuthRequestDto request) {

		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getUserName(), request.getPassword()));

		AuthResponseDto response = authService.generateToken(authentication);

		return ResponseEntity.ok(ApiResponse.success("Authentication successful", response));
	}

	@GetMapping("/validate")
	public ResponseEntity<ApiResponse<String>> validateToken(@RequestParam("token") String token) {

		authService.validateToken(token);

		return ResponseEntity.ok(ApiResponse.success("Token is valid", "VALID"));
	}

}
