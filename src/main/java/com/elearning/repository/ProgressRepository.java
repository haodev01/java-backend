package com.elearning.repository;

import com.elearning.model.Enrollment;
import com.elearning.model.Lesson;
import com.elearning.model.Progress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProgressRepository extends JpaRepository<Progress, Long> {
    Optional<Progress> findByEnrollmentAndLesson(Enrollment enrollment, Lesson lesson);
    long countByEnrollmentAndCompletedTrue(Enrollment enrollment);
}