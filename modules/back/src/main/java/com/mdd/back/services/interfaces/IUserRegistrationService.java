package com.mdd.back.services.interfaces;

import com.mdd.back.entities.User;
import com.mdd.back.models.RegisterRequest;
import reactor.core.publisher.Mono;

/**
 * Service responsable de l'enregistrement des utilisateurs.
 */
public interface IUserRegistrationService {
    /**
     * Enregistre un nouvel utilisateur.
     * @param request Les informations d'enregistrement de l'utilisateur
     * @return Un Mono contenant l'utilisateur enregistré, ou une erreur si l'enregistrement échoue
     */
    Mono<User> registerNewUser(RegisterRequest request);
}
