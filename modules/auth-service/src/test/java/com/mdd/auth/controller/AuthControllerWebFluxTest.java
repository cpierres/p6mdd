package com.mdd.auth.controller;

import com.mdd.auth.config.SecurityConfig;
import com.mdd.auth.entities.User;
import com.mdd.auth.models.LoginRequest;
import com.mdd.auth.models.RegisterRequest;
import com.mdd.auth.repositories.UserRepository;
import com.mdd.auth.services.AuthService;
import com.mdd.auth.services.JwtKeysService;
import com.mdd.auth.services.JwtTokenService;
import com.mdd.auth.services.RefreshTokenService;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;

@WebFluxTest(controllers = AuthController.class)
@Import({SecurityConfig.class, JwtKeysService.class})
class AuthControllerWebFluxTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    JwtKeysService jwtKeysService;

    @MockitoBean
    AuthService authService;

    @MockitoBean
    JwtTokenService jwtTokenService;

    @MockitoBean
    RefreshTokenService refreshTokenService;

    @MockitoBean
    UserRepository userRepository;

    @Test
    void me_shouldReturn401_whenMissingToken() {
        webTestClient.get()
                .uri("/api/auth/me")
                .exchange()
                .expectStatus().isUnauthorized()
                // Important: ne doit pas provoquer une popup navigateur (pas de challenge Basic)
                .expectHeader().doesNotExist(HttpHeaders.WWW_AUTHENTICATE);
    }

    @Test
    void me_shouldReturnUser_whenTokenValid() throws Exception {
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();
        User user = new User(userId, "u1", "u1@test.com", "hash", now, now);

        Mockito.when(userRepository.findById(eq(userId))).thenReturn(Mono.just(user));

        String token = generateAccessToken(userId, user.getEmail());

        webTestClient.get()
                .uri("/api/auth/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.id").isEqualTo(userId.toString())
                .jsonPath("$.data.email").isEqualTo("u1@test.com")
                .jsonPath("$.data.username").isEqualTo("u1")
                .jsonPath("$.data.created_at").exists()
                .jsonPath("$.data.updated_at").exists()
                .jsonPath("$.status").isEqualTo(200);
    }

    private String generateAccessToken(UUID userId, String email) throws Exception {
        Instant now = Instant.now();
        Instant exp = now.plus(Duration.ofMinutes(5));

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer("http://localhost:8081")
                .subject(email)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(exp))
                .claim("id", userId.toString())
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256)
                        .keyID(jwtKeysService.getRsaJwk().getKeyID())
                        .build(),
                claims
        );

        signedJWT.sign(new RSASSASigner(jwtKeysService.getRsaJwk().toPrivateKey()));
        return signedJWT.serialize();
    }

    @Test
    void login_shouldReturn401_whenCredentialsInvalid() {
        LoginRequest req = new LoginRequest();
        req.setIdentifier("u1@test.com");
        req.setPassword("Test!1234");

        Mockito.when(authService.authenticate(anyString(), anyString()))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/api/auth/login")
                .bodyValue(req)
                .exchange()
                .expectStatus().isUnauthorized()
                // Important: ne doit pas provoquer une popup navigateur (pas de challenge Basic)
                .expectHeader().doesNotExist(HttpHeaders.WWW_AUTHENTICATE)
                .expectBody()
                .jsonPath("$.status").isEqualTo(401)
                .jsonPath("$.message").isEqualTo("Authentification échouée. Veuillez vérifier vos identifiants.");
    }

    @Test
    void login_shouldSetRefreshCookie_andReturnToken_whenOk() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "u1", "u1@test.com", "hash", Instant.now(), Instant.now());

        LoginRequest req = new LoginRequest();
        req.setIdentifier("u1@test.com");
        req.setPassword("Test!1234");

        Mockito.when(authService.authenticate(anyString(), anyString()))
                .thenReturn(Mono.just(user));
        Mockito.when(jwtTokenService.generateAccessToken(any(User.class)))
                .thenReturn("access.jwt");
        Mockito.when(refreshTokenService.createRefreshToken(eq(userId)))
                .thenReturn(Mono.just("refresh.token"));

        webTestClient.post()
                .uri("/api/auth/login")
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().value(HttpHeaders.SET_COOKIE, v -> {
                    // Vérifie présence cookie refresh_token
                    assert v.contains("refresh_token=");
                })
                .expectBody()
                .jsonPath("$.data.token").isEqualTo("access.jwt")
                .jsonPath("$.status").isEqualTo(200);
    }

    @Test
    void refresh_shouldReturn401_whenMissingCookie() {
        webTestClient.post()
                .uri("/api/auth/refresh")
                .exchange()
                .expectStatus().isUnauthorized()
                // Important: ne doit pas provoquer une popup navigateur (pas de challenge Basic)
                .expectHeader().doesNotExist(HttpHeaders.WWW_AUTHENTICATE)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Refresh token manquant.")
                .jsonPath("$.status").isEqualTo(401);
    }

    @Test
    void register_shouldReturnToken_andSetCookie_whenOk() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "u1", "u1@test.com", "hash", Instant.now(), Instant.now());

        RegisterRequest req = new RegisterRequest();
        req.setUsername("u1");
        req.setEmail("u1@test.com");
        req.setPassword("Test!1234");

        Mockito.when(authService.register(any(RegisterRequest.class)))
                .thenReturn(Mono.just(user));
        Mockito.when(jwtTokenService.generateAccessToken(any(User.class)))
                .thenReturn("access.jwt");
        Mockito.when(refreshTokenService.createRefreshToken(eq(userId)))
                .thenReturn(Mono.just("refresh.token"));

        webTestClient.post()
                .uri("/api/auth/register")
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists(HttpHeaders.SET_COOKIE)
                .expectBody()
                .jsonPath("$.data.token").isEqualTo("access.jwt")
                .jsonPath("$.status").isEqualTo(200);
    }
}
