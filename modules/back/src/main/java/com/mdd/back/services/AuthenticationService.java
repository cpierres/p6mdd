package com.mdd.back.services;

import com.mdd.back.entities.User;
import com.mdd.back.exception.ResourceNotFoundException;
import com.mdd.back.repositories.UserRepository;
import com.mdd.back.services.interfaces.IAuthenticationService;
import com.mdd.back.models.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Implémentation du service d'authentification.
 */
@Service
public class AuthenticationService implements IAuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<UUID> login(LoginRequest loginRequest) {
        String identifier = loginRequest.getIdentifier(); // email ou username
        String password = loginRequest.getPassword();

        // Déterminer si l'identifiant est un email
        boolean isEmail = identifier.contains("@");

        // Recherche utilisateur par email ou username
        return (isEmail ? userRepository.findByEmail(identifier) : userRepository.findByUsername(identifier))
                .switchIfEmpty(Mono.empty()) // Aucun utilisateur trouvé
                .flatMap(user -> {
                    // Vérifier que le mot de passe concorde
                    if (passwordEncoder.matches(password, user.getPassword())) {
                        return Mono.just(user.getId());
                    } else {
                        return Mono.empty(); // Mot de passe incorrect
                    }
                });
    }

    @Override
    public Mono<User> getAuthenticatedUser() {
        // Obtenir l'objet Authentication du Security Context
        return Mono.deferContextual(contextView ->
                ReactiveSecurityContextHolder.getContext() // Récupérer le SecurityContext en réactif
                        .map(SecurityContext::getAuthentication)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Authentication context vide")))
                        .flatMap(auth -> {
                            if (auth == null || !auth.isAuthenticated()) {
                                return Mono.error(new ResourceNotFoundException("Utilisateur non authentifié"));
                            }
                            //on peut s'authentifier soit par username, soit par email
                            String identifier = auth.getName();
                            boolean isEmail = identifier.contains("@");
                            return (isEmail ? userRepository.findByEmail(identifier) : userRepository.findByUsername(identifier))
                                    .switchIfEmpty(Mono.error(new ResourceNotFoundException("Utilisateur non trouvé!")));
                        })
        );
    }

    @Override
    public Mono<UUID> getAuthenticatedUserId() {
        return getAuthenticatedUser() // Utilise la méthode existante
                .map(User::getId) // Extrait l'UUID de l'utilisateur
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("ID utilisateur non trouvé!")));
    }
}
