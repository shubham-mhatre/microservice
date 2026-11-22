package com.sm.auth.entity.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
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

	@Value("${security.service.order.client-id}")
	private String orderClientId;

	@Value("${security.service.order.client-secret}")
	private String orderClientSecret;
	
	@Value("${security.service.payment.client-id}")
	private String paymentClientId;

	@Value("${security.service.payment.client-secret}")
	private String paymentClientSecret;

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

		String serviceName;
		String serviceRole;
		String expectedSecret;

		if (productClientId.equals(request.getClientId())) {

			serviceName = "product-service";
			serviceRole = "ROLE_SERVICE_PRODUCT";
			expectedSecret = productClientSecret;

		} else if (orderClientId.equals(request.getClientId())) {

			serviceName = "order-service";
			serviceRole = "ROLE_SERVICE_ORDER";
			expectedSecret = orderClientSecret;

		}else if (paymentClientId.equals(request.getClientId())) {

		    serviceName = "payment-service";
		    serviceRole = "ROLE_SERVICE_PAYMENT";
		    expectedSecret = paymentClientSecret;

		} else {
			throw new BadCredentialsException("Invalid service credentials");
		}

		boolean validSecret = MessageDigest.isEqual(expectedSecret.getBytes(StandardCharsets.UTF_8),
				request.getClientSecret().getBytes(StandardCharsets.UTF_8));

		if (!validSecret) {
			throw new BadCredentialsException("Invalid service credentials");
		}

		String token = jwtService.generateServiceToken(serviceName, List.of(serviceRole));

		return new AuthResponseDto(token, "Bearer", serviceJwtExpiration);
	}

	public void validateToken(String token) {

		if (token == null || token.isBlank()) {
			throw new IllegalArgumentException("JWT token is required");
		}

		jwtService.validateToken(token);
	}
}