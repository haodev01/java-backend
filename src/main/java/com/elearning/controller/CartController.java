package com.elearning.controller;

import com.elearning.dto.AddToCartRequest;
import com.elearning.dto.ApiResponse;
import com.elearning.dto.CartResponse;
import com.elearning.dto.UpdateCartItemRequest;
import com.elearning.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// Không cần @PreAuthorize riêng — .anyRequest().authenticated() trong
// SecurityConfig đã bắt buộc đăng nhập cho MỌI endpoint ở đây. Giỏ hàng không
// phân biệt role: STUDENT/INSTRUCTOR/ADMIN đều có giỏ hàng riêng như nhau.
@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("OK", cartService.getCart(authentication.getName())));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(Authentication authentication,
                                                             @RequestBody AddToCartRequest request) {
        int quantity = request.quantity() != null ? request.quantity() : 1;
        cartService.addItem(authentication.getName(), request.courseId(), quantity);
        return ResponseEntity.ok(ApiResponse.success("Đã thêm vào giỏ hàng", cartService.getCart(authentication.getName())));
    }

    @PutMapping("/items/{courseId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(Authentication authentication,
                                                                @PathVariable Long courseId, @RequestBody UpdateCartItemRequest request) {
        cartService.updateItemQuantity(authentication.getName(), courseId, request.quantity());
        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật giỏ hàng", cartService.getCart(authentication.getName())));
    }

    @DeleteMapping("/items/{courseId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(Authentication authentication, @PathVariable Long courseId) {
        cartService.removeItem(authentication.getName(), courseId);
        return ResponseEntity.ok(ApiResponse.success("Đã xoá khỏi giỏ hàng", cartService.getCart(authentication.getName())));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(Authentication authentication) {
        cartService.clearCart(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Đã xoá sạch giỏ hàng", null));
    }
}