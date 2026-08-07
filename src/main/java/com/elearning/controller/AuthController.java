package com.elearning.controller;


import com.elearning.dto.AuthResponse;
import com.elearning.dto.LoginRequest;
import com.elearning.dto.RefreshRequest;
import com.elearning.dto.RegisterRequest;
import com.elearning.model.User;
import com.elearning.security.JwtService;
import com.elearning.security.RefreshTokenStore;
import com.elearning.service.UserService;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenStore refreshTokenStore;
    public AuthController(UserService userService, JwtService jwtService, RefreshTokenStore refreshTokenStore) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.refreshTokenStore = refreshTokenStore;
    }

    @PostMapping("register")
    public ResponseEntity<User>  register(@RequestBody RegisterRequest request) {
        User user = userService.register(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(user);
    }

    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userService.login(request.email(), request.password());
        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());
        refreshTokenStore.save(user.getEmail(), refreshToken);
        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }

    @GetMapping("me")
    public  ResponseEntity<?> me(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(email);
    }

    @PostMapping("refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest request) {
        String email = jwtService.extractEmail(request.refreshToken());
        if (!refreshTokenStore.isValid(email, request.refreshToken())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Refresh token không hợp lệ hoặc đã bị thu hồi");
        }
        User user = userService.getByEmail(email);
        String newAccessToken = jwtService.generateAccessToken(email, user.getRole());

        return ResponseEntity.ok(new AuthResponse(newAccessToken, request.refreshToken()));
    }
    @PostMapping("logout")
    public ResponseEntity<?> logout(Authentication authentication) {
        // authentication.getName() = email, do JwtAuthFilter set từ access token —
        // /logout KHÔNG nằm trong permitAll nên bắt buộc phải có access token hợp lệ.
        refreshTokenStore.revoke(authentication.getName());
        return ResponseEntity.ok("Đã đăng xuất");
    }

    @GetMapping("admin-only")
    @PreAuthorize("hasRole('ADMIN')")
    public  ResponseEntity<String> adminOnly() {
        return ResponseEntity.ok("ADMIN it me");
    }

}
