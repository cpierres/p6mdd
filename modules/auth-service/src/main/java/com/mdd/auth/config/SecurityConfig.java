package com.mdd.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Value("#{'${frontend.url:http://localhost:4200}'.split(',')}")
    private List<String> frontendUrls;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            // Présence d'un client OAuth2 (Google/Apple) → activation conditionnelle du flux social
            ObjectProvider<ReactiveClientRegistrationRepository> clientRegistrations
    ) {
        // configuration de sécurité de base pour le service d'auth
        boolean socialEnabled = clientRegistrations.getIfAvailable() != null;

        ServerHttpSecurity config = http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(
                                "/actuator/health",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/.well-known/jwks.json",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/refresh",
                                "/api/auth/logout"
                        ).permitAll()
                        .anyExchange().authenticated()
                );

        // Activer oauth2Login uniquement si un client OAuth2 est configuré (présence d'un ClientRegistrationRepository)
        if (socialEnabled) {
            // Activation du flux OAuth2 Login (Google/Apple) uniquement si un client est configuré.
            // Un handler de succès dédié pourra être ajouté plus tard si nécessaire.
            config = config.oauth2Login(Customizer.withDefaults());
        }

        return config.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Configuration CORS pour autoriser le front (liste CSV frontend.url)
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(frontendUrls);
        cfg.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Cookie"));
        cfg.setExposedHeaders(Arrays.asList("Set-Cookie", "Access-Control-Allow-Credentials"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
