package com.elearning.controller;

import com.elearning.dto.ApiResponse;
import com.elearning.dto.ProgressResponse;
import com.elearning.model.Enrollment;
import com.elearning.model.Progress;
import com.elearning.repository.CourseRepository;
import com.elearning.service.EnrollmentService;
import com.elearning.service.ProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final ProgressService progressService;
    private final CourseRepository courseRepository;

    public EnrollmentController(EnrollmentService enrollmentService, ProgressService progressService,
                                CourseRepository courseRepository) {
        this.enrollmentService = enrollmentService;
        this.progressService = progressService;
        this.courseRepository = courseRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Enrollment>>> myEnrollments(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("OK", enrollmentService.myEnrollments(authentication.getName())));
    }

    @GetMapping("/{courseId}/progress")
    public ResponseEntity<ApiResponse<ProgressResponse>> getProgress(Authentication authentication, @PathVariable Long courseId) {
        double percent = progressService.calculateCompletion(authentication.getName(), courseId, courseRepository);
        ProgressResponse progressResponse = new ProgressResponse(percent);
        return ResponseEntity.ok(ApiResponse.success("OK", progressResponse));
    }

    @PostMapping("/lessons/{lessonId}/complete")
    public ResponseEntity<ApiResponse<Progress>> completeLesson(Authentication authentication, @PathVariable Long lessonId) {
        Progress progress = progressService.markLessonComplete(authentication.getName(), lessonId);
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu hoàn thành", progress));
    }
}
