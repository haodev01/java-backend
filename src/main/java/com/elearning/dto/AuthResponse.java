package com.elearning.dto;

import com.elearning.model.User;

import java.util.Optional;

public record AuthResponse(String accessToken, String refreshToken, User user){}