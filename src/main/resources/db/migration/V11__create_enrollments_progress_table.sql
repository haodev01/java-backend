CREATE TABLE enrollments (
     id BIGINT AUTO_INCREMENT PRIMARY KEY,
     user_id BIGINT NOT NULL,
     course_id BIGINT NOT NULL,
     enrolled_at TIMESTAMP NOT NULL,
     UNIQUE KEY uk_enrollment_user_course (user_id, course_id),
     FOREIGN KEY (user_id) REFERENCES users(id),
     FOREIGN KEY (course_id) REFERENCES courses(id)
);

CREATE TABLE progress (
      id BIGINT AUTO_INCREMENT PRIMARY KEY,
      enrollment_id BIGINT NOT NULL,
      lesson_id BIGINT NOT NULL,
      completed BOOLEAN NOT NULL DEFAULT FALSE,
      completed_at TIMESTAMP NULL,
      UNIQUE KEY uk_progress_enrollment_lesson (enrollment_id, lesson_id),
      FOREIGN KEY (enrollment_id) REFERENCES enrollments(id) ON DELETE CASCADE,
      FOREIGN KEY (lesson_id) REFERENCES lessons(id)
);