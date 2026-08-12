CREATE TABLE payments (
      id BIGINT AUTO_INCREMENT PRIMARY KEY,
      order_id BIGINT NOT NULL,
      provider VARCHAR(20) NOT NULL,
      txn_ref VARCHAR(100) NOT NULL UNIQUE,
      provider_txn_id VARCHAR(100),
      status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
      amount DECIMAL(12,2) NOT NULL,
      paid_at TIMESTAMP NULL,
      FOREIGN KEY (order_id) REFERENCES orders(id)
);