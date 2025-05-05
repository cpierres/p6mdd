package com.mdd.back.config;

import com.mdd.back.entities.User;
import com.mdd.back.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Classe d'initialisation des données de démonstration.
 * Cette classe est active uniquement en environnement de développement.
 * Elle crée des utilisateurs de test, des abonnements à des topics et des posts.
 */
@Slf4j
@Configuration
//@Profile("dev") // Actif uniquement en environnement de développement
@RequiredArgsConstructor
public class DemoDataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initDemoData() {
        return args -> {
            log.info("Initialisation des données de démonstration...");

            createUsers()
//                    .then(createTopicSubscriptions())
//                    .then(createPosts())
                    .subscribe(
                            null,
                            error -> log.error("Erreur lors de l'initialisation des données de démonstration: {}", error.getMessage()),
                            () -> log.info("Données de démonstration initialisées avec succès.")
                    );
        };
    }

    /**
     * Crée les utilisateurs de démonstration s'ils n'existent pas déjà.
     */
    private Mono<Void> createUsers() {
        // Vérifier si l'utilisateur cpierres existe déjà
        return userRepository.findByUsername("cpierres")
                .flatMap(existingUser -> {
                    log.info("L'utilisateur cpierres existe déjà.");
                    return Mono.empty();
                })
                .switchIfEmpty(
                        // Créer l'utilisateur u2 s'il n'existe pas
                        Mono.defer(() -> {
                            User cpierres = User.builder()
                                    .email("christophe.pierres@gmail.com")
                                    .username("cpierres")
                                    .password(passwordEncoder.encode("Test!1234"))
                                    .createdAt(Instant.now())
                                    .updatedAt(Instant.now())
                                    .build();
                            return userRepository.save(cpierres)
                                    .doOnSuccess(user -> log.info("Utilisateur cpierres créé avec succès."));
                        })
                )
                // Vérifier si l'utilisateur u2 existe déjà
                .then(userRepository.findByUsername("u2"))
                .flatMap(existingUser -> {
                    log.info("L'utilisateur u2 existe déjà.");
                    return Mono.empty();
                })
                .switchIfEmpty(
                        // Créer l'utilisateur
                        Mono.defer(() -> {
                            User u2 = User.builder()
                                    .email("u2@test.com")
                                    .username("u2")
                                    .password(passwordEncoder.encode("Test!1234"))
                                    .createdAt(Instant.now())
                                    .updatedAt(Instant.now())
                                    .build();
                            return userRepository.save(u2)
                                    .doOnSuccess(user -> log.info("Utilisateur u2 créé avec succès."));
                        })
                )
                .then();
    }
}

