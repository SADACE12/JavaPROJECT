package org.example.javaalmas20.controller;

import org.example.javaalmas20.dto.response.UserResponse;
import org.example.javaalmas20.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private UserService userService;

    @Test
    @DisplayName("Unauthenticated → 401")
    void unauthenticated() throws Exception {
        mockMvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("Authenticated user → 200")
    void authenticated() throws Exception {
        when(userService.getByUsername("testuser")).thenReturn(
                UserResponse.builder().id(UUID.randomUUID()).username("testuser").roles(Set.of("ROLE_USER")).build());
        mockMvc.perform(get("/api/users/me")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("Non-admin → 403 on /api/admin")
    void adminForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/audit")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Admin → 200 on /api/admin")
    void adminAllowed() throws Exception {
        mockMvc.perform(get("/api/admin/audit")).andExpect(status().isOk());
    }
}
