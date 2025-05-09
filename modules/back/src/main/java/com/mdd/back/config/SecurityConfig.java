package com.mdd.back.config;

import com.mdd.back.services.JwtService;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {
    @Value("${frontend.url}")
    private String frontendUrl;

    /**
     * Configure la chaîne de filtrage de sécurité pour l'application, en définissant des politiques de sécurité telles
     * que la désactivation de CSRF, la gestion des sessions sans état, l'autorisation d'accès à des points de
     * terminaison spécifiques sans authentification et l'exigence d'authentification pour tous les autres itinéraires.
     * Il configure également le serveur de ressources OAuth2 pour utiliser l'authentification JWT.
     *
     * @param http l'objet {@link HttpSecurity} utilisé pour configurer les paramètres de sécurité
     * @return l'instance {@link SecurityFilterChain} construite après l'application de toutes les configurations
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
                                "/api/topics",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api/topics/stats/stream"
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
     * Configurer le décodeur JWT et utiliser la clé secrète
     * @param jwtService
     * @return
     */
    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder(JwtService jwtService) {
        log.debug("*** ReactiveJwtDecoder ***");
        return NimbusReactiveJwtDecoder
                .withSecretKey(jwtService.getSecretKey()).build();
    }

    @Bean
    public JwtEncoder jwtEncoder(JwtService jwtService) {
        log.debug("*** jwtEncoder ***");
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtService.getSecretKey()));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(Collections.singletonList(this.frontendUrl));
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfig.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        corsConfig.setAllowCredentials(true); // Si vous utilisez des cookies ou des sessions partagées

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig); // Appliquer à tous les endpoints
        return source;
    }

}
