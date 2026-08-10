CREATE TABLE request_logs (
      id BIGINT AUTO_INCREMENT PRIMARY KEY,
      occurred_at TIMESTAMP NOT NULL,
      request_id VARCHAR(64),
      method VARCHAR(10) NOT NULL,
      path VARCHAR(255) NOT NULL,
      status INT,
      latency_ms BIGINT,
      ip VARCHAR(64),
      user_email VARCHAR(255),
      user_agent VARCHAR(500),
      request_headers TEXT,   -- JSON dạng chuỗi, đã redact sẵn trước khi lưu
      request_body TEXT,      -- JSON dạng chuỗi, đã redact + cắt bớt nếu quá dài
      response_body TEXT,
      INDEX idx_request_logs_occurred_at (occurred_at),  -- CMS sort theo thời gian trước tiên
      INDEX idx_request_logs_status (status),             -- lọc nhanh "chỉ xem lỗi" (status >= 400)
      INDEX idx_request_logs_user_email (user_email)
);