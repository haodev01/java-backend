package com.elearning.exception;

import com.elearning.dto.ErrorResponse;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e) {
        return build(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException e) {
        // Dùng chung cho cả login() sai mật khẩu lẫn refresh() bị revoke —
        // cả 2 nơi giờ chỉ cần `throw new BadCredentialsException(...)`.
        return build(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwt(JwtException e) {
        // Chỉ bắt được JwtException ném ra TRONG controller (vd refresh() gọi
        // jwtService.extractEmail() trực tiếp) — xem callout mục 1 để hiểu vì sao
        // JwtAuthFilter không đi qua đây được.
        return build(HttpStatus.UNAUTHORIZED, "Refresh token không hợp lệ hoặc đã hết hạn");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e) {
        // Bắt được vì @PreAuthorize chạy trong lúc DispatcherServlet gọi
        // controller (khác JwtAuthFilter) — vá luôn lỗi 403 thân rỗng ở Lesson 0006.
        return build(HttpStatus.FORBIDDEN, "Bạn không có quyền truy cập tài nguyên này");
    }

    // Lưới an toàn cuối cùng cho MỌI exception chưa có handler riêng.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknown(Exception e) {
        // KHÔNG trả e.getMessage() ở đây — có thể vô tình lộ chi tiết nội bộ
        // (tên bảng, đường dẫn file, thông tin driver DB...) ra ngoài cho client thật.
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Đã có lỗi xảy ra, vui lòng thử lại sau");
    }


    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(message, status.value(), Instant.now()));
    }
}
