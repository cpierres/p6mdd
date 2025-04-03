package com.mdd.back.services;

import com.mdd.back.entities.User;
import com.mdd.back.exception.ResourceAlreadyExistException;
import com.mdd.back.mappers.UserMapper;
import com.mdd.back.models.RegisterRequest;
import com.mdd.back.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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
}
