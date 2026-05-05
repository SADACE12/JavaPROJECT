package org.example.javaalmas20.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Login request DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
