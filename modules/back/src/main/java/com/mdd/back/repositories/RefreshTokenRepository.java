package com.mdd.back.repositories;

import com.mdd.back.entities.RefreshToken;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repository pour gérer les opérations de base de données liées aux refresh tokens.
 * Utilise R2dbcRepository pour les opérations réactives.
 */
public interface RefreshTokenRepository extends R2dbcRepository<RefreshToken, UUID> {
    /**
     * Trouve un refresh token par sa valeur
     * @param token La valeur du token à rechercher
     * @return Un Mono contenant le refresh token s'il existe
     */
    Mono<RefreshToken> findByToken(String token);
    
    /**
     * Supprime tous les refresh tokens associés à un utilisateur
     * @param userId L'ID de l'utilisateur
     * @return Un Mono<Void> indiquant la fin de l'opération
     */
    Mono<Void> deleteByUserId(UUID userId);
}