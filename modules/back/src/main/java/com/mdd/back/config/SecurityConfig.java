package com.mdd.back.config;

import com.mdd.back.services.JwtService;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.adapter.ForwardedHeaderTransformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {
    // Modifier le type pour accepter une liste d'URLs
    @Value("#{'${frontend.url}'.split(',')}")
    private List<String> frontendUrls;

    @Value("${api.url:}")
    private String apiUrl;


    /**
     * Configure la chaîne de filtrage de sécurité pour l'application, en définissant des politiques de sécurité telles
     * que la désactivation de CSRF, la gestion des sessions sans état, l'autorisation d'accès à des points de
     * terminaison spécifiques sans authentification et l'exigence d'authentification pour tous les autres itinéraires.
     * 
     * Cette méthode configure l'application comme un serveur de ressources OAuth2 via la ligne:
     * `.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))`
     * 
     * Dans l'architecture OAuth2, on distingue généralement deux rôles:
     * 1. Le serveur d'autorisation: responsable de l'authentification des utilisateurs et de l'émission des tokens
     * 2. Le serveur de ressources: responsable de la validation des tokens et de la protection des ressources
     * 
     * Notre application implémente ces deux rôles:
     * - Serveur d'autorisation: via les endpoints /api/auth/login et /api/auth/register et le JwtService
     * - Serveur de ressources: via la configuration oauth2ResourceServer qui valide les tokens JWT
     * 
     * Cette approche "tout-en-un" est appelée "serveur de ressources OAuth2 autonome".
     *
     * @param http l'objet {@link ServerHttpSecurity} utilisé pour configurer les paramètres de sécurité
     * @return l'instance {@link SecurityWebFilterChain} construite après l'application de toutes les configurations
     * @throws Exception si une erreur se produit lors de la configuration de la chaîne de filtrage de sécurité
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) throws Exception {
        log.debug("*** securityFilterChain ***");
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/refresh",
                                "/api/topics",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api/topics/stats/stream",
                                "/api/posts/stream",
                                "/api/comments/stream"
                        ).permitAll() // Autoriser les accès publics
                        .anyExchange().authenticated() // Authentification pour toutes les autres routes
                )
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable) // Désactiver authentication HTTP basic si non nécessaire
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults())) // Configurer OAuth2 avec JWT si utilisé
                .build();
    }

    /**
     * Crée et renvoie un bean pour l'interface {@link PasswordEncoder} pour encoder les mots de passe de manière
     * sécurisée.
     * On fournit à Spring Security une implémentation concrète correspondant à l'interface qu'il doit utiliser
     * dans son fonctionnement.
     * On doit configurer ce bean pour Spring parce qu'on est dans le cadre d'un serveur de ressources OAuth2 autonome
     * et qu'on a choisi de gérer nous-même l'authentification et la gestion du token.
     * L'encodeur renvoyé utilise l'algorithme de hachage BCrypt.
     * Ce bean est injectable n'importe où.
     *
     * @return une instance de {@link PasswordEncoder} utilisant l'algorithme de hachage BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.debug("*** passwordEncoder ***");
        return new BCryptPasswordEncoder();
    }

    /**
     * Configurer le décodeur JWT et utiliser la clé secrète.
     * Ce décodeur est utilisé par le serveur de ressources OAuth2 pour valider les tokens JWT.
     * Il fait partie de la configuration du serveur de ressources, qui est responsable de
     * protéger les ressources en vérifiant que les requêtes contiennent des tokens valides.
     * 
     * @param jwtService Service qui fournit la clé secrète pour valider les tokens
     * @return Un décodeur JWT réactif configuré avec la clé secrète
     */
    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder(JwtService jwtService) {
        log.debug("*** ReactiveJwtDecoder ***");
        return NimbusReactiveJwtDecoder
                .withSecretKey(jwtService.getSecretKey()).build();
    }

    /**
     * Configure l'encodeur JWT utilisé pour créer et signer les tokens JWT.
     * Cet encodeur est utilisé par le serveur d'autorisation (implémenté dans notre application)
     * pour générer des tokens JWT lors de l'authentification des utilisateurs.
     * Il fait partie de la logique du serveur d'autorisation, qui est responsable de
     * l'authentification des utilisateurs et de l'émission des tokens.
     *
     * @param jwtService Service qui fournit la clé secrète pour signer les tokens
     * @return Un encodeur JWT configuré avec la clé secrète
     */
    @Bean
    public JwtEncoder jwtEncoder(JwtService jwtService) {
        log.debug("*** jwtEncoder ***");
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtService.getSecretKey()));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        List<String> allowedOrigins = new ArrayList<>(frontendUrls);
        if (apiUrl != null && !apiUrl.isEmpty() && !allowedOrigins.contains(apiUrl)) {
            allowedOrigins.add(apiUrl);
        }

        corsConfig.setAllowedOrigins(allowedOrigins);

        log.debug("*** corsConfigurationSource (origines autorisées) *** : {}", frontendUrls);
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfig.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Cookie"));
        corsConfig.setExposedHeaders(Arrays.asList("Set-Cookie", "Access-Control-Allow-Credentials"));
        corsConfig.setAllowCredentials(true); // Important pour les cookies HttpOnly
        corsConfig.setMaxAge(3600L); // Cache la réponse pre-flight pendant 1 heure

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig); // Appliquer à tous les endpoints
        return source;
    }

    /**
     * Active la prise en compte des en-têtes Forwarded / X-Forwarded-*
     * ajoutés par un reverse proxy (ex: Synology / Nginx) afin que
     * Spring reconnaisse correctement le schéma (HTTP/HTTPS), l'hôte et les préfixes.
     */
    @Bean
    public ForwardedHeaderTransformer forwardedHeaderTransformer() {
        return new ForwardedHeaderTransformer();
    }
}
