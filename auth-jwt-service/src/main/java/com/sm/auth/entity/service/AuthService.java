package com.sm.auth.entity.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sm.auth.entity.UserCredentials;
import com.sm.auth.repositories.UserCredentialsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	
	private final UserCredentialsRepository userCredentialsRepository;
	
	private final PasswordEncoder passwordEncoder;
	
	private final JwtService jwtService;
	
	public String saveUser(UserCredentials usercredentials) {
		usercredentials.setPassword(passwordEncoder.encode(usercredentials.getPassword()));
		userCredentialsRepository.save(usercredentials);
		return "user added to system";
	}
	
	public String generateToken(String usernanme) {
		return jwtService.generateToken(usernanme);
	}
	
	public void validateToken(String token) {
		jwtService.validateToken(token);
	}

}
