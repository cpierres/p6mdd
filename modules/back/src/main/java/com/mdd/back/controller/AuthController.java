package com.mdd.back.controller;

import com.mdd.back.exception.ResourceNotFoundException;
import com.mdd.back.mappers.UserMapper;
import com.mdd.back.models.*;
import com.mdd.back.repositories.UserRepository;
import com.mdd.back.services.AuthFacade;
import com.mdd.back.services.JwtService;
import com.mdd.back.services.RefreshTokenService;
import com.mdd.back.utils.context.RequestIdContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

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
    private final AuthFacade authFacade;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    public AuthController(AuthFacade authFacade, JwtService jwtService, UserMapper userMapper, RefreshTokenService refreshTokenService, UserRepository userRepository) {
        this.authFacade = authFacade;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Enregistrement d'un utilisateur (contrôle d'unicité sur email et username)",
            description = """
                    Suite à son enregistrement, le nouvel utilisateur sera directement connecté (authentification stateless Bearer jwt)
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Succès : retour du token JWT ",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class,
                                    subTypes = {AuthSuccess.class},
                                    example = """
                                            {
                                              "data": {
                                                "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1MUB0ZXN0LmNvbSIsImlkIjoiMWQz..."
                                              },
                                              "message": "Utilisateur enregistré avec succès.",
                                              "status": 200,
                                              "timestamp": "2025-05-05T15:03:11.217714100Z",
                                              "requestId": "90f265ed-7a07-4ba1-8847-3a733c6ddeae"
                                            }
                                            """)
                    )),
            @ApiResponse(responseCode = "400", description = "Raison(s) de l'erreur (validation de RegisterRequest)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class,
                                    subTypes = {ResponseDetails.class}))),
            @ApiResponse(responseCode = "409", description = "Un utilisateur avec cet email ou ce nom existe déjà",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class,
                                    subTypes = {ResponseDetails.class},
                                    example = """
                                            {
                                              "data": {
                                                "message": "Conflit(s) : email: Un utilisateur avec cet email existe déjà. username: Un utilisateur avec ce nom existe déjà.",
                                                "severity": "warning",
                                                "fieldErrors": [
                                                  {
                                                    "field": "email",
                                                    "message": "Un utilisateur avec cet email existe déjà.",
                                                    "severity": "warning"
                                                  },
                                                  {
                                                    "field": "username",
                                                    "message": "Un utilisateur avec ce nom existe déjà.",
                                                    "severity": "warning"
                                                  }
                                                ]
                                              },
                                              "message": "Conflits multiples",
                                              "status": 409,
                                              "timestamp": "2025-05-05T15:04:34.233750Z",
                                              "requestId": "28bf14d4-ee93-448f-9bdf-6442162de14c"
                                            }
                                            """)))

    })
    @SecurityRequirement(name = "") // Aucun schéma de sécurité
    @PostMapping("/register")
    public Mono<ResponseEntity<ApiResult<AuthSuccess>>> registerUser(@Valid @RequestBody RegisterRequest request, ServerWebExchange exchange) {
        // Récupérer l'ID de requête depuis les attributs d'échange
        String requestId = (String) exchange.getAttributes().get(RequestIdContext.REQUEST_ID_KEY);

        return authFacade.registerNewUser(request)
                .map(user -> {
                    String token = jwtService.generateToken(user.getId(), user.getEmail());

                    ApiResult<AuthSuccess> apiResult = new ApiResult<>(
                            new AuthSuccess(token),
                            "Utilisateur enregistré avec succès.",
                            HttpStatus.OK.value(),
                            requestId
                    );

                    return ResponseEntity.ok(apiResult);
                }) // ne pas traiter l'erreur ici ; la laisser remonter dans gestionnaire global
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
            @ApiResponse(responseCode = "200", description = "Succès : retour du token JWT",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class,
                                    subTypes = {AuthSuccess.class},
                                    example = """
                                            {
                                                "data": {
                                                    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6Ik..."
                                                },
                                                "message": "Authentification réussie.",
                                                "status": 200,
                                                "timestamp": "2025-05-05T14:25:34.726938Z",
                                                "requestId": "60f7396f-df28-4b64-888e-a1932adfe12a"
                                            }
                                            """)
                    )),
            @ApiResponse(responseCode = "401", description = "Echec : login ou mot de passe incorrect",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class,
                                    example = """
                                            {
                                                "data": null,
                                                "message": "Authentification échouée. Veuillez vérifier vos identifiants.",
                                                "status": 401,
                                                "timestamp": "2025-05-05T14:25:34.726938Z",
                                                "requestId": "60f7396f-df28-4b64-888e-a1932adfe12a"
                                            }
                                            """)
                    ))
    })

    @PostMapping("/login")
    public Mono<ResponseEntity<ApiResult<AuthSuccess>>> login(@Valid @RequestBody LoginRequest loginRequest, ServerWebExchange exchange) {
        // Récupérer l'ID de requête depuis les attributs d'échange
        String requestId = (String) exchange.getAttributes().get(RequestIdContext.REQUEST_ID_KEY);

        // Détecter si la requête est en HTTPS
        boolean isSecure = isSslEnabled(exchange);

        return authFacade.login(loginRequest)
                .flatMap(userId -> {
                    // Générer l'access token
                    String token = jwtService.generateToken(userId, loginRequest.getIdentifier());

                    // Créer un refresh token
                    return refreshTokenService.createRefreshToken(userId)
                            .map(refreshToken -> {
                                // Créer la réponse avec l'access token
                                ApiResult<AuthSuccess> successResponse = new ApiResult<>(
                                        new AuthSuccess(token),
                                        "Authentification réussie.",
                                        HttpStatus.OK.value(),
                                        requestId
                                );

                                // Déterminer le mode SameSite/secure en fonction du contexte (proxy, HTTPS, cross-site)
                                boolean crossSite = isCrossSite(exchange);
                                String sameSite = chooseSameSite(isSecure, crossSite);

                                // Créer un cookie HttpOnly pour le refresh token
                                ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                                        .httpOnly(true)
                                        .secure(isSecure)
                                        .sameSite(sameSite)
                                        .path("/")
                                        .maxAge(Duration.ofDays(7))
                                        .build();

                                // Retourner la réponse avec le cookie
                                return ResponseEntity.ok()
                                        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                                        .body(successResponse);
                            });
                })
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResult<>(
                                null,
                                "Authentification échouée. Veuillez vérifier vos identifiants.",
                                HttpStatus.UNAUTHORIZED.value(),
                                requestId
                        ))));
    }

    @Operation(summary = "Affichage de l'utilisateur authentifié.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Information sur l'utilisateur connecté (sans le mot de passe)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class, subTypes = {UserDto.class},
                                    example = """
                                            {
                                              "data": {
                                                "id": "1d31bfe3-92ed-49c9-a2af-09c0bc87f034",
                                                "username": "u1",
                                                "email": "u1@test.com",
                                                "created_at": "2025-05-05T15:03:11.196801Z",
                                                "updated_at": "2025-05-05T15:03:11.196801Z"
                                              },
                                              "message": "Informations utilisateur récupérées avec succès.",
                                              "status": 200,
                                              "timestamp": "2025-05-05T15:40:12.230646300Z",
                                              "requestId": "fccb86cf-3717-4a27-b9ce-5add6cf14f6f"
                                            }
                                            """))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Utilisateur introuvable",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class,
                                    example = """
                                            {
                                            "message": "Contexte d'Authentification vide",
                                            "status": 200,
                                            "timestamp": "2025-05-05T15:40:12.230646300Z",
                                            "requestId": "fccb86cf-3717-4a27-b9ce-5add6cf14f6f"
                                            }
                                            """))
            )
    })

    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/refresh")
    public Mono<ResponseEntity<ApiResult<AuthSuccess>>> refreshToken(ServerWebExchange exchange) {
        // Récupérer l'ID de requête depuis les attributs d'échange
        String requestId = (String) exchange.getAttributes().get(RequestIdContext.REQUEST_ID_KEY);
        boolean isSecure = isSslEnabled(exchange);

        // Récupérer le refresh token depuis le cookie
        return Mono.justOrEmpty(exchange.getRequest().getCookies().getFirst(REFRESH_TOKEN_COOKIE_NAME))
                .map(HttpCookie::getValue)
                .flatMap(refreshToken -> refreshTokenService.validateRefreshToken(refreshToken)
                        .flatMap(userId -> {
                            // Récupérer l'utilisateur pour obtenir son identifiant
                            return userRepository.findById(userId)
                                    .flatMap(user -> {
                                        // Générer un nouveau token
                                        String newToken = jwtService.generateToken(userId, user.getEmail());

                                        // Créer la réponse
                                        ApiResult<AuthSuccess> successResponse = new ApiResult<>(
                                                new AuthSuccess(newToken),
                                                "Token rafraîchi avec succès.",
                                                HttpStatus.OK.value(),
                                                requestId
                                        );

                                        // Configurer le cookie de refresh token (prolonger sa durée)
                                        boolean crossSite = isCrossSite(exchange);
                                        String sameSite = chooseSameSite(isSecure, crossSite);
                                        ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                                                .httpOnly(true)
                                                .secure(isSecure)
                                                .sameSite(sameSite)
                                                .path("/")//.path("/api/auth")
                                                .maxAge(Duration.ofDays(7))
                                                .build();

                                        return Mono.just(ResponseEntity.ok()
                                                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                                                .body(successResponse));
                                    });
                        })
                        .switchIfEmpty(Mono.just(ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(new ApiResult<>(
                                        null,
                                        "Refresh token invalide.",
                                        HttpStatus.UNAUTHORIZED.value(),
                                        requestId
                                ))))
                )
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResult<>(
                                null,
                                "Refresh token manquant.",
                                HttpStatus.UNAUTHORIZED.value(),
                                requestId
                        ))));
    }

    @Operation(summary = "Déconnexion de l'utilisateur",
            description = """
                    Invalide le refresh token de l'utilisateur et supprime le cookie.
                    """,
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Déconnexion réussie",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class)
                    ))
    })
    @PostMapping("/logout")
    public Mono<ResponseEntity<ApiResult<Void>>> logout(ServerWebExchange exchange) {
        // Récupérer l'ID de requête depuis les attributs d'échange
        String requestId = (String) exchange.getAttributes().get(RequestIdContext.REQUEST_ID_KEY);
        boolean isSecure = isSslEnabled(exchange);

        // Créer la réponse avec le cookie expiré
        boolean crossSite = isCrossSite(exchange);
        String sameSite = chooseSameSite(isSecure, crossSite);
        ResponseEntity<ApiResult<Void>> response = ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                        .httpOnly(true)
                        .secure(isSecure)
                        .sameSite(sameSite)
                        .path("/")
                        .maxAge(0)
                        .build().toString())
                .body(new ApiResult<Void>(
                        null,
                        "Déconnexion réussie.",
                        HttpStatus.OK.value(),
                        requestId
                ));

        return authFacade.getAuthenticatedUserId()
                .flatMap(userId -> refreshTokenService.deleteByUserId(userId)
                        .then(Mono.just(response))
                )
                .switchIfEmpty(Mono.just(response));
    }

    private static boolean isSslEnabled(ServerWebExchange exchange) {
        // Détection HTTPS réelle (derrière proxy pris en charge via ForwardedHeaderTransformer)
        return exchange.getRequest().getSslInfo() != null;
    }

    /**
     * Détermine si la requête est cross-site en comparant l'en-tête Origin
     * à l'origine (schéma + hôte + port) de la requête vue par l'application
     * après application des en-têtes Forwarded/X-Forwarded-*. 
     */
    private static boolean isCrossSite(ServerWebExchange exchange) {
        String origin = exchange.getRequest().getHeaders().getOrigin();
        if (origin == null || origin.isBlank()) {
            return false; // Pas d'origin => on considère même-site
        }
        String scheme = exchange.getRequest().getURI().getScheme();
        String host = exchange.getRequest().getHeaders().getHost() != null
                ? exchange.getRequest().getHeaders().getHost().getHostName()
                : exchange.getRequest().getURI().getHost();

        int port = determinePort(exchange.getRequest());
        String portPart = "";
        if (port > 0 && !(("http".equalsIgnoreCase(scheme) && port == 80) || ("https".equalsIgnoreCase(scheme) && port == 443))) {
            portPart = ":" + port;
        }
        String requestOrigin = scheme + "://" + host + portPart;
        return !origin.equalsIgnoreCase(requestOrigin);
    }

    private static int determinePort(ServerHttpRequest request) {
        // Essayer d'abord l'en-tête Host
        if (request.getHeaders().getHost() != null) {
            int hostPort = request.getHeaders().getHost().getPort();
            if (hostPort != -1) {
                return hostPort;
            }
        }

        // Fallback vers l'URI
        int uriPort = request.getURI().getPort();
        if (uriPort != -1) {
            return uriPort;
        }

        // Valeurs par défaut selon le schéma
        String scheme = request.getURI().getScheme();
        return "https".equals(scheme) ? 443 : 80;
    }

    /**
     * Choisit la stratégie SameSite suivant le contexte.
     * - Cross-site en HTTPS: SameSite=None (Secure requis par les navigateurs)
     * - HTTPS même-site: SameSite=Strict
     * - HTTP (dev local): SameSite=Lax
     */
    private static String chooseSameSite(boolean isSecure, boolean crossSite) {
        if (crossSite && isSecure) {
            return "None";
        }
        return isSecure ? "Strict" : "Lax";
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<ApiResult<UserDto>>> getCurrentUser(ServerWebExchange exchange) {
        // Récupérer l'ID de requête depuis les attributs d'échange
        String requestId = (String) exchange.getAttributes().get(RequestIdContext.REQUEST_ID_KEY);

        return authFacade.getAuthenticatedUser()
                .map(userMapper::userToUserDto)
                .map(userDto -> {
                    ApiResult<UserDto> apiResult = new ApiResult<>(
                            userDto,
                            "Informations utilisateur récupérées avec succès.",
                            HttpStatus.OK.value(),
                            requestId
                    );
                    return ResponseEntity.ok(apiResult);
                })
                .onErrorResume(ResourceNotFoundException.class, ex -> {
                    ApiResult<UserDto> apiResult = new ApiResult<>(
                            null,
                            ex.getMessage(),
                            HttpStatus.NOT_FOUND.value(),
                            requestId
                    );

                    return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(apiResult)
                    );
                }); // Gérer les erreurs type 404
    }

    @Operation(
            summary = "Met à jour les informations de l'utilisateur connecté",
            description = "Cette méthode permet à l'utilisateur actuellement connecté de mettre à jour son profil. Les informations telles que l'email, le nom d'utilisateur et le mot de passe peuvent être modifiées."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Mise à jour réussie",
                    content = @Content(schema = @Schema(implementation = ApiResult.class, subTypes = AuthSuccess.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requête invalide ou données de mise à jour mal formatées",
                    content = @Content(schema = @Schema(implementation = ApiResult.class,
                            example = """
                                    {
                                      "data": {
                                        "message": "Les données d'entrée ne sont pas valides.",
                                        "severity": "error",
                                        "fieldErrors": [
                                          {
                                            "field": "password",
                                            "message": "Le mot de passe doit contenir au moins ..",
                                            "severity": "error"
                                          }
                                        ]
                                      },
                                      "message": "Erreur de validation",
                                      "status": 400,
                                      "timestamp": "2025-05-05T15:30:06.376661100Z",
                                      "requestId": "fbaa96a8-14b9-440e-b8ed-c5be4791595e"
                                    }
                                    """)
                    )),
            @ApiResponse(
                    responseCode = "401",
                    description = "Utilisateur non authentifié ou session expirée",
                    content = @Content()
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Utilisateur introuvable",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class, example = """
                                    {
                                      "message": "Utilisateur introuvable.",
                                      "status": 404,
                                      "timestamp": "2025-05-05T15:30:06.376661100Z",
                                      "requestId": "fbaa96a8-14b9-440e-b8ed-c5be4791595e"
                                    }
                                    """))
            )
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/me")
    public Mono<ResponseEntity<ApiResult<AuthSuccess>>> updateAuthenticatedUser(
            @Validated @RequestBody UpdateAuthenticatedUserRequest updateAuthenticatedUserRequest,
            ServerWebExchange exchange
    ) {
        // Récupérer l'ID de requête depuis les attributs d'échange
        String requestId = (String) exchange.getAttributes().get(RequestIdContext.REQUEST_ID_KEY);

        return authFacade.updateAuthenticatedUser(updateAuthenticatedUserRequest)
                .flatMap(userDto -> {
                    // Générer un nouveau token avec les informations mises à jour
                    String newToken = jwtService.generateToken(
                            userDto.getId(),
                            userDto.getEmail()
                    );

                    ApiResult<AuthSuccess> apiResult = new ApiResult<>(
                            new AuthSuccess(newToken),
                            "Informations utilisateur mises à jour avec succès.",
                            HttpStatus.OK.value(),
                            requestId
                    );

                    return Mono.just(ResponseEntity.ok(apiResult));
                })
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(new ApiResult<>(
                                null,
                                "Utilisateur non trouvé",
                                HttpStatus.NOT_FOUND.value(),
                                requestId
                        ))
                ));
    }
}
