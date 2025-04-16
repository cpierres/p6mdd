package com.mdd.back.services;

import com.mdd.back.entities.User;
import com.mdd.back.exception.ResourceNotFoundException;
import com.mdd.back.mappers.UserMapper;
import com.mdd.back.models.LoginRequest;
import com.mdd.back.models.RegisterRequest;
import com.mdd.back.models.UpdateAuthenticatedUserRequest;
import com.mdd.back.models.UserDto;
import com.mdd.back.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserValidator userValidator;


    @Autowired
    public AuthService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder, UserValidator userValidator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.userValidator = userValidator;
    }

    /**
     * Enregistre un nouvel utilisateur après vérifications et encodage du mot de passe.
     *
     * @param request L'objet contenant les informations de l'utilisateur à enregistrer,
     *                telles que l'email, le username et le mot de passe.
     * @return Un objet Mono contenant l'utilisateur enregistré si l'opération réussit,
     * ou une erreur si un utilisateur avec l'email spécifié existe déjà.
     */
    public Mono<User> registerNewUser(RegisterRequest request) {
        return userValidator.validateUniqueEmailAndUsername(null, request.getEmail(), request.getUsername())
                .then(Mono.defer(() -> {
                    String encodedPassword = passwordEncoder.encode(request.getPassword());
                    User newUser = userMapper.registerRequestToUser(request, encodedPassword);
                    return userRepository.save(newUser);
                }));
    }


    /**
     * Authentifie un utilisateur en fonction de son e-mail ou nom d'utulisateur et de son mot de passe.
     * Si l'e-mail ou le nom de l'utilisateur n'ont pas été trouvés ou bien si le mot de passe fourni est incorrect,
     * la méthode renvoie null sans lever d'exception.
     * On ne veut pas donner d'indication précise sur la raison précise qui a empêché l'authentification.
     *
     * @param loginRequest La demande de connexion contenant l'e-mail ou nom ainsi que le mot de passe de l'utilisateur.
     * @return L'ID de l'utilisateur s'il est authentifié avec succès, ou null si ce n'est pas le cas.
     */
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

    /**
     * Récupère l'utilisateur actuellement authentifié à partir du contexte de sécurité réactif.
     * Si aucune authentification valide n'est trouvée ou si l'utilisateur n'existe pas dans la base de données,
     * une exception est levée.
     *
     * @return Un Mono contenant l'utilisateur authentifié si celui-ci est trouvé et valide, sinon une erreur.
     * @throws com.mdd.back.exception.ResourceNotFoundException Si le contexte d'authentification est vide,
     *                                                          si l'utilisateur n'est pas authentifié ou si l'utilisateur n'existe pas dans la base de données.
     */
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
                            String email = auth.getName();
                            return userRepository.findByEmail(email)
                                    .switchIfEmpty(Mono.error(new ResourceNotFoundException("Utilisateur non trouvé!")));
                        })
        );
    }

    /**
     * Récupère l'ID de l'utilisateur actuellement authentifié.
     * Si aucune authentification valide n'est trouvée ou si l'utilisateur n'existe pas dans la base de données,
     * une exception est levée.
     *
     * @return Un Mono contenant l'UUID de l'utilisateur authentifié si celui-ci est trouvé et valide, sinon une erreur.
     * @throws com.mdd.back.exception.ResourceNotFoundException Si le contexte d'authentification est vide,
     *                                                          si l'utilisateur n'est pas authentifié ou si
     *                                                          l'utilisateur n'existe pas dans la base de données.
     */
    public Mono<UUID> getAuthenticatedUserId() {
        return getAuthenticatedUser() // Utilise la méthode existante
                .map(User::getId) // Extrait l'UUID de l'utilisateur
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("ID utilisateur non trouvé!")));
    }

    public Mono<UserDto> updateAuthenticatedUser(UpdateAuthenticatedUserRequest request) {
        return getAuthenticatedUser()
                .flatMap(authenticatedUser -> {
                    // Vérifier si les données ont changé
                    boolean emailChanged = !authenticatedUser.getEmail().equals(request.getEmail());
                    boolean usernameChanged = !authenticatedUser.getUsername().equals(request.getUsername());

                    if (emailChanged || usernameChanged) {
                        // Validation d'unicité uniquement si les champs ont changé
                        return userValidator.validateUniqueEmailAndUsername(authenticatedUser.getId(), request.getEmail(), request.getUsername())
                                .then(Mono.defer(() -> {
                                    // Mise à jour après validation réussie
                                    userMapper.updateAuthenticatedUserFromRequest(request, authenticatedUser, passwordEncoder);
                                    return userRepository.save(authenticatedUser)
                                            .map(userMapper::userToUserDto);
                                }));
                    }

                    // Si aucune donnée ne change, sauvegarde directe
                    userMapper.updateAuthenticatedUserFromRequest(request, authenticatedUser, passwordEncoder);
                    return userRepository.save(authenticatedUser)
                            .map(userMapper::userToUserDto);
                })
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Utilisateur non authentifié ou introuvable")));

    }

}
