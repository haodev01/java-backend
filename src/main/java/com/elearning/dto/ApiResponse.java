package com.elearning.dto;

public record ApiResponse<T>(String message, boolean success, int code, T data) {

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(message, true, 200, data);
    }

    public static <T> ApiResponse<T> success(int code, String message, T data) {
        return new ApiResponse<>(message, true, code, data);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        // data luôn null khi lỗi — không có gì để trả về, không giả vờ có.
        return new ApiResponse<>(message, false, code, null);
    }
}
