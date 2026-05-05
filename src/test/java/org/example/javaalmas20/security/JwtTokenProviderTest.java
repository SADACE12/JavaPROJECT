package org.example.javaalmas20.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for JwtTokenProvider.
 */
class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private static final String SECRET = "dGVzdC1zZWNyZXQta2V5LWZvci1qYXZhYWxtYXMyMC11bml0LXRlc3RzLWhlcmUhIQ==";
    private static final long EXPIRATION_MS = 3600000;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(SECRET, EXPIRATION_MS);
    }

    @Test
    @DisplayName("generateToken() — should create a valid JWT")
    void generateToken_success() {
        String token = tokenProvider.generateToken("testuser");

        assertThat(token).isNotNull().isNotBlank();
        assertThat(tokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("getUsernameFromToken() — should extract correct username")
    void getUsernameFromToken_success() {
        String token = tokenProvider.generateToken("testuser");

        String username = tokenProvider.getUsernameFromToken(token);

        assertThat(username).isEqualTo("testuser");
    }

    @Test
    @DisplayName("validateToken() — should return false for invalid token")
    void validateToken_invalidToken() {
        assertThat(tokenProvider.validateToken("invalid.token.here")).isFalse();
    }

    @Test
    @DisplayName("validateToken() — should return false for expired token")
    void validateToken_expiredToken() {
        // Create a token that is already expired
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
        String expiredToken = Jwts.builder()
                .subject("testuser")
                .issuedAt(new Date(System.currentTimeMillis() - 7200000))
                .expiration(new Date(System.currentTimeMillis() - 3600000))
                .signWith(key)
                .compact();

        assertThat(tokenProvider.validateToken(expiredToken)).isFalse();
    }
}
