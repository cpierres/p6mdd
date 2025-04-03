package com.mdd.back.controller;

import com.mdd.back.exception.ResourceAlreadyExistException;
import com.mdd.back.models.AuthSuccess;
import com.mdd.back.models.RegisterRequest;
import com.mdd.back.services.AuthService;
import com.mdd.back.services.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

//    @PostMapping("/register")
//    public Mono<ResponseEntity<User>> registerUser(@RequestBody RegisterRequest request) {
//        return authService.registerNewUser(request)
//                .map(user -> ResponseEntity.ok(user)) // Retourner l'utilisateur créé si tout fonctionne
//                .onErrorResume(ResourceAlreadyExistException.class, ex -> {
//                    // Gérer l'exception si un utilisateur avec l'email existe déjà
//                    return Mono.just(ResponseEntity.badRequest().body(null));
//                });
//    }


//    @PostMapping("/register")
//    public Mono<ResponseEntity<?>> registerUser(@Valid @RequestBody RegisterRequest request) {
//        return authService.registerNewUser(request)
//                .map(user -> {
//                    //ResponseEntity.ok(user);
//                    String token = jwtService.generateToken(user.getId(), user.getEmail());
//                    return ok(new AuthSuccess(token));
//                }) // Retourner l'utilisateur créé si tout fonctionne
//                .onErrorResume(ResourceAlreadyExistException.class, ex -> {
//                    // Gérer l'exception si un utilisateur avec l'email existe déjà
//                    return Mono.just(ResponseEntity.badRequest().body(null));
//                });
//    }

    @PostMapping("/register")
    public Mono<ResponseEntity<AuthSuccess>> registerUser(@Valid @RequestBody RegisterRequest request) {
        return authService.registerNewUser(request)
                .map(user -> {
                    //ResponseEntity.ok(user);
                    String token = jwtService.generateToken(user.getId(), user.getEmail());
                    return ok(new AuthSuccess(token));
                }) // Retourner l'utilisateur créé si tout fonctionne
                .onErrorResume(ResourceAlreadyExistException.class, ex -> {
                    // Gérer l'exception si un utilisateur avec l'email existe déjà
                    return Mono.just(ResponseEntity.badRequest().body(null));
                });
    }
}
