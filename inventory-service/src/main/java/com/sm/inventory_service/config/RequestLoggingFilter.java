
package com.sm.inventory_service.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		long startTime = System.nanoTime();

		try {
			filterChain.doFilter(request, response);

		} finally {

			long duration = (System.nanoTime() - startTime) / 1_000_000;

			int status = response.getStatus();

			if (status >= 500) {
				log.error("HTTP {} {} | Status: {} | Duration: {} ms", request.getMethod(), request.getRequestURI(),
						status, duration);

			} else if (status >= 400) {
				log.warn("HTTP {} {} | Status: {} | Duration: {} ms", request.getMethod(), request.getRequestURI(),
						status, duration);

			} else {
				log.info("HTTP {} {} | Status: {} | Duration: {} ms", request.getMethod(), request.getRequestURI(),
						status, duration);
			}
		}
	}
}
