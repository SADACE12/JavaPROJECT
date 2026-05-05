package org.example.javaalmas20.dto.response;

import lombok.*;

/**
 * JWT authentication response DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponse {

    private String accessToken;
    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private UserResponse user;
}
