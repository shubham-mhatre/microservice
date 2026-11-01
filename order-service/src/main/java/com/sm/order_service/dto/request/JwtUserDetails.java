package com.sm.order_service.dto.request;

import java.util.List;

public record JwtUserDetails(Integer userId, String username, List<String> roles) {
}
