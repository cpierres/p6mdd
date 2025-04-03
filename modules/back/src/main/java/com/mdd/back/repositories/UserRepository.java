package com.mdd.back.repositories;

import com.mdd.back.entities.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, UUID> {
    /**
     * Recherche un utilisateur par son email.
     * @param email l'email de l'utilisateur
     * @return un Mono<User> contenant l'utilisateur si trouvé, sinon Mono.empty()
     */
    Mono<User> findByEmail(String email);
}