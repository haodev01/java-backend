package com.elearning.controller;


import com.elearning.dto.*;
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
    public ResponseEntity<ApiResponse<User>>  register(@RequestBody RegisterRequest request) {
        User user = userService.register(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(ApiResponse.success("Đăng ký thành công", user));
    }

    @PostMapping("login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        User user = userService.login(request.email(), request.password());
        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());
        refreshTokenStore.save(user.getEmail(), refreshToken);
        AuthResponse tokens = new AuthResponse(accessToken, refreshToken, user);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", tokens));
    }

    @GetMapping("me")
    public  ResponseEntity<ApiResponse<String>> me(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin người dùng thành công", email));
    }

    @PostMapping("refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest request) {
        String email = jwtService.extractEmail(request.refreshToken());
        if (!refreshTokenStore.isValid(email, request.refreshToken())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "Refresh token không hợp lệ hoặc đã bị thu hồi"));
        }
        User user = userService.getByEmail(email);
        String newAccessToken = jwtService.generateAccessToken(email, user.getRole());
        AuthResponse response = new AuthResponse(newAccessToken, request.refreshToken(), user);
        return ResponseEntity.ok(ApiResponse.success("Refresh Token thành công", response));
    }
    @PostMapping("logout")
    public ResponseEntity<?> logout(Authentication authentication) {
        // authentication.getName() = email, do JwtAuthFilter set từ access token —
        // /logout KHÔNG nằm trong permitAll nên bắt buộc phải có access token hợp lệ.
        refreshTokenStore.revoke(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Đã đăng xuất", true));
    }

    @GetMapping("admin-only")
    @PreAuthorize("hasRole('ADMIN')")
    public  ResponseEntity<ApiResponse<String>> adminOnly() {
        return ResponseEntity.ok(ApiResponse.success("Thành công", "ADMIN it me"));
    }

    @PutMapping("change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(Authentication authentication,
                                                 @RequestBody ChangePasswordRequest request) {
        userService.changePassword(authentication.getName(), request.oldPassword(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công", ""));
    }

    @PostMapping("forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        userService.requestPasswordReset(request.email());
        return ResponseEntity.ok(ApiResponse.success("Nếu email tồn tại trong hệ thống, link đặt lại mật khẩu đã được gửi", ""));
    }

    @PostMapping("reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.success("Đặt lại mật khẩu thành công",  ""));
    }

}
