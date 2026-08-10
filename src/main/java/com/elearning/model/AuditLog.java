package com.elearning.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "request_id")
    private String requestId;

    @Column(name = "actor_email")
    private String actorEmail;

    @Column(nullable = false)
    private String action;

    @Column(name = "target_email")
    private String targetEmail;

    private String ip;
    private String metadata;

    protected AuditLog() {}

    public AuditLog(String requestId, String actorEmail, String action, String targetEmail,
                    String ip, String metadata) {
        this.occurredAt = Instant.now();
        this.requestId = requestId;
        this.actorEmail = actorEmail;
        this.action = action;
        this.targetEmail = targetEmail;
        this.ip = ip;
        this.metadata = metadata;
    }

    public Long getId() { return id; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getRequestId() { return requestId; }
    public String getActorEmail() { return actorEmail; }
    public String getAction() { return action; }
    public String getTargetEmail() { return targetEmail; }
    public String getIp() { return ip; }
    public String getMetadata() { return metadata; }
}