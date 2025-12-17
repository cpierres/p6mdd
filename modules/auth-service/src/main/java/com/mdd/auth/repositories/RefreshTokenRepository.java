package com.mdd.auth.repositories;

import com.mdd.auth.entities.RefreshToken;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RefreshTokenRepository extends ReactiveCrudRepository<RefreshToken, UUID> {
    Mono<RefreshToken> findByToken(String token);

    Mono<Long> deleteByUserId(UUID userId);
}
