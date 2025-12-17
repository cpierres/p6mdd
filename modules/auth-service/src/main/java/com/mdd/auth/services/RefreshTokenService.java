package com.mdd.auth.services;

import com.mdd.auth.entities.RefreshToken;
import com.mdd.auth.repositories.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTokenExpirationSeconds;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${app.jwt.refresh-token-expiration:604800}") long refreshTokenExpirationSeconds
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
    }

    public Mono<String> createRefreshToken(UUID userId) {
        String token = UUID.randomUUID() + "-" + UUID.randomUUID();
        Instant now = Instant.now();
        RefreshToken entity = new RefreshToken(
                null,
                userId,
                token,
                now.plusSeconds(refreshTokenExpirationSeconds),
                false,
                now
        );
        return refreshTokenRepository.save(entity).map(RefreshToken::getToken);
    }

    /**
     * Valide le refresh token et retourne l'userId si OK.
     */
    public Mono<UUID> validateRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(rt -> rt.getExpiresAt() != null && rt.getExpiresAt().isAfter(Instant.now()))
                .map(RefreshToken::getUserId);
    }

    public Mono<Void> deleteByUserId(UUID userId) {
        return refreshTokenRepository.deleteByUserId(userId).then();
    }
}
