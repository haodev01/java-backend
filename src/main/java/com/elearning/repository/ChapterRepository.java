package com.elearning.repository;

import com.elearning.model.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

// Chỉ dùng để TRA CỨU trực tiếp 1 Chapter theo id (thêm lesson/video vào chapter
// cụ thể mà không phải tải nguyên cây Course). KHÔNG phá nguyên tắc aggregate
// root ở Lesson 0012 — vẫn không có cách nào tạo Chapter đứng một mình ngoài
// Course, và vẫn không có LessonRepository.
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
}
