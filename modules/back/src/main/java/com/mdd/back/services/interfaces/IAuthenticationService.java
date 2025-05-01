package com.mdd.back.services.interfaces;

import com.mdd.back.entities.User;
import com.mdd.back.models.LoginRequest;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service responsable de l'authentification des utilisateurs.
 */
public interface IAuthenticationService {
    /**
     * Authentifie un utilisateur en fonction de son e-mail ou nom d'utilisateur et de son mot de passe.
     * @param loginRequest La demande de connexion contenant l'identifiant et le mot de passe
     * @return L'ID de l'utilisateur s'il est authentifié avec succès, ou un Mono vide si l'authentification échoue
     */
    Mono<UUID> login(LoginRequest loginRequest);

    /**
     * Récupère l'utilisateur actuellement authentifié.
     * @return Un Mono contenant l'utilisateur authentifié, ou une erreur si aucun utilisateur n'est authentifié
     */
    Mono<User> getAuthenticatedUser();

    /**
     * Récupère l'ID de l'utilisateur actuellement authentifié.
     * @return Un Mono contenant l'ID de l'utilisateur authentifié, ou une erreur si aucun utilisateur n'est authentifié
     */
    Mono<UUID> getAuthenticatedUserId();
}
