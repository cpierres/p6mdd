package com.mdd.back.services;

import com.mdd.back.entities.User;
import com.mdd.back.models.LoginRequest;
import com.mdd.back.models.RegisterRequest;
import com.mdd.back.models.UpdateAuthenticatedUserRequest;
import com.mdd.back.models.UserDto;
import com.mdd.back.services.interfaces.IAuthenticationService;
import com.mdd.back.services.interfaces.IUserProfileService;
import com.mdd.back.services.interfaces.IUserRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Classe façade qui délègue les appels aux services spécifiques.
 * Cette classe est maintenue pour assurer la compatibilité avec le code existant.
 */
@Service
public class AuthFacade {
    private final IAuthenticationService authenticationService;
    private final IUserRegistrationService userRegistrationService;
    private final IUserProfileService userProfileService;

    @Autowired
    public AuthFacade(IAuthenticationService authenticationService,
                      IUserRegistrationService userRegistrationService,
                      IUserProfileService userProfileService) {
        this.authenticationService = authenticationService;
        this.userRegistrationService = userRegistrationService;
        this.userProfileService = userProfileService;
    }

    public Mono<User> registerNewUser(RegisterRequest request) {
        return userRegistrationService.registerNewUser(request);
    }

    public Mono<UUID> login(LoginRequest loginRequest) {
        return authenticationService.login(loginRequest);
    }

    public Mono<User> getAuthenticatedUser() {
        return authenticationService.getAuthenticatedUser();
    }

    public Mono<UUID> getAuthenticatedUserId() {
        return authenticationService.getAuthenticatedUserId();
    }

    public Mono<UserDto> updateAuthenticatedUser(UpdateAuthenticatedUserRequest request) {
        return userProfileService.updateAuthenticatedUser(request);
    }
}
