package com.sm.auth.dto.response;

public record AuthResponseDto(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
