package com.mdd.back.config;

import com.mdd.back.entities.User;
import com.mdd.back.entities.UserTopicSubscription;
import com.mdd.back.repositories.TopicRepository;
import com.mdd.back.repositories.UserRepository;
import com.mdd.back.repositories.UserTopicSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

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
    private final TopicRepository topicRepository;
    private final UserTopicSubscriptionRepository userTopicSubscriptionRepository;

    @Bean
    public CommandLineRunner initDemoData() {
        return args -> {
            log.info("Initialisation des données de démonstration...");

            createUsers()
                    .then(createTopicSubscriptionsForUserCpierres())
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

    /**
     * Crée les abonnements aux topics pour l'utilisateur cpierres.
     * L'utilisateur s'abonne aux topics suivants :
     * - Spring WebFlux
     * - Databases R2DBC
     * - Microservices
     * - Angular Nouveautés
     * - Tous les topics dont le titre commence par "Projet"
     */
    private Mono<Void> createTopicSubscriptionsForUserCpierres() {
        log.info("Création des abonnements aux topics pour l'utilisateur cpierres...");

        // Liste des titres de topics spécifiques
        List<String> specificTopicTitles = Arrays.asList(
                "Spring WebFlux",
                "Databases R2DBC",
                "Microservices",
                "Angular Nouveautés"
        );

        // Récupérer l'utilisateur cpierres
        return userRepository.findByUsername("cpierres")
                .flatMap(user -> {
                    // Récupérer tous les topics
                    return topicRepository.findAll()
                            .filter(topic ->
                                    // Filtrer les topics spécifiques ou ceux commençant par "Projet"
                                    specificTopicTitles.contains(topic.getTitle()) ||
                                            topic.getTitle().startsWith("Projet")
                            )
                            .flatMap(topic -> {
                                // Vérifier si l'abonnement existe déjà
                                return userTopicSubscriptionRepository.existsByUserIdAndTopicId(user.getId(), topic.getId())
                                        .flatMap(exists -> {
                                            if (Boolean.TRUE.equals(exists)) {
                                                log.info("L'utilisateur cpierres est déjà abonné au topic '{}'", topic.getTitle());
                                                return Mono.empty();
                                            } else {
                                                // Créer l'abonnement
                                                UserTopicSubscription subscription = UserTopicSubscription.builder()
                                                        .userId(user.getId())
                                                        .topicId(topic.getId())
                                                        .build();
                                                return userTopicSubscriptionRepository.save(subscription)
                                                        .doOnSuccess(s -> log.info("Abonnement créé pour l'utilisateur cpierres au topic '{}'", topic.getTitle()));
                                            }
                                        });
                            })
                            .then();
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("L'utilisateur cpierres n'a pas été trouvé, impossible de créer les abonnements aux topics.");
                    return Mono.empty();
                }));
    }

}

