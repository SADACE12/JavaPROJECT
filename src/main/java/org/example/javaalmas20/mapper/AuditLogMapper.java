package org.example.javaalmas20.mapper;

import org.example.javaalmas20.domain.entity.AuditLog;
import org.example.javaalmas20.dto.response.AuditLogResponse;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper: AuditLog ↔ DTO.
 */
@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    AuditLogResponse toResponse(AuditLog auditLog);
}
