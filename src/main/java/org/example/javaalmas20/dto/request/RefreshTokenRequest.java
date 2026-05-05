package org.example.javaalmas20.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Token refresh request DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
