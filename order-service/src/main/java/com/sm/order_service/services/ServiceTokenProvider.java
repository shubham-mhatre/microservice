package com.sm.order_service.services;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * This class caches the token so Product Service doesn't call Auth Service for
 * every Inventory request.
 */
@Component
@RequiredArgsConstructor
public class ServiceTokenProvider {

	private final ServiceTokenClient serviceTokenClient;

	@Value("${security.service.token-cache-duration}")
	private long tokenCacheDuration;

	private String cachedToken;
	private Instant tokenExpiry;

	public synchronized String getToken() {

		if (cachedToken == null || tokenExpiry == null || Instant.now().isAfter(tokenExpiry)) {

			cachedToken = serviceTokenClient.getServiceToken();

			tokenExpiry = Instant.now().plusMillis(tokenCacheDuration);
		}

		return cachedToken;
	}
}