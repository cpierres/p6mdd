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

            // Création ou récupération de l'utilisateur cpierres, puis création des abonnements
            ensureUserExists("cpierres", "christophe.pierres@gmail.com", "Test!1234")
                    .flatMap(this::createTopicSubscriptionsForUserCpierres)
                    // Création de l'utilisateur u2 (sans enchaînement d'abonnements)
                    .then(ensureUserExists("u2", "u2@test.com", "Test!1234"))
//                    .then(createPosts())
                    .subscribe(
                            user -> log.info("Utilisateur {} initialisé", user.getUsername()),
                            error -> log.error("Erreur lors de l'initialisation des données de démonstration: {}", error.getMessage()),
                            () -> log.info("Données de démonstration initialisées avec succès.")
                    );
        };
    }

    /**
     * Crée ou récupère un utilisateur par son nom d'utilisateur
     */
    private Mono<User> ensureUserExists(String username, String email, String password) {
        return userRepository.findByUsername(username)
                .doOnNext(existingUser -> log.info("L'utilisateur {} existe déjà.", username))
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("Création de l'utilisateur {}...", username);
                    User user = User.builder()
                            .email(email)
                            .username(username)
                            .password(passwordEncoder.encode(password))
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build();
                    return userRepository.save(user)
                            .doOnSuccess(savedUser -> log.info("Utilisateur {} créé avec succès.", username));
                }));
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
    private Mono<Void> createTopicSubscriptionsForUserCpierres(User user) {
        log.info("Création des abonnements aux topics pour l'utilisateur {}...", user.getUsername());

        // Liste des titres de topics spécifiques
        List<String> specificTopicTitles = Arrays.asList(
                "Spring WebFlux",
                "Databases R2DBC",
                "Microservices",
                "Angular Nouveautés"
        );

        // Récupérer tous les topics et créer les abonnements
        return topicRepository.findAll()
                .doOnNext(topic -> log.info("Topic disponible : {}", topic.getTitle()))
                .filter(topic ->
                        // Filtrer les topics spécifiques ou ceux commençant par "Projet"
                        specificTopicTitles.contains(topic.getTitle()) ||
                                topic.getTitle().startsWith("Projet")
                )
                .doOnNext(topic -> log.info("Topic sélectionné pour abonnement : {}", topic.getTitle()))
                .flatMap(topic -> {
                    // Vérifier si l'abonnement existe déjà
                    return userTopicSubscriptionRepository.existsByUserIdAndTopicId(user.getId(), topic.getId())
                            .flatMap(exists -> {
                                if (Boolean.TRUE.equals(exists)) {
                                    log.info("L'utilisateur {} est déjà abonné au topic '{}'", user.getUsername(), topic.getTitle());
                                    return Mono.empty();
                                } else {
                                    // Créer l'abonnement
                                    log.info("Création d'un nouvel abonnement pour l'utilisateur {} au topic '{}'", user.getUsername(), topic.getTitle());
                                    UserTopicSubscription subscription = UserTopicSubscription.builder()
                                            .userId(user.getId())
                                            .topicId(topic.getId())
                                            .build();
                                    return userTopicSubscriptionRepository.save(subscription)
                                            .doOnSuccess(s -> log.info("Abonnement créé pour l'utilisateur {} au topic '{}'", user.getUsername(), topic.getTitle()));
                                }
                            });
                })
                .then();
    }
}

