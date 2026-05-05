package org.example.javaalmas20.mapper;

import org.example.javaalmas20.domain.entity.AuditLog;
import org.example.javaalmas20.dto.response.AuditLogResponse;
import org.springframework.stereotype.Component;

/**
 * Manual mapper: AuditLog ↔ DTO.
 */
@Component
public class AuditLogMapper {

    public AuditLogResponse toResponse(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .userId(auditLog.getUserId())
                .username(auditLog.getUsername())
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .details(auditLog.getDetails())
                .ipAddress(auditLog.getIpAddress())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }
}
