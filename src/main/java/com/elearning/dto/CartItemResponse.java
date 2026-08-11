package com.elearning.dto;

import java.math.BigDecimal;

// Redis chỉ lưu (courseId, quantity) — title/slug/thumbnailUrl/price phải
// join thêm từ MySQL (nguồn dữ liệu thật) mỗi lần đọc giỏ hàng, không lưu
// trùng trong Redis để tránh lệch dữ liệu khi course đổi giá/tên.
public record CartItemResponse(Long courseId, String title, String slug, String thumbnailUrl,
                               BigDecimal price, int quantity, BigDecimal subtotal) {}