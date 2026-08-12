package com.elearning.service;

import com.elearning.exception.NotFoundException;
import com.elearning.model.Enrollment;
import com.elearning.model.Lesson;
import com.elearning.model.Progress;
import com.elearning.repository.EnrollmentRepository;
import com.elearning.repository.LessonRepository;
import com.elearning.repository.ProgressRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserService userService;

    public ProgressService(ProgressRepository progressRepository, LessonRepository lessonRepository,
                           EnrollmentRepository enrollmentRepository, UserService userService) {
        this.progressRepository = progressRepository;
        this.lessonRepository = lessonRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userService = userService;
    }

    public Progress markLessonComplete(String email, Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bài học id=" + lessonId));
        var user = userService.getByEmail(email);
        // Đi từ Lesson lên Chapter lên Course — cần getChapter() vừa thêm ở
        // Bước 1, và Chapter.getCourse() đã có sẵn từ Lesson 0012.
        var course = lesson.getChapter().getCourse();

        // Chưa enroll thì KHÔNG cho đánh dấu hoàn thành — chặn trường hợp
        // đánh tiến độ cho khoá học chưa hề mua.
        Enrollment enrollment = enrollmentRepository.findByUserAndCourse(user, course)
                .orElseThrow(() -> new AccessDeniedException("Bạn chưa đăng ký khoá học này"));

        Progress progress = progressRepository.findByEnrollmentAndLesson(enrollment, lesson)
                .orElseGet(() -> new Progress(enrollment, lesson));
        progress.markComplete();
        return progressRepository.save(progress);
    }

    // Rollup: % hoàn thành = số lesson đã complete / tổng số lesson của course.
    // Tính lúc đọc (on-the-fly) thay vì lưu sẵn %  trong DB — đơn giản hơn,
    // không lo đồng bộ lệch giữa 2 nguồn số liệu. Tối ưu lại (lưu cache) chỉ
    // khi đo thấy thật sự cần (YAGN — chưa có traffic thật để biết có cần không).
    public double calculateCompletion(String email, Long courseId, com.elearning.repository.CourseRepository courseRepository) {
        var user = userService.getByEmail(email);
        var course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy khoá học id=" + courseId));
        Enrollment enrollment = enrollmentRepository.findByUserAndCourse(user, course)
                .orElseThrow(() -> new AccessDeniedException("Bạn chưa đăng ký khoá học này"));

        int totalLessons = course.getChapters().stream()
                .mapToInt(ch -> ch.getLessons().size())
                .sum();
        if (totalLessons == 0) return 0.0;

        long completedCount = progressRepository.countByEnrollmentAndCompletedTrue(enrollment);
        return (completedCount * 100.0) / totalLessons;
    }
}
