package com.elearning.service;


import com.elearning.dto.CourseSummary;
import com.elearning.dto.CreateChapterRequest;
import com.elearning.dto.CreateCourseRequest;
import com.elearning.dto.CreateLessonRequest;
import com.elearning.exception.NotFoundException;
import com.elearning.model.Chapter;
import com.elearning.model.Course;
import com.elearning.model.Lesson;
import com.elearning.model.User;
import com.elearning.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    private  final CourseRepository courseRepository;
    private  final UserService userService;

    public CourseService(CourseRepository courseRepository, UserService userService) {
        this.courseRepository = courseRepository;
        this.userService = userService;
    }

    public Course createCourse(String instructorEmail, CreateCourseRequest request) {
        // Check chủ động trước — cùng nguyên tắc email ở UserService.register()
        // (Lesson 0001): fail nhanh với message rõ ràng, thay vì để tới tận DB
        // constraint mới báo lỗi (xem thêm DataIntegrityViolationException handler
        // trong GlobalExceptionHandler — vẫn cần giữ, phòng race condition: 2
        // request cùng slug gửi gần như đồng thời có thể cùng vượt qua check này).
        if (courseRepository.existsBySlug(request.slug())) {
            throw new IllegalStateException("Slug '" + request.slug() + "' đã tồn tại");
        }

        User instructor = userService.getByEmail(instructorEmail);

        Course course = new Course(request.title(), request.slug(), request.price(), instructor);

        for (CreateChapterRequest chapterReq : request.chapters()) {
            Chapter chapter = new Chapter(chapterReq.title(), chapterReq.order());
            for (CreateLessonRequest lessonReq : chapterReq.lessons()) {
                chapter.addLesson(new Lesson(lessonReq.title(), lessonReq.order(), lessonReq.contentType()));
            }
            course.addChapter(chapter);
        }
        return courseRepository.save(course);
    }

    public List<CourseSummary> listCourses() {
        return courseRepository.findAllSummaries();
    }

    public Course getCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy khoá học id=" + id));
    }

}
