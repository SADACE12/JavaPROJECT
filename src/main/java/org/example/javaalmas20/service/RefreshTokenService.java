package org.example.javaalmas20.service;

import lombok.RequiredArgsConstructor;
import org.example.javaalmas20.domain.entity.RefreshToken;
import org.example.javaalmas20.domain.entity.User;
import org.example.javaalmas20.exception.TokenException;
import org.example.javaalmas20.repository.RefreshTokenRepository;
import org.example.javaalmas20.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Refresh token service — creation, validation, rotation & revocation.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Transactional
    public RefreshToken createRefreshToken(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TokenException("User not found for refresh token"));

        // Revoke all existing refresh tokens for this user (rotation)
        refreshTokenRepository.revokeAllByUser(user);

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000))
                .build();

        return refreshTokenRepository.save(token);
    }

    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenException("Refresh token not found"));

        if (refreshToken.isRevoked()) {
            throw new TokenException("Refresh token has been revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenException("Refresh token expired. Please log in again");
        }

        return refreshToken;
    }

    @Transactional
    public void revokeAllUserTokens(User user) {
        refreshTokenRepository.revokeAllByUser(user);
    }
}
