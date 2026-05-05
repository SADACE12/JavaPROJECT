package org.example.javaalmas20.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.javaalmas20.dto.request.LoginRequest;
import org.example.javaalmas20.dto.request.RegisterRequest;
import org.example.javaalmas20.dto.response.JwtResponse;
import org.example.javaalmas20.dto.response.UserResponse;
import org.example.javaalmas20.security.JwtTokenProvider;
import org.example.javaalmas20.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/auth/register — should register user and return 201")
    void register_success() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .email("new@example.com")
                .password("Password123!")
                .firstName("New")
                .lastName("User")
                .build();

        UserResponse response = UserResponse.builder()
                .id(UUID.randomUUID())
                .username("newuser")
                .email("new@example.com")
                .roles(Set.of("ROLE_USER"))
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("new@example.com"));
    }

    @Test
    @DisplayName("POST /api/auth/register — should return 400 for invalid input")
    void register_validation_failure() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("")   // blank — should fail validation
                .email("invalid-email")
                .password("short")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    @Test
    @DisplayName("POST /api/auth/login — should return JWT tokens")
    void login_success() throws Exception {
        LoginRequest request = new LoginRequest("testuser", "Password123!");

        JwtResponse jwtResponse = JwtResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .user(UserResponse.builder()
                        .id(UUID.randomUUID())
                        .username("testuser")
                        .build())
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }
}
