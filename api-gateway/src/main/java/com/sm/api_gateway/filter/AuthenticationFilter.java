package com.sm.api_gateway.filter;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sm.api_gateway.util.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

	public static final String USER_HEADER = "X-Authenticated-User";
	public static final String ROLES_HEADER = "X-Authenticated-Roles";

	private final RouteValidator validator;
	private final JwtService jwtService;
	private final ObjectMapper objectMapper;

	public AuthenticationFilter(RouteValidator validator, JwtService jwtService, ObjectMapper objectMapper) {

		super(Config.class);
		this.validator = validator;
		this.jwtService = jwtService;
		this.objectMapper = objectMapper;
	}

	@Override
	public GatewayFilter apply(Config config) {

		return (exchange, chain) -> {

			if (!validator.isSecured.test(exchange.getRequest())) {
				return chain.filter(exchange);
			}

			String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

			if (authHeader == null || !authHeader.startsWith("Bearer ")) {
				return writeError(exchange, HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
			}

			String token = authHeader.substring(7).trim();

			if (token.isBlank()) {
				return writeError(exchange, HttpStatus.UNAUTHORIZED, "JWT token is required");
			}

			try {
				Claims claims = jwtService.validateToken(token);

				String username = claims.getSubject();
				Integer userId = claims.get("userId", Integer.class);

				List<?> rolesClaim = claims.get("roles", List.class);

				if (username == null || username.isBlank() || rolesClaim == null || rolesClaim.isEmpty()) {
					return writeError(exchange, HttpStatus.UNAUTHORIZED,
							"JWT does not contain required user information");
				}

				List<String> roles = rolesClaim.stream().filter(String.class::isInstance).map(String.class::cast)
						.toList();

				if (roles.isEmpty()) {
					return writeError(exchange, HttpStatus.UNAUTHORIZED, "JWT does not contain valid roles");
				}
				
				if (userId == null) {
					return writeError(exchange, HttpStatus.UNAUTHORIZED, "JWT does not contain valid userId");
				}
				
				

				// Remove client-supplied identity headers and add trusted values.
				var mutatedRequest = exchange.getRequest().mutate().headers(headers -> {
					headers.remove(USER_HEADER);
					headers.remove(ROLES_HEADER);

					headers.set(USER_HEADER, username);
					headers.set(ROLES_HEADER, String.join(",", roles));
					
					headers.remove("X-Authenticated-User-Id");
					headers.add("X-Authenticated-User-Id", userId.toString());
				}).build();

				return chain.filter(exchange.mutate().request(mutatedRequest).build());

			} catch (JwtException | IllegalArgumentException ex) {
				return writeError(exchange, HttpStatus.UNAUTHORIZED, "Invalid or expired JWT token");
			}
		};
	}

	private Mono<Void> writeError(org.springframework.web.server.ServerWebExchange exchange, HttpStatus status,
			String message) {

		exchange.getResponse().setStatusCode(status);
		exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

		Map<String, Object> response = new LinkedHashMap<>();
		response.put("time", LocalDateTime.now());
		response.put("message", message);
		response.put("status", status.value());
		response.put("error", status.getReasonPhrase());
		response.put("path", exchange.getRequest().getURI().getPath());
		response.put("success", false);

		try {
			byte[] bytes = objectMapper.writeValueAsBytes(response);

			return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));

		} catch (Exception ex) {
			return Mono.error(ex);
		}
	}

	public static class Config {
	}
}