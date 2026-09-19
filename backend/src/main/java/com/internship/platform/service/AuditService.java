package com.internship.platform.service;

import com.internship.platform.model.entity.AuditLog;
import com.internship.platform.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

/** Persists audit records; never throws. */
@Service
public class AuditService {

    private final AuditLogRepository repo;

    public AuditService(AuditLogRepository repo) {
        this.repo = repo;
    }

    /** Record an event (best-effort). */
    public void record(Long userId, String action, String entityType, Long entityId) {
        try {
            AuditLog log = new AuditLog();
            log.setUserId(userId);
            log.setAction(action);
            log.setEntityType(entityType);
            log.setEntityId(entityId);
            repo.save(log);
        } catch (Exception ignored) {
            // audit must not break business flows
        }
    }
}
