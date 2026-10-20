package com.sm.product_service.dto.response;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        Object errors
) {
    public static <T> ApiResponse<T> success(
            String message, T data) {
        return new ApiResponse<>(true, message, data, null);
    }

    public static <T> ApiResponse<T> failure(
            String message, Object errors) {
        return new ApiResponse<>(false, message, null, errors);
    }
}
