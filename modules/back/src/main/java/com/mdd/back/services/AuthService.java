package com.mdd.back.services;

import com.mdd.back.entities.User;
import com.mdd.back.exception.MultipleResourceAlreadyExistException;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;


    @Autowired
    public AuthService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    /**
     * Enregistre un nouvel utilisateur après vérifications et encodage du mot de passe.
     *
     * @param request L'objet contenant les informations de l'utilisateur à enregistrer,
     *                telles que l'email, le username et le mot de passe.
     * @return Un objet Mono contenant l'utilisateur enregistré si l'opération réussit,
     * ou une erreur si un utilisateur avec l'email spécifié existe déjà.
     */
//    public Mono<User> registerNewUser(RegisterRequest request) {
//        // Vérifier si l'utilisateur existe déjà (de manière réactive)
//        return userRepository.findByEmail(request.getEmail())
//                .flatMap(existingUser -> {
//                    // Si un utilisateur existe déjà, on rejette l'opération avec une exception
//                    return Mono.<User>error(new ResourceAlreadyExistException(
//                            "Un utilisateur avec cet email: " + existingUser.getEmail() + " existe déjà. Veuillez en choisir un autre."));
//                })
//                .switchIfEmpty(userRepository.findByUsername(request.getUsername())
//                        .flatMap(existingUser -> {
//                            // Si un utilisateur existe déjà avec ce username, rejeter l'opération avec une exception
//                            return Mono.<User>error(new ResourceAlreadyExistException(
//                                    "Un utilisateur avec ce nom: " + existingUser.getUsername() + " existe déjà. Veuillez en choisir un autre."));
//                        })
//                )
//                .switchIfEmpty(Mono.defer(() -> {
//                    // Si aucun utilisateur n'existe, enregistrement
//                    String encodedPassword = passwordEncoder.encode(request.getPassword());
//
//                    //Mapper le RegisterRequest vers entité User via mappping AUTOMATIQUE (mapstruct)
//                    //en tenant compte du traitement particulier sur le password
//                    User newUser = userMapper.registerRequestToUser(request, encodedPassword);
//
//                    // Sauvegarder l'utilisateur et retourner l'objet sauvegardé
//                    return userRepository.save(newUser);
//                }));
//    }
    public Mono<User> registerNewUser(RegisterRequest request) {
        Map<String, String> errors = new HashMap<>();

        Mono<Boolean> emailExists = userRepository.existsByEmail(request.getEmail());

        Mono<Boolean> usernameExists = userRepository.existsByUsername(request.getUsername());

        // Combine les 2 vérifications (asynchrone) pour capturer les résultats
        return Mono.zip(emailExists, usernameExists)
                .flatMap(results -> {
                    boolean emailConflict = results.getT1();
                    boolean usernameConflict = results.getT2();

                    // Ajoutez les erreurs en cas de conflits détectés
                    if (emailConflict) {
                        errors.put("email", "Un utilisateur avec cet email existe déjà.");
                    }
                    if (usernameConflict) {
                        errors.put("username", "Un utilisateur avec ce nom d'utilisateur existe déjà.");
                    }

                    // Si des conflits existent, exception
                    if (!errors.isEmpty()) {
                        return Mono.error(new MultipleResourceAlreadyExistException(errors));
                    }

                    // Si aucune erreur, encoder mot de passe et créer l'utilisateur
                    // Si aucun utilisateur n'existe, enregistrement
                    String encodedPassword = passwordEncoder.encode(request.getPassword());

                    //Mapper le RegisterRequest vers entité User via mappping AUTOMATIQUE (mapstruct)
                    //en tenant compte du traitement particulier sur le password
                    User newUser = userMapper.registerRequestToUser(request, encodedPassword);

                    return userRepository.save(newUser);
                });
    }

    /**
     * Authentifie un utilisateur en fonction de son e-mail et de son mot de passe.
     * Si l'e-mail de l'utilisateur n'est pas trouvé ou bien si le mot de passe fourni est incorrect,
     * la méthode renvoie null sans lever d'exception.
     * On ne veut pas donner d'indication précise sur la raison précise qui a empêché l'authentification.
     *
     * @param loginRequest La demande de connexion contenant l'e-mail et le mot de passe de l'utilisateur.
     * @return L'ID de l'utilisateur s'il est authentifié avec succès, ou null si ce n'est pas le cas.
     */
    public Mono<UUID> login(LoginRequest loginRequest) {
        return userRepository.findByEmail(loginRequest.getEmail())
                .flatMap(user -> {
                    // Vérification du mot de passe via le bean passwordEncoder (comparaison avec pw crypté)
                    if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                        return Mono.just(user.getId());
                    } else {
                        // Retourner un Mono vide si le mot de passe est incorrect
                        return Mono.empty();
                    }
                })
                .switchIfEmpty(Mono.empty()); // Retourner Mono.just(null) si l'utilisateur n'est pas trouvé ou non authentifié
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
                    // Utilisation du mapper pour modifier l'utilisateur existant avec encodage du mot de passe
                    //(injection du passwordEncoder grâce à @Context dans le mapper !)
                    userMapper.updateAuthenticatedUserFromRequest(request, authenticatedUser, passwordEncoder);

                    // Sauvegarder les modifications et retourner le DTO
                    return userRepository.save(authenticatedUser)
                            .map(userMapper::userToUserDto);
                })
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Utilisateur non authentifié ou introuvable")));
    }

}
