package com.elearning.controller;

import com.elearning.dto.*;
import com.elearning.model.Chapter;
import com.elearning.model.Course;
import com.elearning.service.CourseService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    public ResponseEntity<ApiResponse<Course>> create(Authentication authentication,
                                                      @RequestBody CreateCourseRequest request) {
        // authentication.getName() = email — instructor luôn là NGƯỜI ĐANG ĐĂNG
        // NHẬP, không tin instructorId nào client tự gửi lên trong body.
        Course course = courseService.createCourse(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Tạo khoá học thành công", course));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CourseSummary>>> list(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String slug,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("OK", courseService.listCourses(title, slug, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Course>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("OK", courseService.getCourse(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    public ResponseEntity<ApiResponse<Course>> update(Authentication authentication,
                                                       @PathVariable Long id, @RequestBody UpdateCourseRequest request) {
        Course course = courseService.updateCourse(authentication.getName(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật khoá học thành công", course));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(Authentication authentication, @PathVariable Long id) {
        courseService.deleteCourse(authentication.getName(), id);
        return ResponseEntity.ok(ApiResponse.success("Xoá khoá học thành công", null));
    }

    @PostMapping("/{courseId}/chapters")
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    public ResponseEntity<ApiResponse<Course>> addChapter(Authentication authentication,
                                                          @PathVariable Long courseId, @RequestBody CreateChapterRequest request) {
        Course course = courseService.addChapter(authentication.getName(), courseId, request);
        return ResponseEntity.ok(ApiResponse.success("Thêm chương thành công", course));
    }

    @PutMapping("/{courseId}/chapters/{chapterId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    public ResponseEntity<ApiResponse<Chapter>> updateChapter(Authentication authentication,
                                                               @PathVariable Long courseId, @PathVariable Long chapterId,
                                                               @RequestBody UpdateChapterRequest request) {
        Chapter chapter = courseService.updateChapter(authentication.getName(), courseId, chapterId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật chương thành công", chapter));
    }

    @DeleteMapping("/{courseId}/chapters/{chapterId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteChapter(Authentication authentication,
                                                            @PathVariable Long courseId, @PathVariable Long chapterId) {
        courseService.deleteChapter(authentication.getName(), courseId, chapterId);
        return ResponseEntity.ok(ApiResponse.success("Xoá chương thành công", null));
    }

    @PostMapping("/{courseId}/chapters/{chapterId}/lessons")
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    public ResponseEntity<ApiResponse<Chapter>> addLesson(Authentication authentication,
                                                           @PathVariable Long courseId, @PathVariable Long chapterId,
                                                           @RequestBody CreateLessonRequest request) {
        Chapter chapter = courseService.addLesson(authentication.getName(), courseId, chapterId, request);
        return ResponseEntity.ok(ApiResponse.success("Thêm bài học thành công", chapter));
    }

    @PutMapping("/{courseId}/chapters/{chapterId}/lessons/{lessonId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    public ResponseEntity<ApiResponse<Chapter>> updateLesson(Authentication authentication,
                                                              @PathVariable Long courseId, @PathVariable Long chapterId,
                                                              @PathVariable Long lessonId, @RequestBody UpdateLessonRequest request) {
        Chapter chapter = courseService.updateLesson(authentication.getName(), courseId, chapterId, lessonId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật bài học thành công", chapter));
    }

    @DeleteMapping("/{courseId}/chapters/{chapterId}/lessons/{lessonId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    public ResponseEntity<ApiResponse<Chapter>> deleteLesson(Authentication authentication,
                                                              @PathVariable Long courseId, @PathVariable Long chapterId,
                                                              @PathVariable Long lessonId) {
        Chapter chapter = courseService.deleteLesson(authentication.getName(), courseId, chapterId, lessonId);
        return ResponseEntity.ok(ApiResponse.success("Xoá bài học thành công", chapter));
    }

    @PostMapping(value = "/{courseId}/thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    public ResponseEntity<ApiResponse<Course>> uploadThumbnail(Authentication authentication,
                                                               @PathVariable Long courseId, @RequestParam("file") MultipartFile file) {
        Course course = courseService.uploadThumbnail(authentication.getName(), courseId, file);
        return ResponseEntity.ok(ApiResponse.success("Tải thumbnail thành công", course));
    }

    @PostMapping(value = "/{courseId}/chapters/{chapterId}/lessons/{lessonId}/video",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    public ResponseEntity<ApiResponse<Chapter>> uploadLessonVideo(Authentication authentication,
                                                                  @PathVariable Long courseId, @PathVariable Long chapterId, @PathVariable Long lessonId,
                                                                  @RequestParam("file") MultipartFile file) {
        Chapter chapter = courseService.uploadLessonVideo(
                authentication.getName(), courseId, chapterId, lessonId, file);
        return ResponseEntity.ok(ApiResponse.success("Tải video thành công", chapter));
    }
}