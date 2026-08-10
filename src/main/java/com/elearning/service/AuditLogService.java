package com.elearning.service;


import com.elearning.model.AuditLog;
import com.elearning.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void record(String requestId, String actorEmail, String action, String targetEmail,
                       String ip, String metadata) {
        auditLogRepository.save(new AuditLog(requestId, actorEmail, action, targetEmail, ip, metadata));
    }
}
