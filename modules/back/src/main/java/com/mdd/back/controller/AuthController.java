package com.mdd.back.controller;

import com.mdd.back.exception.ResourceAlreadyExistException;
import com.mdd.back.models.*;
import com.mdd.back.services.AuthService;
import com.mdd.back.services.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @Operation(summary = "Enregistrement d'un utilisateur (doublon sur email interdit)",
            description = """
                    Suite à son enregistrement, le nouvel utilisateur est directement connecté (authentification stateless Bearer jwt)
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Succès : retour du token JWT ",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthSuccess.class))),
            @ApiResponse(responseCode = "400", description = "Raison(s) de l'erreur (validation de RegisterRequest)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Un utilisateur avec cet email existe déjà",
                    content = @Content(mediaType = "application/json"))
    })
    @SecurityRequirement(name = "") // Aucun schéma de sécurité
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

    @Operation(summary = "Authentification d'un utilisateur déjà enregistré, via son email et mot de passe",
            description = """
                    L'utilisateur sera connecté via une authentification stateless (token).
                    Si email et/ou mot de passe incorrect, message erreur (ne précisant volontairement
                    pas quel élément est en erreur).
                    """,
            security = @SecurityRequirement(name = "") // Désactive la sécurité
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Succès : retour du token JWT ",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthSuccess.class))),
            @ApiResponse(responseCode = "401", description = "Login ou mot de passe incorrect",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/login")
    public Mono<ResponseEntity<AuthSuccess>> login(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest)
                .flatMap(userId -> {
                    String token = jwtService.generateToken(userId, loginRequest.getEmail());
                    return Mono.just(ok(new AuthSuccess(token)));// Retourne le JWT au client
                })
                .switchIfEmpty(Mono.just(status(HttpStatus.UNAUTHORIZED).body(null)));
    }

}
