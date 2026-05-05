package org.example.javaalmas20.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.javaalmas20.domain.entity.RefreshToken;
import org.example.javaalmas20.domain.entity.Role;
import org.example.javaalmas20.domain.entity.RoleName;
import org.example.javaalmas20.domain.entity.User;
import org.example.javaalmas20.dto.request.LoginRequest;
import org.example.javaalmas20.dto.request.RefreshTokenRequest;
import org.example.javaalmas20.dto.request.RegisterRequest;
import org.example.javaalmas20.dto.response.JwtResponse;
import org.example.javaalmas20.dto.response.UserResponse;
import org.example.javaalmas20.exception.DuplicateResourceException;
import org.example.javaalmas20.exception.ResourceNotFoundException;
import org.example.javaalmas20.mapper.UserMapper;
import org.example.javaalmas20.repository.RoleRepository;
import org.example.javaalmas20.repository.UserRepository;
import org.example.javaalmas20.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Authentication service — register, login, refresh token.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final AuditService auditService;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        // Check for duplicates
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        // Map DTO → Entity
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Assign default role
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", RoleName.ROLE_USER));
        user.setRoles(Set.of(userRole));

        User saved = userRepository.save(user);

        auditService.logAction(saved.getUsername(), "USER_REGISTERED",
                "New user registered: " + saved.getUsername());

        return userMapper.toResponse(saved);
    }

    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        String accessToken = jwtTokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", request.getUsername()));

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        auditService.logAction(user.getId(), user.getUsername(), "USER_LOGIN",
                "User", user.getId(), "Login successful", null, null);

        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .user(userMapper.toResponse(user))
                .build();
    }

    public JwtResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.getRefreshToken());
        User user = refreshToken.getUser();

        String newAccessToken = jwtTokenProvider.generateToken(user.getUsername());
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

        return JwtResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .user(userMapper.toResponse(user))
                .build();
    }

    @Transactional
    public void logout(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        refreshTokenService.revokeAllUserTokens(user);
        auditService.logAction(user.getUsername(), "USER_LOGOUT", "User logged out");
    }
}
