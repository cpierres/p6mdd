package com.mdd.back.services;

import com.mdd.back.entities.User;
import com.mdd.back.exception.ResourceNotFoundException;
import com.mdd.back.mappers.UserMapper;
import com.mdd.back.models.UpdateAuthenticatedUserRequest;
import com.mdd.back.models.UserDto;
import com.mdd.back.repositories.UserRepository;
import com.mdd.back.services.interfaces.IAuthenticationService;
import com.mdd.back.services.interfaces.IUserProfileService;
import com.mdd.back.services.interfaces.IValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implémentation du service de gestion des profils utilisateurs.
 */
@Service
public class UserProfileService implements IUserProfileService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final IValidator<User> userValidator; // Dépendance à l'interface
    private final IAuthenticationService authenticationService;

    @Autowired
    public UserProfileService(UserRepository userRepository,
                              UserMapper userMapper,
                              PasswordEncoder passwordEncoder,
                              @Qualifier("userValidator") IValidator<User> userValidator,
                              IAuthenticationService authenticationService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.userValidator = userValidator;
        this.authenticationService = authenticationService;
    }

    @Override
    public Mono<UserDto> updateAuthenticatedUser(UpdateAuthenticatedUserRequest request) {
        return authenticationService.getAuthenticatedUser()
                .flatMap(authenticatedUser -> {
                    // Vérifier si les données ont changé
                    boolean emailChanged = !authenticatedUser.getEmail().equals(request.getEmail());
                    boolean usernameChanged = !authenticatedUser.getUsername().equals(request.getUsername());

                    if (emailChanged || usernameChanged) {
                        // Créer un User temporaire pour la validation
                        User userToValidate = User.builder()
                                .id(authenticatedUser.getId())
                                .email(request.getEmail())
                                .username(request.getUsername())
                                .build();

                        // Utiliser la méthode validate() de l'interface
                        return userValidator.validate(userToValidate)
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
