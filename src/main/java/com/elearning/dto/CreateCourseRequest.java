package com.elearning.dto;

import java.math.BigDecimal;
import java.util.List;

public record CreateCourseRequest(String title, String slug, BigDecimal price, String description,
                                   List<CreateChapterRequest> chapters) {}