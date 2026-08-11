package com.elearning.controller;

import com.elearning.dto.ApiResponse;
import com.elearning.model.Order;
import com.elearning.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Không cần @PreAuthorize riêng cho phần lớn endpoint — .anyRequest().authenticated()
// trong SecurityConfig đã bắt buộc đăng nhập. Riêng markPaid() cần ADMIN vì
// đây là thao tác GIẢ LẬP thủ công (Phase 5 - Payment sẽ thay bằng callback
// thật từ cổng thanh toán, không phải ai gọi cũng đánh dấu "đã trả tiền" được).
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<Order>> checkout(Authentication authentication) {
        Order order = orderService.checkout(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Đặt hàng thành công", order));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Order>>> myOrders(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("OK", orderService.myOrders(authentication.getName())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("OK", orderService.getOrder(id)));
    }

    @PostMapping("/{id}/mark-paid")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Order>> markPaid(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu đã thanh toán", orderService.markPaid(id)));
    }
}