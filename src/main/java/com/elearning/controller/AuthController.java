package com.elearning.controller;


import com.elearning.dto.LoginRequest;
import com.elearning.dto.RegisterRequest;
import com.elearning.model.User;
import com.elearning.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("register")
    public ResponseEntity<User>  register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.register(request.getEmail(), request.getPassword()));
    }

    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            User user = userService.login(request.email(), request.password());
            // Chưa có JWT ở lesson này — chỉ xác nhận đăng nhập đúng.
            // Lesson 0004 sẽ thay response này bằng access token + refresh token thật.
            return ResponseEntity.ok(user);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}
