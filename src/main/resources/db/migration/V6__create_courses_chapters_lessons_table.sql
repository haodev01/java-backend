CREATE TABLE courses (
     id BIGINT AUTO_INCREMENT PRIMARY KEY,
     title VARCHAR(255) NOT NULL,
     slug VARCHAR(255) NOT NULL UNIQUE,
     price DECIMAL(10,2) NOT NULL,
     instructor_id BIGINT NOT NULL,
     status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
     FOREIGN KEY (instructor_id) REFERENCES users(id)
);

CREATE TABLE chapters (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  chapter_order INT NOT NULL,   -- không đặt tên "order" — trùng từ khoá SQL (ORDER BY)
  course_id BIGINT NOT NULL,
  FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

CREATE TABLE lessons (
     id BIGINT AUTO_INCREMENT PRIMARY KEY,
     title VARCHAR(255) NOT NULL,
     lesson_order INT NOT NULL,
     content_type VARCHAR(50) NOT NULL,
     chapter_id BIGINT NOT NULL,
     FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE
);