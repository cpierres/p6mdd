package com.mdd.back.config;

import com.mdd.back.services.AuthService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import reactor.core.publisher.Mono;

import java.util.UUID;


/**
 * Configuration pour R2DBC avec activation des fonctionnalités d'auditing.
 * Cette classe configure l'audit réactif pour les entités R2DBC, en permettant
 * la gestion automatisée des champs d'audit tels que `createdAt` et `updatedAt`.
 * Elle expose également un bean permettant de déterminer l'identité de l'utilisateur
 * actuellement authentifié via le service AuthService.
 * L'objectif principal est d'intégrer la gestion des métadonnées d'audit lors
 * des opérations sur les entités au sein d'un environnement réactif.
 */
@Configuration
@EnableR2dbcAuditing
public class R2dbcConfig {
    // Active auditing pour R2DBC (createdAt, updatedAt)
    private final AuthService authService;

    public R2dbcConfig(AuthService authService) {
        this.authService = authService;
    }

    @Bean
    ReactiveAuditorAware<UUID> auditorAware() {
        return () -> authService.getAuthenticatedUserId()
                .onErrorResume(e -> Mono.empty());
    }

}