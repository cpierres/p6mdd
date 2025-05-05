package com.mdd.back.config;

import com.mdd.back.entities.User;
import com.mdd.back.repositories.PostRepository;
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
    private final TopicRepository topicRepository;
    private final UserTopicSubscriptionRepository userTopicSubscriptionRepository;
    private final PostRepository postRepository;
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

//    /**
//     * Crée les abonnements aux topics pour les utilisateurs de démonstration.
//     */
//    private Mono<Void> createTopicSubscriptions() {
//        // Récupérer les utilisateurs u2 et u3
//        Mono<User> u2Mono = userRepository.findByUsername("u2")
//                .switchIfEmpty(Mono.error(new IllegalArgumentException("Utilisateur u2 non trouvé")));
//        Mono<User> u3Mono = userRepository.findByUsername("u3")
//                .switchIfEmpty(Mono.error(new IllegalArgumentException("Utilisateur u3 non trouvé")));
//
//        // Récupérer les topics par titre
//        Mono<Topic> topic1Mono = topicRepository.findAll()
//                .filter(topic -> "Thème 1".equals(topic.getTitle()))
//                .next()
//                .switchIfEmpty(Mono.error(new IllegalArgumentException("Topic 'Thème 1' non trouvé")));
//
//        Mono<Topic> topic2Mono = topicRepository.findAll()
//                .filter(topic -> "Thème 2".equals(topic.getTitle()))
//                .next()
//                .switchIfEmpty(Mono.error(new IllegalArgumentException("Topic 'Thème 2' non trouvé")));
//
//        Mono<Topic> topic3Mono = topicRepository.findAll()
//                .filter(topic -> "Thème 3".equals(topic.getTitle()))
//                .next()
//                .switchIfEmpty(Mono.error(new IllegalArgumentException("Topic 'Thème 3' non trouvé")));
//
//        // Créer les abonnements pour u2 (Thème 1 et Thème 3)
//        Mono<Void> u2Subscriptions = Mono.zip(u2Mono, topic1Mono, topic3Mono)
//                .flatMap(tuple -> {
//                    User u2 = tuple.getT1();
//                    Topic topic1 = tuple.getT2();
//                    Topic topic3 = tuple.getT3();
//
//                    // Vérifier si l'abonnement existe déjà pour Thème 1
//                    return userTopicSubscriptionRepository.existsByUserIdAndTopicId(u2.getId(), topic1.getId())
//                            .flatMap(exists -> {
//                                if (Boolean.TRUE.equals(exists)) {
//                                    log.info("L'abonnement de u2 au Thème 1 existe déjà.");
//                                    return Mono.<UserTopicSubscription>empty();
//                                } else {
//                                    // Créer l'abonnement pour Thème 1
//                                    UserTopicSubscription subscription1 = UserTopicSubscription.builder()
//                                            .userId(u2.getId())
//                                            .topicId(topic1.getId())
//                                            .build();
//                                    return userTopicSubscriptionRepository.save(subscription1)
//                                            .doOnSuccess(s -> log.info("Abonnement de u2 au Thème 1 créé avec succès."));
//                                }
//                            })
//                            // Vérifier si l'abonnement existe déjà pour Thème 3
//                            .then(userTopicSubscriptionRepository.existsByUserIdAndTopicId(u2.getId(), topic3.getId()))
//                            .flatMap(exists -> {
//                                if (Boolean.TRUE.equals(exists)) {
//                                    log.info("L'abonnement de u2 au Thème 3 existe déjà.");
//                                    return Mono.<UserTopicSubscription>empty();
//                                } else {
//                                    // Créer l'abonnement pour Thème 3
//                                    UserTopicSubscription subscription3 = UserTopicSubscription.builder()
//                                            .userId(u2.getId())
//                                            .topicId(topic3.getId())
//                                            .build();
//                                    return userTopicSubscriptionRepository.save(subscription3)
//                                            .doOnSuccess(s -> log.info("Abonnement de u2 au Thème 3 créé avec succès."));
//                                }
//                            });
//                });
//
//
//    /**
//     * Crée les posts pour les utilisateurs de démonstration.
//     */
//    private Mono<Void> createPosts() {
//        // Formater la date actuelle
//        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
//
//        // Contenu commun pour tous les posts
//        String contentTemplate =
//                "contenu ligne 1 pour thème %s\n" +
//                        "contenu ligne 2 pour thème %s\n" +
//                        "contenu ligne 3 pour thème %s\n" +
//                        "contenu ligne 4 pour thème %s\n" +
//                        "contenu ligne 5 pour thème %s\n" +
//                        "contenu ligne 6 pour thème %s";
//
//        // Récupérer utilisateurs u2
//        Mono<User> u2Mono = userRepository.findByUsername("u2");
//
//        // Récupérer les topics par titre
//        Mono<Topic> topic1Mono = topicRepository.findAll()
//                .filter(topic -> "Thème 1".equals(topic.getTitle()))
//                .next();
//
//        Mono<Topic> topic2Mono = topicRepository.findAll()
//                .filter(topic -> "Thème 2".equals(topic.getTitle()))
//                .next();
//
//        Mono<Topic> topic3Mono = topicRepository.findAll()
//                .filter(topic -> "Thème 3".equals(topic.getTitle()))
//                .next();
//
//        // Créer le post de u2 pour le Thème 1
//        Mono<Void> u2Post1 = Mono.zip(u2Mono, topic1Mono)
//                .flatMap(tuple -> {
//                    User u2 = tuple.getT1();
//                    Topic topic1 = tuple.getT2();
//
//                    String title = "titre de " + currentDateTime + " pour le thème 1";
//                    String content = String.format(contentTemplate, "1", "1", "1", "1", "1", "1");
//
//                    Post post = Post.builder()
//                            .topicId(topic1.getId())
//                            .title(title)
//                            .content(content)
//                            .createdBy(u2.getId())
//                            .createdAt(Instant.now())
//                            .updatedAt(Instant.now())
//                            .build();
//
//                    return postRepository.save(post)
//                            .doOnSuccess(p -> log.info("Post de u2 pour le Thème 1 créé avec succès."));
//                })
//                .then();
//
//        // Créer le post de u2 pour le Thème 2
//        Mono<Void> u2Post2 = Mono.zip(u2Mono, topic2Mono)
//                .flatMap(tuple -> {
//                    User u2 = tuple.getT1();
//                    Topic topic2 = tuple.getT2();
//
//                    String title = "Post du " + currentDateTime + " pour le thème 2";
//                    String content = String.format(contentTemplate, "2", "2", "2", "2", "2", "2");
//
//                    Post post = Post.builder()
//                            .topicId(topic2.getId())
//                            .title(title)
//                            .content(content)
//                            .createdBy(u2.getId())
//                            .createdAt(Instant.now())
//                            .updatedAt(Instant.now())
//                            .build();
//
//                    return postRepository.save(post)
//                            .doOnSuccess(p -> log.info("Post de u2 pour le Thème 2 créé avec succès."));
//                })
//                .then();
//
//
//        return Flux.concat(u2Post1, u2Post2).then();
//    }
}

