package com.mdd.back.services;

import com.mdd.back.entities.User;
import com.mdd.back.mappers.UserMapper;
import com.mdd.back.models.RegisterRequest;
import com.mdd.back.repositories.UserRepository;
import com.mdd.back.services.interfaces.IUserRegistrationService;
import com.mdd.back.services.interfaces.IValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implémentation du service d'enregistrement des utilisateurs.
 */
@Service
public class UserRegistrationService implements IUserRegistrationService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final IValidator<User> userValidator; // Dépendance à l'interface

    @Autowired
    public UserRegistrationService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder, @Qualifier("userValidator") IValidator<User> userValidator) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.userValidator = userValidator;
    }

    @Override
    public Mono<User> registerNewUser(RegisterRequest request) {
        // Créer un User temporaire pour la validation
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User userToValidate = userMapper.registerRequestToUser(request, encodedPassword);

        // Utiliser la méthode validate() de l'interface
        return userValidator.validate(userToValidate).then(userRepository.save(userToValidate));
    }
}


