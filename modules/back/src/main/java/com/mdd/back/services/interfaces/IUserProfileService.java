package com.mdd.back.services.interfaces;

import com.mdd.back.models.UpdateAuthenticatedUserRequest;
import com.mdd.back.models.UserDto;
import reactor.core.publisher.Mono;

/**
 * Service responsable de la gestion des profils utilisateurs.
 */
public interface IUserProfileService {
    /**
     * Met à jour les informations de l'utilisateur authentifié.
     * @param request Les nouvelles informations de l'utilisateur
     * @return Un Mono contenant le DTO de l'utilisateur mis à jour, ou une erreur si la mise à jour échoue
     */
    Mono<UserDto> updateAuthenticatedUser(UpdateAuthenticatedUserRequest request);
}

