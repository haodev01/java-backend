package com.elearning.repository;

import com.elearning.model.Course;
import com.elearning.model.Enrollment;
import com.elearning.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByUserAndCourse(User user, Course course);
    Optional<Enrollment> findByUserAndCourse(User user, Course course);
    List<Enrollment> findByUser(User user);
}