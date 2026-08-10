package com.elearning.service;


import com.elearning.dto.*;
import com.elearning.exception.NotFoundException;
import com.elearning.model.Chapter;
import com.elearning.model.Course;
import com.elearning.model.Lesson;
import com.elearning.model.User;
import com.elearning.exception.InvalidFileException;
import com.elearning.model.Role;
import com.elearning.repository.ChapterRepository;
import com.elearning.repository.CourseRepository;
import com.elearning.storage.FileStorageService;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

@Service
public class CourseService {

    private static final long MAX_THUMBNAIL_BYTES = 2L * 1024 * 1024;   // 2MB
    private static final long MAX_VIDEO_BYTES = 500L * 1024 * 1024;     // 500MB

    private  final CourseRepository courseRepository;
    private  final ChapterRepository chapterRepository;
    private  final UserService userService;
    private  final FileStorageService fileStorageService;

    public CourseService(CourseRepository courseRepository, ChapterRepository chapterRepository,
                          UserService userService, FileStorageService fileStorageService) {
        this.courseRepository = courseRepository;
        this.chapterRepository = chapterRepository;
        this.userService = userService;
        this.fileStorageService = fileStorageService;
    }

    @CacheEvict(value = "courseList", allEntries = true)
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
    @Cacheable("courseList")
    public PageResponse<CourseSummary> listCourses(String title, String slug, Pageable pageable) {
        return PageResponse.from(courseRepository.findAllSummaries(title, slug, pageable));
    }
    @CacheEvict(value = "courseList", allEntries = true)
    public Course getCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy khoá học id=" + id));
    }
    @CacheEvict(value = "courseList", allEntries = true)
    public Course addChapter(String requesterEmail, Long courseId, CreateChapterRequest request) {
        Course course = getCourse(courseId);
        ensureOwnerOrAdmin(requesterEmail, course);

        Chapter chapter = new Chapter(request.title(), request.order());
        for (CreateLessonRequest lessonReq : request.lessons()) {
            chapter.addLesson(new Lesson(lessonReq.title(), lessonReq.order(), lessonReq.contentType()));
        }
        course.addChapter(chapter);
        return courseRepository.save(course); // save Course — cascade tự INSERT đúng 1 chapter mới, không đụng chapter cũ
    }

    @CacheEvict(value = "courseList", allEntries = true)
    public Chapter addLesson(String requesterEmail, Long courseId, Long chapterId, CreateLessonRequest request) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy chương id=" + chapterId));

        // Chapter này có thật sự thuộc đúng Course trong URL không? Chặn trường hợp
        // ai đó truyền courseId và chapterId không khớp nhau.
        if (!chapter.getCourse().getId().equals(courseId)) {
            throw new NotFoundException("Chương id=" + chapterId + " không thuộc khoá học id=" + courseId);
        }
        ensureOwnerOrAdmin(requesterEmail, chapter.getCourse());

        chapter.addLesson(new Lesson(request.title(), request.order(), request.contentType()));
        chapterRepository.save(chapter); // cascade tự INSERT lesson mới
        return chapter;
    }

    public Course uploadThumbnail(String requesterEmail, Long courseId, MultipartFile file) {
        Course course = getCourse(courseId);
        ensureOwnerOrAdmin(requesterEmail, course);
        validateFile(file, "image/", MAX_THUMBNAIL_BYTES, "Thumbnail phải là ảnh, tối đa 2MB");

        course.attachThumbnail(fileStorageService.store(file, "thumbnails"));
        return courseRepository.save(course);
    }

    public Chapter uploadLessonVideo(String requesterEmail, Long courseId, Long chapterId,
                                     Long lessonId, MultipartFile file) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy chương id=" + chapterId));
        if (!chapter.getCourse().getId().equals(courseId)) {
            throw new NotFoundException("Chương id=" + chapterId + " không thuộc khoá học id=" + courseId);
        }
        ensureOwnerOrAdmin(requesterEmail, chapter.getCourse());

        Lesson lesson = chapter.getLessons().stream()
                .filter(l -> l.getId().equals(lessonId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bài học id=" + lessonId));

        validateFile(file, "video/", MAX_VIDEO_BYTES, "Video bài học phải là file video, tối đa 500MB");

        lesson.attachContentUrl(fileStorageService.store(file, "lessons"));
        chapterRepository.save(chapter); // cascade cập nhật đúng Lesson vừa sửa
        return chapter;
    }

    // Từ Lesson 0012 (Bước 8) — kiểm tra ĐÚNG người sở hữu course cụ thể, không
    // chỉ kiểm tra vai trò (role-based). @PreAuthorize ở Controller chỉ chặn
    // được "có phải INSTRUCTOR/ADMIN không", KHÔNG chặn được "có phải chủ khoá
    // học NÀY không" — 2 câu hỏi khác nhau, cần 2 cơ chế khác nhau.
    private void ensureOwnerOrAdmin(String requesterEmail, Course course) {
        boolean isOwner = course.getInstructor().getEmail().equals(requesterEmail);
        boolean isAdmin = userService.getByEmail(requesterEmail).getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Bạn không phải giảng viên của khoá học này");
        }
    }

    // Từ Lesson 0015 — validate loại file + kích thước TRƯỚC khi lưu, không tin
    // Content-Type client tự khai báo là đủ an toàn nhưng vẫn là lớp chặn đầu
    // tiên hợp lý (rẻ, nhanh) trước khi ghi file thật lên đĩa.
    private void validateFile(MultipartFile file, String requiredTypePrefix, long maxBytes, String errorMessage) {
        if (file.isEmpty()) {
            throw new InvalidFileException("File rỗng");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith(requiredTypePrefix)) {
            throw new InvalidFileException(errorMessage + " (nhận được: " + contentType + ")");
        }
        if (file.getSize() > maxBytes) {
            throw new InvalidFileException(errorMessage);
        }
    }
}
