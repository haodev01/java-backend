CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    occurred_at TIMESTAMP NOT NULL,
    request_id VARCHAR(64),       -- nối sang đúng dòng access log tương ứng khi cần
    actor_email VARCHAR(255),
    action VARCHAR(50) NOT NULL,
    target_email VARCHAR(255),    -- dùng khi ADMIN tác động lên user khác (Phase 2+)
    ip VARCHAR(64),
    metadata VARCHAR(500),
    INDEX idx_audit_actor (actor_email),  -- truy vấn thường gặp: "lịch sử của user X"
    INDEX idx_audit_action (action)       -- hoặc "mọi lần LOGIN_FAILED gần đây"
);