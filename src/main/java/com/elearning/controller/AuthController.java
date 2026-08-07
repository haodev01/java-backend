package com.elearning.controller;


import com.elearning.dto.AuthResponse;
import com.elearning.dto.LoginRequest;
import com.elearning.dto.RegisterRequest;
import com.elearning.model.User;
import com.elearning.security.JwtService;
import com.elearning.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("register")
    public ResponseEntity<User>  register(@RequestBody RegisterRequest request) {
        User user = userService.register(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(user);
    }

    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            User user = userService.login(request.email(), request.password());

            String accessToken = jwtService.generateAccessToken(user.getEmail());
            String refreshToken = jwtService.generateRefreshToken(user.getEmail());

            return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @GetMapping("me")
    public  ResponseEntity<?> me(Authentication authentication) {
        return ResponseEntity.ok(authentication.getName());
    }
}
