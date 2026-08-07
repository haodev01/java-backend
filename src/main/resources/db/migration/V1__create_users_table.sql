-- Tên file V1__... theo đúng quy ước Flyway: version + 2 dấu gạch dưới + mô tả.
-- Flyway chạy các file này theo thứ tự version, chỉ một lần, và ghi lại lịch sử
-- vào bảng flyway_schema_history mà nó tự tạo.
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,  -- ràng buộc UNIQUE ở tầng DB, không chỉ tin code Java
                       password VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);