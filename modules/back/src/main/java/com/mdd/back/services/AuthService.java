package com.mdd.back.services;

import com.mdd.back.entities.User;
import com.mdd.back.exception.ResourceAlreadyExistException;
import com.mdd.back.mappers.UserMapper;
import com.mdd.back.models.LoginRequest;
import com.mdd.back.models.RegisterRequest;
import com.mdd.back.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;
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

    public Mono<User> registerNewUser(RegisterRequest request) {
        // Vérifier si l'utilisateur existe déjà (de manière réactive)
        return userRepository.findByEmail(request.getEmail())
                .flatMap(existingUser -> {
                    // Si un utilisateur existe déjà, on rejette l'opération avec une exception
                    return Mono.<User>error(new ResourceAlreadyExistException(
                            "Un utilisateur avec cet email existe déjà !"));
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // Si aucun utilisateur n'existe, procéder à l'enregistrement
                    String encodedPassword = passwordEncoder.encode(request.getPassword());

                    //Mapper le RegisterRequest vers entité User via mappping AUTOMATIQUE (mapstruct)
                    //en tenant compte du traitement particulier sur le password
                    User newUser = userMapper.registerRequestToUser(request, encodedPassword);

                    // Sauvegarder l'utilisateur et retourner l'objet sauvegardé
                    return userRepository.save(newUser);
                }));
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

}
