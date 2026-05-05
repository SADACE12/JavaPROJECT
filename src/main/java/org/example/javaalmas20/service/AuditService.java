package org.example.javaalmas20.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.javaalmas20.domain.entity.AuditLog;
import org.example.javaalmas20.dto.response.AuditLogResponse;
import org.example.javaalmas20.mapper.AuditLogMapper;
import org.example.javaalmas20.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Audit service — logs all important user/system actions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    /**
     * Record an audit event asynchronously.
     */
    @Async
    public void logAction(UUID userId, String username, String action,
                          String entityType, UUID entityId, String details,
                          String ipAddress, String userAgent) {
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .username(username)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
        auditLogRepository.save(auditLog);
        log.info("AUDIT: user={}, action={}, entity={}:{}", username, action, entityType, entityId);
    }

    /**
     * Simplified overload for quick logging.
     */
    @Async
    public void logAction(String username, String action, String details) {
        logAction(null, username, action, null, null, details, null, null);
    }

    public Page<AuditLogResponse> getByUser(UUID userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable)
                .map(auditLogMapper::toResponse);
    }

    public Page<AuditLogResponse> getByAction(String action, Pageable pageable) {
        return auditLogRepository.findByAction(action, pageable)
                .map(auditLogMapper::toResponse);
    }

    public Page<AuditLogResponse> getAll(Pageable pageable) {
        return auditLogRepository.findAll(pageable)
                .map(auditLogMapper::toResponse);
    }
}
