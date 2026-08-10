package com.elearning.repository;

import com.elearning.dto.CourseSummary;
import com.elearning.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    boolean existsBySlug(String slug);


    @Override
    @EntityGraph(attributePaths = {"chapters", "chapters.lessons"})
    Optional<Course> findById(Long id);

    // :title/:slug IS NULL OR ... — điều kiện TUỲ CHỌN: null thì bỏ qua điều
    // kiện đó, có giá trị thì lọc. Đơn giản hơn Specification khi chỉ có vài
    // field lọc; đáng chuyển sang Specification nếu sau này filter tăng lên
    // nhiều field kết hợp AND/OR linh hoạt (chưa cần ở quy mô hiện tại — YAGNI).
    @Query(value = "SELECT new com.elearning.dto.CourseSummary(" +
            "c.id, c.title, c.slug, c.price, c.description, c.instructor.email, SIZE(c.chapters)) " +
            "FROM Course c " +
            "WHERE (:title IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:slug IS NULL OR c.slug = :slug)",
            countQuery = "SELECT COUNT(c) FROM Course c " +
            "WHERE (:title IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:slug IS NULL OR c.slug = :slug)")
    Page<CourseSummary> findAllSummaries(@Param("title") String title, @Param("slug") String slug, Pageable pageable);
}
