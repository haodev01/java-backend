package com.elearning.dto;

// Đặt ở dto/ vì cùng bản chất "gói dữ liệu đơn giản" như DTO — record này
// không có hành vi, không phải entity JPA, chỉ mang đúng 1 thông tin cần
// thiết (orderId) cho bất kỳ ai lắng nghe event này.
public record OrderPaidEvent(Long orderId) {}