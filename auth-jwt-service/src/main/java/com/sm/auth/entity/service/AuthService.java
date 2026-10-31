
package com.sm.auth.entity.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sm.auth.dto.RegisterRequestDto;
import com.sm.auth.dto.response.AuthResponseDto;
import com.sm.auth.entity.UserCredentials;
import com.sm.auth.entity.UserRole;
import com.sm.auth.exception.DuplicateResourceException;
import com.sm.auth.repositories.UserCredentialsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserCredentialsRepository userCredentialsRepository;

	private final PasswordEncoder passwordEncoder;

	private final JwtService jwtService;

	@Value("${security.jwt.expiration-time}")
	private long jwtExpiration;

	public String saveUser(RegisterRequestDto request) {

		if (userCredentialsRepository.existsByName(request.getName())) {
			throw new DuplicateResourceException("User with username " + request.getName() + " already exists");
		}

		UserCredentials user = new UserCredentials();

		user.setName(request.getName());
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setRole(UserRole.CUSTOMER);

		userCredentialsRepository.save(user);

		return "User registered successfully";
	}

	public AuthResponseDto generateToken(Authentication authentication) {

		String token = jwtService.generateToken(authentication.getName(), authentication.getAuthorities());

		return new AuthResponseDto(token, "Bearer", jwtExpiration);
	}

	public void validateToken(String token) {

		if (token == null || token.isBlank()) {
			throw new IllegalArgumentException("JWT token is required");
		}

		jwtService.validateToken(token);
	}
}
