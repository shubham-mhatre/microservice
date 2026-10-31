package com.sm.auth.entity.service;

import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	@Value("${security.jwt.secret-key}")
	private String secretKey;

	@Value("${security.jwt.expiration-time}")
	private long jwtExpiration;

	public void validateToken(final String token) {

		if (token == null || token.isBlank()) {
			throw new IllegalArgumentException("JWT token is required");
		}

		Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
	}

	public String generateToken(String userName, Collection<? extends GrantedAuthority> authorities) {

		Map<String, Object> claims = new HashMap<>();

		List<String> roles = authorities.stream().map(GrantedAuthority::getAuthority).toList();

		claims.put("roles", roles);

		return createToken(claims, userName);
	}

	private String createToken(Map<String, Object> claims, String userName) {

		long currentTime = System.currentTimeMillis();

		return Jwts.builder().setClaims(claims).setSubject(userName).setIssuedAt(new Date(currentTime))
				.setExpiration(new Date(currentTime + jwtExpiration)).signWith(getSignKey(), SignatureAlgorithm.HS256)
				.compact();
	}

	private Key getSignKey() {

		byte[] keyBytes = Decoders.BASE64.decode(secretKey);

		return Keys.hmacShaKeyFor(keyBytes);
	}
}