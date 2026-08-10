package com.elearning.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "request_logs")
public class RequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "request_id")
    private String requestId;

    @Column(nullable = false)
    private String method;

    @Column(nullable = false)
    private String path;

    private Integer status;

    @Column(name = "latency_ms")
    private Long latencyMs;

    private String ip;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "user_agent")
    private String userAgent;

    // Không dùng @Lob — nó để Hibernate tự suy luận kiểu cột theo dialect, dễ ra
    // kết quả bất ngờ (ở đây là TINYTEXT, chỉ 255 byte). columnDefinition khai rõ
    // TEXT (65KB) khớp đúng với migration Flyway, không phụ thuộc suy luận ngầm.
    @Column(name = "request_headers", columnDefinition = "LONGTEXT")
    private String requestHeaders;

    @Column(name = "request_body", columnDefinition = "LONGTEXT")
    private String requestBody;

    @Column(name = "response_body", columnDefinition = "LONGTEXT")
    private String responseBody;

    protected RequestLog() {}

    public RequestLog(String requestId, String method, String path, Integer status, Long latencyMs,
                      String ip, String userEmail, String userAgent,
                      String requestHeaders, String requestBody, String responseBody) {
        this.occurredAt = Instant.now();
        this.requestId = requestId;
        this.method = method;
        this.path = path;
        this.status = status;
        this.latencyMs = latencyMs;
        this.ip = ip;
        this.userEmail = userEmail;
        this.userAgent = userAgent;
        this.requestHeaders = requestHeaders;
        this.requestBody = requestBody;
        this.responseBody = responseBody;
    }

    public Long getId() { return id; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getRequestId() { return requestId; }
    public String getMethod() { return method; }
    public String getPath() { return path; }
    public Integer getStatus() { return status; }
    public Long getLatencyMs() { return latencyMs; }
    public String getIp() { return ip; }
    public String getUserEmail() { return userEmail; }
    public String getUserAgent() { return userAgent; }
    public String getRequestHeaders() { return requestHeaders; }
    public String getRequestBody() { return requestBody; }
    public String getResponseBody() { return responseBody; }
}