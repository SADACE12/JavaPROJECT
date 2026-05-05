package org.example.javaalmas20.service;

import org.example.javaalmas20.domain.entity.Role;
import org.example.javaalmas20.domain.entity.RoleName;
import org.example.javaalmas20.domain.entity.User;
import org.example.javaalmas20.dto.request.RegisterRequest;
import org.example.javaalmas20.dto.response.JwtResponse;
import org.example.javaalmas20.dto.response.UserResponse;
import org.example.javaalmas20.dto.request.LoginRequest;
import org.example.javaalmas20.exception.DuplicateResourceException;
import org.example.javaalmas20.mapper.UserMapper;
import org.example.javaalmas20.repository.RoleRepository;
import org.example.javaalmas20.repository.UserRepository;
import org.example.javaalmas20.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private AuditService auditService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private Role userRole;
    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("Password123!")
                .firstName("Test")
                .lastName("User")
                .build();

        userRole = Role.builder()
                .id(UUID.randomUUID())
                .name(RoleName.ROLE_USER)
                .build();

        user = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .roles(Set.of(userRole))
                .build();

        userResponse = UserResponse.builder()
                .id(user.getId())
                .username("testuser")
                .email("test@example.com")
                .roles(Set.of("ROLE_USER"))
                .build();
    }

    @Test
    @DisplayName("register() — should register a new user successfully")
    void register_success() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userMapper.toEntity(registerRequest)).thenReturn(user);
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleName.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = authService.register(registerRequest);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).save(any(User.class));
        verify(auditService).logAction(anyString(), eq("USER_REGISTERED"), anyString());
    }

    @Test
    @DisplayName("register() — should throw DuplicateResourceException for existing username")
    void register_duplicateUsername() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register() — should throw DuplicateResourceException for existing email")
    void register_duplicateEmail() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    @DisplayName("login() — should return JWT tokens on valid credentials")
    void login_success() {
        LoginRequest loginRequest = new LoginRequest("testuser", "Password123!");
        Authentication auth = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(jwtTokenProvider.generateToken(auth)).thenReturn("access-token");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(refreshTokenService.createRefreshToken(user.getId()))
                .thenReturn(org.example.javaalmas20.domain.entity.RefreshToken.builder()
                        .token("refresh-token").build());
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        JwtResponse result = authService.login(loginRequest);

        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(result.getUser().getUsername()).isEqualTo("testuser");
    }
}
