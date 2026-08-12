package com.elearning.repository;

import com.elearning.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

// CHỈ dùng để ĐỌC 1 Lesson theo id (Progress cần) — không thêm bất kỳ method
// tạo/sửa/xoá nào ở đây. Mọi thao tác GHI vẫn phải qua Course/Chapter, giữ
// nguyên nguyên tắc aggregate root từ Lesson 0012.
public interface LessonRepository extends JpaRepository<Lesson, Long> {
}