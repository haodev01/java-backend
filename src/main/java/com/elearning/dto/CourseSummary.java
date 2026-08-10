package com.elearning.dto;

import java.math.BigDecimal;

public record CourseSummary(Long id, String title, String slug, BigDecimal price, String description,
                            String instructorEmail, long chapterCount) {}