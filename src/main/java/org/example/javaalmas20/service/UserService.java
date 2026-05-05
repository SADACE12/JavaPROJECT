package org.example.javaalmas20.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.javaalmas20.domain.entity.RoleName;
import org.example.javaalmas20.domain.entity.User;
import org.example.javaalmas20.dto.request.UpdateUserRequest;
import org.example.javaalmas20.dto.response.UserResponse;
import org.example.javaalmas20.exception.DuplicateResourceException;
import org.example.javaalmas20.exception.ResourceNotFoundException;
import org.example.javaalmas20.mapper.UserMapper;
import org.example.javaalmas20.repository.RoleRepository;
import org.example.javaalmas20.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User service — CRUD, role management, GDPR operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserResponse getById(UUID id) {
        return userMapper.toResponse(findUserById(id));
    }

    public UserResponse getByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return userMapper.toResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserResponse> getAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toResponse);
    }

    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        User user = findUserById(id);

        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Email already in use: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        if (StringUtils.hasText(request.getFirstName())) {
            user.setFirstName(request.getFirstName());
        }
        if (StringUtils.hasText(request.getLastName())) {
            user.setLastName(request.getLastName());
        }
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updated = userRepository.save(user);
        auditService.logAction(updated.getId(), updated.getUsername(), "USER_UPDATED",
                "User", updated.getId(), "Profile updated", null, null);
        return userMapper.toResponse(updated);
    }

    /**
     * Assign a role to a user (admin only).
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse assignRole(UUID userId, RoleName roleName) {
        User user = findUserById(userId);
        var role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));
        user.getRoles().add(role);
        User saved = userRepository.save(user);
        auditService.logAction(saved.getUsername(), "ROLE_ASSIGNED",
                "Role " + roleName + " assigned to user " + saved.getUsername());
        return userMapper.toResponse(saved);
    }

    /**
     * GDPR: Request account data deletion.
     */
    @Transactional
    public void requestGdprDeletion(UUID userId) {
        User user = findUserById(userId);
        user.setGdprDeleteRequested(true);
        user.setGdprDeleteRequestedAt(LocalDateTime.now());
        userRepository.save(user);
        auditService.logAction(user.getUsername(), "GDPR_DELETE_REQUESTED",
                "User requested account deletion under GDPR");
    }

    /**
     * GDPR: Anonymize user data (admin executes after review).
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void anonymizeUser(UUID userId) {
        User user = findUserById(userId);
        user.setUsername("deleted_" + user.getId().toString().substring(0, 8));
        user.setEmail("deleted_" + user.getId() + "@anonymized.local");
        user.setFirstName(null);
        user.setLastName(null);
        user.setPassword("ANONYMIZED");
        user.setEnabled(false);
        userRepository.save(user);
        auditService.logAction("SYSTEM", "GDPR_USER_ANONYMIZED",
                "User " + userId + " data anonymized under GDPR");
    }

    private User findUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }
}
