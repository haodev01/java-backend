package com.elearning.repository;

import com.elearning.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
    boolean existsBySlug(String slug);
}
