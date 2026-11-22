package com.sm.auth.entity.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sm.auth.config.CustomUserDetails;
import com.sm.auth.dto.RegisterRequestDto;
import com.sm.auth.dto.ServiceTokenRequestDto;
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

	@Value("${security.jwt.service-expiration-time:900000}")
	private long serviceJwtExpiration;

	@Value("${security.service.product.client-id}")
	private String productClientId;

	@Value("${security.service.product.client-secret}")
	private String productClientSecret;

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

		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

		String token = jwtService.generateToken(authentication.getName(), userDetails.getId(),
				authentication.getAuthorities());

		return new AuthResponseDto(token, "Bearer", jwtExpiration);
	}

	public AuthResponseDto generateServiceToken(ServiceTokenRequestDto request) {

		boolean validClientId = productClientId.equals(request.getClientId());

		boolean validClientSecret = MessageDigest.isEqual(productClientSecret.getBytes(StandardCharsets.UTF_8),
				request.getClientSecret().getBytes(StandardCharsets.UTF_8));

		if (!validClientId || !validClientSecret) {
			throw new org.springframework.security.authentication.BadCredentialsException(
					"Invalid service credentials");
		}

		String token = jwtService.generateServiceToken("product-service", List.of("ROLE_SERVICE_PRODUCT"));

		return new AuthResponseDto(token, "Bearer", serviceJwtExpiration);
	}

	public void validateToken(String token) {

		if (token == null || token.isBlank()) {
			throw new IllegalArgumentException("JWT token is required");
		}

		jwtService.validateToken(token);
	}
}