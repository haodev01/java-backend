package com.elearning.dto;

// order dùng Integer (boxed) chứ không phải int nguyên thuỷ — cần phân biệt
// được "client không gửi order" (null, giữ nguyên) với "client gửi order=0".
public record UpdateChapterRequest(String title, Integer order) {}
