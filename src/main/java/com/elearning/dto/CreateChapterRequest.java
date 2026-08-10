package com.elearning.dto;

import java.util.List;

public record CreateChapterRequest(String title, int order, List<CreateLessonRequest> lessons) {}