package org.example.javaalmas20.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Audit log response DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {

    private UUID id;
    private UUID userId;
    private String username;
    private String action;
    private String entityType;
    private UUID entityId;
    private String details;
    private String ipAddress;
    private LocalDateTime createdAt;
}
