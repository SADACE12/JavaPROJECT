package org.example.javaalmas20.mapper;

import org.example.javaalmas20.domain.entity.Role;
import org.example.javaalmas20.domain.entity.User;
import org.example.javaalmas20.dto.request.RegisterRequest;
import org.example.javaalmas20.dto.response.UserResponse;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * MapStruct mapper: User ↔ DTO.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "accountNonLocked", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "gdprDeleteRequested", constant = "false")
    @Mapping(target = "gdprDeleteRequestedAt", ignore = true)
    User toEntity(RegisterRequest request);

    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    UserResponse toResponse(User user);

    default Set<String> mapRoles(Set<Role> roles) {
        if (roles == null) return Set.of();
        return roles.stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());
    }
}
