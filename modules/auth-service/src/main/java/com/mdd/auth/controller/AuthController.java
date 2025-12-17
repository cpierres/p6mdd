package com.mdd.auth.controller;

import com.mdd.auth.entities.User;
import com.mdd.auth.models.*;
import com.mdd.auth.repositories.UserRepository;
import com.mdd.auth.services.AuthService;
import com.mdd.auth.services.JwtTokenService;
import com.mdd.auth.services.RefreshTokenService;
import com.mdd.auth.utils.RequestIdFilter;
import jakarta.validation.Valid;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

/**
 * API d'authentification (JWT access token + cookie HttpOnly refresh token).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    private final AuthService authService;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, JwtTokenService jwtTokenService, RefreshTokenService refreshTokenService, UserRepository userRepository) {
        this.authService = authService;
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<ApiResult<AuthSuccess>>> register(@Valid @RequestBody RegisterRequest request, ServerWebExchange exchange) {
        String requestId = (String) exchange.getAttributes().get(RequestIdFilter.REQUEST_ID_KEY);

        return authService.register(request)
                .flatMap(user -> {
                    String accessToken = jwtTokenService.generateAccessToken(user);
                    return refreshTokenService.createRefreshToken(user.getId())
                            .map(refreshToken -> {
                                boolean isSecure = isSslEnabled(exchange);
                                boolean crossSite = isCrossSite(exchange);
                                String sameSite = chooseSameSite(isSecure, crossSite);

                                ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                                        .httpOnly(true)
                                        .secure(isSecure)
                                        .sameSite(sameSite)
                                        .path("/")
                                        .maxAge(Duration.ofDays(7))
                                        .build();

                                ApiResult<AuthSuccess> apiResult = new ApiResult<>(
                                        new AuthSuccess(accessToken),
                                        "Utilisateur enregistré avec succès.",
                                        HttpStatus.OK.value(),
                                        requestId
                                );
                                return ResponseEntity.ok()
                                        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                                        .body(apiResult);
                            });
                });
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<ApiResult<AuthSuccess>>> login(@Valid @RequestBody LoginRequest request, ServerWebExchange exchange) {
        String requestId = (String) exchange.getAttributes().get(RequestIdFilter.REQUEST_ID_KEY);
        boolean isSecure = isSslEnabled(exchange);

        return authService.authenticate(request.getIdentifier(), request.getPassword())
                .flatMap(user -> {
                    String accessToken = jwtTokenService.generateAccessToken(user);
                    return refreshTokenService.createRefreshToken(user.getId())
                            .map(refreshToken -> {
                                boolean crossSite = isCrossSite(exchange);
                                String sameSite = chooseSameSite(isSecure, crossSite);
                                ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                                        .httpOnly(true)
                                        .secure(isSecure)
                                        .sameSite(sameSite)
                                        .path("/")
                                        .maxAge(Duration.ofDays(7))
                                        .build();

                                ApiResult<AuthSuccess> apiResult = new ApiResult<>(
                                        new AuthSuccess(accessToken),
                                        "Authentification réussie.",
                                        HttpStatus.OK.value(),
                                        requestId
                                );
                                return ResponseEntity.ok()
                                        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                                        .body(apiResult);
                            });
                })
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResult<>(null,
                                "Authentification échouée. Veuillez vérifier vos identifiants.",
                                HttpStatus.UNAUTHORIZED.value(),
                                requestId))));
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<ApiResult<AuthSuccess>>> refresh(ServerWebExchange exchange) {
        String requestId = (String) exchange.getAttributes().get(RequestIdFilter.REQUEST_ID_KEY);
        boolean isSecure = isSslEnabled(exchange);

        return Mono.justOrEmpty(exchange.getRequest().getCookies().getFirst(REFRESH_TOKEN_COOKIE_NAME))
                .map(HttpCookie::getValue)
                .flatMap(refreshToken -> refreshTokenService.validateRefreshToken(refreshToken)
                        .flatMap(userId -> userRepository.findById(userId)
                                .map(user -> {
                                    String newAccessToken = jwtTokenService.generateAccessToken(user);

                                    boolean crossSite = isCrossSite(exchange);
                                    String sameSite = chooseSameSite(isSecure, crossSite);
                                    ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                                            .httpOnly(true)
                                            .secure(isSecure)
                                            .sameSite(sameSite)
                                            .path("/")
                                            .maxAge(Duration.ofDays(7))
                                            .build();

                                    ApiResult<AuthSuccess> apiResult = new ApiResult<>(
                                            new AuthSuccess(newAccessToken),
                                            "Token rafraîchi avec succès.",
                                            HttpStatus.OK.value(),
                                            requestId
                                    );
                                    return ResponseEntity.ok()
                                            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                                            .body(apiResult);
                                }))
                        .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(new ApiResult<>(null, "Refresh token invalide.", HttpStatus.UNAUTHORIZED.value(), requestId))))
                )
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResult<>(null, "Refresh token manquant.", HttpStatus.UNAUTHORIZED.value(), requestId))));
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<ApiResult<Void>>> logout(ServerWebExchange exchange) {
        String requestId = (String) exchange.getAttributes().get(RequestIdFilter.REQUEST_ID_KEY);
        boolean isSecure = isSslEnabled(exchange);

        boolean crossSite = isCrossSite(exchange);
        String sameSite = chooseSameSite(isSecure, crossSite);

        ResponseEntity<ApiResult<Void>> baseResponse = ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                        .httpOnly(true)
                        .secure(isSecure)
                        .sameSite(sameSite)
                        .path("/")
                        .maxAge(0)
                        .build().toString())
                .body(new ApiResult<>(null, "Déconnexion réussie.", HttpStatus.OK.value(), requestId));

        // Si un refresh token est présent, on le supprime côté DB (best effort)
        return Mono.justOrEmpty(exchange.getRequest().getCookies().getFirst(REFRESH_TOKEN_COOKIE_NAME))
                .map(HttpCookie::getValue)
                .flatMap(rt -> refreshTokenService.validateRefreshToken(rt)
                        .flatMap(userId -> refreshTokenService.deleteByUserId(userId).thenReturn(baseResponse))
                )
                .switchIfEmpty(Mono.just(baseResponse));
    }

    /**
     * Retourne l'utilisateur actuellement authentifié.
     *
     * Le Front appelle cet endpoint après `login`/`register` afin de constituer la session.
     */
    @GetMapping("/me")
    public Mono<ResponseEntity<ApiResult<UserDto>>> me(@AuthenticationPrincipal Jwt jwt, ServerWebExchange exchange) {
        String requestId = (String) exchange.getAttributes().get(RequestIdFilter.REQUEST_ID_KEY);

        if (jwt == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResult<>(null, "Non authentifié.", HttpStatus.UNAUTHORIZED.value(), requestId)));
        }

        String userIdClaim = jwt.getClaimAsString("id");
        if (userIdClaim == null || userIdClaim.trim().isEmpty()) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResult<>(null, "Token invalide.", HttpStatus.UNAUTHORIZED.value(), requestId)));
        }

        UUID userId;
        try {
            userId = UUID.fromString(userIdClaim);
        } catch (IllegalArgumentException ex) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResult<>(null, "Token invalide.", HttpStatus.UNAUTHORIZED.value(), requestId)));
        }

        return userRepository.findById(userId)
                .map(AuthController::toUserDto)
                .map(dto -> ResponseEntity.ok(new ApiResult<>(dto, "Utilisateur courant.", HttpStatus.OK.value(), requestId)))
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResult<>(null, "Utilisateur introuvable.", HttpStatus.UNAUTHORIZED.value(), requestId))));
    }

    private static UserDto toUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setCreated_at(user.getCreatedAt());
        dto.setUpdated_at(user.getUpdatedAt());
        return dto;
    }

    private static boolean isSslEnabled(ServerWebExchange exchange) {
        return exchange.getRequest().getSslInfo() != null;
    }

    private static boolean isCrossSite(ServerWebExchange exchange) {
        String origin = exchange.getRequest().getHeaders().getOrigin();
        if (origin == null || origin.trim().isEmpty()) {
            return false;
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
        if (request.getHeaders().getHost() != null) {
            int hostPort = request.getHeaders().getHost().getPort();
            if (hostPort != -1) {
                return hostPort;
            }
        }
        int uriPort = request.getURI().getPort();
        if (uriPort != -1) {
            return uriPort;
        }
        String scheme = request.getURI().getScheme();
        return "https".equals(scheme) ? 443 : 80;
    }

    private static String chooseSameSite(boolean isSecure, boolean crossSite) {
        if (crossSite && isSecure) {
            return "None";
        }
        return isSecure ? "Strict" : "Lax";
    }
}
