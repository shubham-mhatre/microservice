package com.sm.inventory_service.util;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import com.sm.inventory_service.request.dto.JwtUserDetails;


@Component
public class SecurityUtils {

	public JwtUserDetails getAuthenticatedUser() {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
			throw new IllegalStateException("Invalid authentication");
		}

		Number userIdClaim = jwtAuthentication.getToken().getClaim("userId");

		Integer userId = userIdClaim != null ? userIdClaim.intValue() : null;

		String username = jwtAuthentication.getToken().getSubject();

		List<String> roles = jwtAuthentication.getToken().getClaimAsStringList("roles");

		return new JwtUserDetails(userId, username, roles);
	}
}
