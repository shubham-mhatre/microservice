package com.sm.inventory_service.request.dto;

import java.util.List;

public record JwtUserDetails(Integer userId, String username, List<String> roles) {
}
