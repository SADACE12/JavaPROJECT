package org.example.javaalmas20.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.javaalmas20.domain.entity.RoleName;
import org.example.javaalmas20.dto.response.AuditLogResponse;
import org.example.javaalmas20.dto.response.UserResponse;
import org.example.javaalmas20.service.AuditService;
import org.example.javaalmas20.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Admin controller — role management, audit logs, GDPR anonymization.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Administration API")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final UserService userService;
    private final AuditService auditService;

    @PostMapping("/users/{userId}/roles/{roleName}")
    @Operation(summary = "Assign a role to a user")
    public ResponseEntity<UserResponse> assignRole(@PathVariable UUID userId,
                                                   @PathVariable RoleName roleName) {
        return ResponseEntity.ok(userService.assignRole(userId, roleName));
    }

    @PostMapping("/users/{userId}/anonymize")
    @Operation(summary = "Anonymize user data (GDPR)")
    public ResponseEntity<Void> anonymizeUser(@PathVariable UUID userId) {
        userService.anonymizeUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/audit")
    @Operation(summary = "Get all audit logs")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogs(Pageable pageable) {
        return ResponseEntity.ok(auditService.getAll(pageable));
    }

    @GetMapping("/audit/user/{userId}")
    @Operation(summary = "Get audit logs for a specific user")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogsByUser(@PathVariable UUID userId,
                                                                     Pageable pageable) {
        return ResponseEntity.ok(auditService.getByUser(userId, pageable));
    }

    @GetMapping("/audit/action/{action}")
    @Operation(summary = "Get audit logs by action")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogsByAction(@PathVariable String action,
                                                                       Pageable pageable) {
        return ResponseEntity.ok(auditService.getByAction(action, pageable));
    }
}
