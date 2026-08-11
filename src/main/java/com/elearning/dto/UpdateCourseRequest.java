package com.elearning.dto;

import com.elearning.model.CourseStatus;

import java.math.BigDecimal;

// Field nào null thì GIỮ NGUYÊN giá trị cũ (partial update) — client chỉ cần
// gửi đúng field muốn sửa, không phải load lại rồi gửi nguyên object như PUT
// truyền thống. slug KHÔNG có ở đây — cố tình khoá, đổi slug sau khi đã publish
// phá mọi link cũ trỏ tới course, cần luồng riêng có cảnh báo nếu thực sự cần.
public record UpdateCourseRequest(String title, String description, BigDecimal price, CourseStatus status) {}
