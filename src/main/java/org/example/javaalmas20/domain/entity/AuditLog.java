package org.example.javaalmas20.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Audit log entity — records who did what and when (GDPR / compliance).
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_user", columnList = "userId"),
        @Index(name = "idx_audit_action", columnList = "action"),
        @Index(name = "idx_audit_created", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** ID of the user who performed the action (nullable for system actions). */
    private UUID userId;

    /** Username snapshot at time of action. */
    @Column(length = 50)
    private String username;

    /** Action performed (e.g., "USER_LOGIN", "USER_CREATED", "ROLE_ASSIGNED"). */
    @Column(nullable = false, length = 100)
    private String action;

    /** Target entity type (e.g., "User", "Role"). */
    @Column(length = 100)
    private String entityType;

    /** Target entity ID. */
    private UUID entityId;

    /** JSON details / diff of changes. */
    @Column(columnDefinition = "TEXT")
    private String details;

    /** IP address of the request. */
    @Column(length = 45)
    private String ipAddress;

    /** User-Agent header. */
    @Column(length = 512)
    private String userAgent;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
