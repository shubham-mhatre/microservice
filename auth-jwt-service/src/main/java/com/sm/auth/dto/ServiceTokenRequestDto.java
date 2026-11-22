package com.sm.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceTokenRequestDto {

	@NotBlank(message = "Client ID is required")
	private String clientId;

	@NotBlank(message = "Client secret is required")
	private String clientSecret;
}