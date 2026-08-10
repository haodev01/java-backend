package com.elearning.repository;

import com.elearning.model.RequestLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface RequestLogRepository extends JpaRepository<RequestLog, Long> {

    // SELECT full entity — bao gồm cả requestHeaders/requestBody/responseBody
    // (3 cột LONGTEXT). Đổi lại: mỗi dòng trả về sẽ nặng hơn nhiều so với bản
    // DTO tóm tắt, nên vẫn NÊN lọc chặt (from/to, status, userEmail...) trước
    // khi phân trang để không kéo về hàng trăm dòng full body cùng lúc.
    @Query(value = "SELECT r FROM RequestLog r " +
            "WHERE (:from IS NULL OR r.occurredAt >= :from) " +
            "AND (:to IS NULL OR r.occurredAt <= :to) " +
            "AND (:onlyErrors = false OR r.status >= 400) " +
            "AND (:status IS NULL OR r.status = :status) " +
            "AND (:userEmail IS NULL OR r.userEmail = :userEmail) " +
            "AND (:method IS NULL OR r.method = :method) " +
            "AND (:path IS NULL OR r.path LIKE CONCAT('%', :path, '%'))",
            countQuery = "SELECT COUNT(r) FROM RequestLog r " +
            "WHERE (:from IS NULL OR r.occurredAt >= :from) " +
            "AND (:to IS NULL OR r.occurredAt <= :to) " +
            "AND (:onlyErrors = false OR r.status >= 400) " +
            "AND (:status IS NULL OR r.status = :status) " +
            "AND (:userEmail IS NULL OR r.userEmail = :userEmail) " +
            "AND (:method IS NULL OR r.method = :method) " +
            "AND (:path IS NULL OR r.path LIKE CONCAT('%', :path, '%'))")
    Page<RequestLog> findAllFiltered(@Param("from") Instant from, @Param("to") Instant to,
                                     @Param("onlyErrors") boolean onlyErrors, @Param("status") Integer status,
                                     @Param("userEmail") String userEmail, @Param("method") String method,
                                     @Param("path") String path, Pageable pageable);
}
