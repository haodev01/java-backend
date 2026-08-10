package com.elearning.dto;

public record ChangePasswordRequest(String oldPassword, String newPassword) {}
