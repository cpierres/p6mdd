package com.mdd.back.controller;

import com.mdd.back.exception.ResourceNotFoundException;
import com.mdd.back.mappers.UserMapper;
import com.mdd.back.models.*;
import com.mdd.back.services.AuthService;
import com.mdd.back.services.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static org.springframework.http.ResponseEntity.ok;

@Tag(
        name = "auth-controller",
        description = """
                Permet de gérer l'authentification, l'enregistrement, et les informations des utilisateurs
                connectés. Les méthodes protégées utilisent des tokens JWT pour une authentification stateless sécurisée.
                """
)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    public AuthController(AuthService authService, JwtService jwtService, UserMapper userMapper) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
    }

    @Operation(summary = "Enregistrement d'un utilisateur (doublon sur email et username interdit)",
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
            @ApiResponse(responseCode = "409", description = "Un utilisateur avec cet email ou ce nom existe déjà",
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
                }) // ne pas traiter l'erreur ici ; la laisser remonter dans gestionnaire global
//                .onErrorResume(ResourceAlreadyExistException.class, ex -> {
//                    // Gérer l'exception si un utilisateur avec l'email existe déjà
//                    return Mono.just(ResponseEntity.badRequest().body(null));
//                })
                ;
    }

    @Operation(summary = "Authentification d'un utilisateur via son email ou son nom et son mot de passe",
            description = """
                    L'utilisateur sera connecté via une authentification stateless (token).
                    Si email/username et/ou mot de passe incorrect, message erreur (ne précisant volontairement
                    pas quel élément est en erreur).
                    """,
            security = @SecurityRequirement(name = "") // Désactive la sécurité
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Succès : retour du token JWT ",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthSuccess.class))),
            @ApiResponse(responseCode = "401", description = "Login (email ou nom) ou mot de passe incorrect",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/login")
    public Mono<ResponseEntity<AuthSuccess>> login(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest)
                .flatMap(userId -> {
                    String token = jwtService.generateToken(userId, loginRequest.getIdentifier());
                    return Mono.just(ok(new AuthSuccess(token)));// Retourne le JWT au client
                })
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build())); // Échec login
    }

    @Operation(summary = "Affichage de l'utilisateur authentifié.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Information sur l'utilisateur connecté (sans le mot de passe)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "404", description = "Impossible de retrouver l'utilisateur connecté",
                    content = @Content(mediaType = "application/json")),
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/me")
    public Mono<ResponseEntity<UserDto>> getCurrentUser() {
        return authService.getAuthenticatedUser()
                .map(userMapper::userToUserDto)
                //.map(userDto -> ResponseEntity.ok(userDto)) // Retourner le DTO dans le `ResponseEntity`
                .map(ResponseEntity::ok)
                .onErrorResume(ResourceNotFoundException.class, ex ->
                        Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(null)
                        )); // Gérer les erreurs type 404
    }

    @Operation(
            summary = "Met à jour les informations de l'utilisateur connecté",
            description = "Cette méthode permet à l'utilisateur actuellement connecté de mettre à jour son profil. Les informations telles que l'email, le nom d'utilisateur et le mot de passe peuvent être modifiées."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Mise à jour réussie",
                    content = @Content(schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requête invalide ou données de mise à jour mal formatées",
                    content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Utilisateur non authentifié ou session expirée",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Utilisateur introuvable",
                    content = @Content(mediaType = "application/json")
            )
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/me")
    public Mono<ResponseEntity<AuthSuccess>> updateAuthenticatedUser(
            @Validated @RequestBody UpdateAuthenticatedUserRequest updateAuthenticatedUserRequest
    ) {
        return authService.updateAuthenticatedUser(updateAuthenticatedUserRequest)
                .flatMap(userDto -> {
                    // Générer un nouveau token avec les informations mises à jour
                    String newToken = jwtService.generateToken(
                            userDto.getId(),
                            userDto.getEmail()
                    );
                    return Mono.just(ResponseEntity.ok(new AuthSuccess(newToken)));
                })
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }


}
