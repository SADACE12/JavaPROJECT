package org.example.javaalmas20.mapper;

import org.example.javaalmas20.domain.entity.Role;
import org.example.javaalmas20.domain.entity.User;
import org.example.javaalmas20.dto.request.RegisterRequest;
import org.example.javaalmas20.dto.response.UserResponse;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manual mapper: User ↔ DTO (replaces MapStruct).
 */
@Component
public class UserMapper {

    public User toEntity(RegisterRequest request) {
        return User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .enabled(true)
                .accountNonLocked(true)
                .gdprDeleteRequested(false)
                .build();
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .enabled(user.isEnabled())
                .roles(mapRoles(user.getRoles()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private Set<String> mapRoles(Set<Role> roles) {
        if (roles == null) return Set.of();
        return roles.stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());
    }
}
