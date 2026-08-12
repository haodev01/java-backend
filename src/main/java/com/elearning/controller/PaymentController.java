package com.elearning.controller;

import com.elearning.dto.ApiResponse;
import com.elearning.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{orderId}/create-url")
    public ResponseEntity<ApiResponse<String>> createUrl(Authentication authentication,
                                                         @PathVariable Long orderId, HttpServletRequest request) {
        String url = paymentService.createPaymentUrl(authentication.getName(), orderId, request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success("Tạo link thanh toán thành công", url));
    }

    // VNPay gọi IPN bằng GET, toàn bộ tham số nằm trên query string — KHÔNG
    // dùng @RequestBody. Response PHẢI đúng format JSON RIÊNG mà VNPay quy
    // định ({"RspCode":"...","Message":"..."}), KHÔNG PHẢI ApiResponse<T>
    // thường dùng cho API của chính mình — VNPay đọc đúng 2 field này để
    // quyết định có cần gọi lại (retry) hay không.
    @GetMapping("/vnpay-ipn")
    public ResponseEntity<Map<String, String>> vnpayIpn(@RequestParam Map<String, String> allParams) {
        try {
            paymentService.handleIpnCallback(allParams);
            return ResponseEntity.ok(Map.of("RspCode", "00", "Message", "Confirm Success"));
        } catch (Exception e) {
            // KHÔNG ném exception để GlobalExceptionHandler xử lý như bình
            // thường — VNPay cần ĐÚNG format RspCode/Message này để hiểu kết
            // quả, trả về ApiResponse/ErrorResponse thông thường sẽ khiến
            // VNPay không parse được và cứ gọi lại IPN liên tục.
            return ResponseEntity.ok(Map.of("RspCode", "99", "Message", "Confirm Fail"));
        }
    }
    @GetMapping("/vnpay-return")
    public ResponseEntity<String> vnpayReturn(@RequestParam Map<String, String> allParams) {
        boolean success = "00".equals(allParams.get("vnp_ResponseCode"));
        String message = success
                ? "Thanh toán thành công! Vui lòng quay lại ứng dụng."
                : "Thanh toán thất bại hoặc đã huỷ. Vui lòng thử lại.";
        return ResponseEntity.ok(message);
    }
}