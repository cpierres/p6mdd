package com.mdd.back.services;

import com.mdd.back.entities.RefreshToken;
import com.mdd.back.repositories.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Service pour gérer les opérations liées aux refresh tokens.
 */
@Service
public class RefreshTokenService {

    @Value("${jwt.refresh-token-expiration:604800}") // 7 jours par défaut
    private Long refreshTokenDurationSeconds;

    private final RefreshTokenRepository refreshTokenRepository;

    @Autowired
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Crée un nouveau refresh token pour un utilisateur.
     * 
     * @param userId L'ID de l'utilisateur
     * @return Un Mono contenant la valeur du token créé
     */
    public Mono<String> createRefreshToken(UUID userId) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plus(Duration.ofSeconds(refreshTokenDurationSeconds)));

        return refreshTokenRepository.save(refreshToken)
                .map(RefreshToken::getToken);
    }

    /**
     * Valide un refresh token et retourne l'ID de l'utilisateur associé si valide.
     * 
     * @param token La valeur du token à valider
     * @return Un Mono contenant l'ID de l'utilisateur si le token est valide
     */
    public Mono<UUID> validateRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(refreshToken -> refreshToken.getExpiryDate().isAfter(Instant.now()))
                .map(RefreshToken::getUserId);
    }

    /**
     * Supprime tous les refresh tokens d'un utilisateur.
     * 
     * @param userId L'ID de l'utilisateur
     * @return Un Mono<Void> indiquant la fin de l'opération
     */
    public Mono<Void> deleteByUserId(UUID userId) {
        return refreshTokenRepository.deleteByUserId(userId);
    }
}