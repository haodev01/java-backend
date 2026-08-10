package com.elearning.dto;

public record ResetPasswordRequest(String token, String newPassword) {}