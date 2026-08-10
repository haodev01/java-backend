package com.elearning.exception;

import com.elearning.dto.ApiResponse;
import com.elearning.dto.ErrorResponse;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
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
        // Log ĐẦY ĐỦ stack trace ở server — đây là chỗ DUY NHẤT bạn thấy được lỗi
        // thật. Không trả e.getMessage()/stack trace cho client (có thể lộ tên
        // bảng, đường dẫn file, thông tin driver DB) — 2 việc khác nhau: log đủ
        // cho mình, trả gọn cho client.
        log.error("Lỗi không lường trước: {}", e.getMessage(), e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Đã có lỗi xảy ra, vui lòng thử lại sau");
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException e) {
        return build(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // Lớp phòng thủ thứ 2 cho MỌI ràng buộc UNIQUE/FOREIGN KEY ở DB (không riêng
    // slug) — phòng race condition: 2 request cùng giá trị gửi gần như đồng thời
    // có thể cùng vượt qua check chủ động ở Service (check-then-act không atomic),
    // nhưng DB constraint luôn là lớp chặn cuối cùng, đáng tin cậy tuyệt đối.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("Vi phạm ràng buộc dữ liệu: {}", e.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT, "Dữ liệu bị trùng hoặc vi phạm ràng buộc, vui lòng kiểm tra lại");
    }
    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFile(InvalidFileException e) {
        return build(HttpStatus.BAD_REQUEST, e.getMessage());
    }


    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(message, status.value(), Instant.now()));
    }
}
