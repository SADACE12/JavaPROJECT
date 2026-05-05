package org.example.javaalmas20.service;

import org.example.javaalmas20.domain.entity.Role;
import org.example.javaalmas20.domain.entity.RoleName;
import org.example.javaalmas20.domain.entity.User;
import org.example.javaalmas20.dto.request.UpdateUserRequest;
import org.example.javaalmas20.dto.response.UserResponse;
import org.example.javaalmas20.exception.DuplicateResourceException;
import org.example.javaalmas20.exception.ResourceNotFoundException;
import org.example.javaalmas20.mapper.UserMapper;
import org.example.javaalmas20.repository.RoleRepository;
import org.example.javaalmas20.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuditService auditService;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserResponse userResponse;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .roles(new HashSet<>())
                .build();

        userResponse = UserResponse.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .roles(Set.of("ROLE_USER"))
                .build();
    }

    @Test
    @DisplayName("getById() — should return user when exists")
    void getById_success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.getById(userId);

        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("getById() — should throw ResourceNotFoundException when not exists")
    void getById_notFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("update() — should update user profile successfully")
    void update_success() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .firstName("Updated")
                .lastName("Name")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.update(userId, request);

        assertThat(result).isNotNull();
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("update() — should throw DuplicateResourceException for taken email")
    void update_duplicateEmail() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .email("taken@example.com")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.update(userId, request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("assignRole() — should add role to user")
    void assignRole_success() {
        Role adminRole = Role.builder().id(UUID.randomUUID()).name(RoleName.ROLE_ADMIN).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(RoleName.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.assignRole(userId, RoleName.ROLE_ADMIN);

        assertThat(result).isNotNull();
        verify(auditService).logAction(anyString(), eq("ROLE_ASSIGNED"), anyString());
    }

    @Test
    @DisplayName("requestGdprDeletion() — should flag user for GDPR deletion")
    void requestGdprDeletion_success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.requestGdprDeletion(userId);

        assertThat(user.isGdprDeleteRequested()).isTrue();
        assertThat(user.getGdprDeleteRequestedAt()).isNotNull();
    }
}
