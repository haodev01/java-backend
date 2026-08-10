package com.elearning.repository;

import com.elearning.dto.CourseSummary;
import com.elearning.model.Course;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    boolean existsBySlug(String slug);


    @Override
    @EntityGraph(attributePaths = {"chapters", "chapters.lessons"})
    Optional<Course> findById(Long id);

    @Query("SELECT new com.elearning.dto.CourseSummary(" +
            "c.id, c.title, c.slug, c.price, c.instructor.email, SIZE(c.chapters)) " +
            "FROM Course c")
    List<CourseSummary> findAllSummaries();
}
