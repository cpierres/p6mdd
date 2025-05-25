package com.mdd.back.services;

import com.mdd.back.entities.UserTopicSubscription;
import com.mdd.back.mappers.TopicMapper;
import com.mdd.back.models.TopicSubscribedForAuthUserDto;
import com.mdd.back.repositories.PostCommentRepository;
import com.mdd.back.repositories.PostRepository;
import com.mdd.back.repositories.TopicRepository;
import com.mdd.back.repositories.UserTopicSubscriptionRepository;
import com.mdd.back.services.interfaces.IAuthenticationService;
import com.mdd.back.services.interfaces.ISubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Implémentation du service de gestion des abonnements aux topics.
 */
@Service
public class SubscriptionService implements ISubscriptionService {
    private final UserTopicSubscriptionRepository userTopicSubscriptionRepository;
    private final TopicRepository topicRepository;
    private final TopicMapper topicMapper;
    private final IAuthenticationService authenticationService;
    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;

    @Autowired
    public SubscriptionService(UserTopicSubscriptionRepository userTopicSubscriptionRepository,
                               TopicRepository topicRepository,
                               TopicMapper topicMapper,
                               IAuthenticationService authenticationService,
                               PostRepository postRepository,
                               PostCommentRepository postCommentRepository) {
        this.userTopicSubscriptionRepository = userTopicSubscriptionRepository;
        this.topicRepository = topicRepository;
        this.topicMapper = topicMapper;
        this.authenticationService = authenticationService;
        this.postRepository = postRepository;
        this.postCommentRepository = postCommentRepository;
    }

    @Override
    public Mono<Void> subscribeAuthenticatedUserToTopic(UUID topicId) {
        // Vérifier l'existence du topic dans la base
        return topicRepository.existsById(topicId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new IllegalArgumentException("Le topic avec l'identifiant fourni n'existe pas."));
                    }
                    // Récupérer l'utilisateur authentifié et vérifier l'abonnement
                    return authenticationService.getAuthenticatedUserId()
                            .flatMap(userId -> userTopicSubscriptionRepository.existsByUserIdAndTopicId(userId, topicId)
                                    .flatMap(isSubscribed -> {
                                        if (isSubscribed) {
                                            return Mono.empty(); // L'utilisateur est déjà abonné
                                        } else {
                                            UserTopicSubscription subscription = UserTopicSubscription.builder()
                                                    .userId(userId)
                                                    .topicId(topicId)
                                                    .build();
                                            return userTopicSubscriptionRepository.save(subscription).then(); // Sauvegarder l'abonnement
                                        }
                                    }));
                });
    }

    @Override
    public Mono<Void> unsubscribeAuthenticatedUserFromTopic(UUID topicId) {
        return authenticationService.getAuthenticatedUserId()
                .flatMap(userId -> userTopicSubscriptionRepository.deleteByUserIdAndTopicId(userId, topicId).then());
    }

    @Override
    public Mono<Boolean> isAuthenticatedUserSubscribedToTopic(UUID topicId) {
        return authenticationService.getAuthenticatedUserId()
                .flatMap(userId -> userTopicSubscriptionRepository.existsByUserIdAndTopicId(userId, topicId));
    }

    @Override
    public Flux<TopicSubscribedForAuthUserDto> getAllTopicsWithAuthUserSubscription() {
        return authenticationService.getAuthenticatedUser()
                .flatMapMany(authenticatedUser -> {
                    UUID userId = authenticatedUser.getId();
                    // Récupère tous les topics puis ajoute un flag 'subscribed'
                    return topicRepository.findAll()
                            .flatMap(topic -> {
                                Mono<Boolean> isSubscribedMono = userTopicSubscriptionRepository
                                        .existsByUserIdAndTopicId(userId, topic.getId());
                                Mono<Long> postCountMono = postRepository.countByTopicId(topic.getId());
                                Mono<Long> commentCountMono = postCommentRepository.countByTopicId(topic.getId());

                                return Mono.zip(isSubscribedMono, postCountMono, commentCountMono)
                                        .map(tuple -> {
                                            TopicSubscribedForAuthUserDto dto = topicMapper
                                                    .topicToTopicSubscribedForAuthUserDto(topic);
                                            dto.setSubscribed(tuple.getT1());
                                            dto.setCountPosts(tuple.getT2());
                                            dto.setCountComments(tuple.getT3());
                                            return dto;
                                        });
                            })
                            // Tri par popularité (somme des posts et commentaires) en ordre décroissant
                            // avec tri secondaire sur priorityOrder si popularité égale
                            .sort((t1, t2) -> {
                                Long t1Popularity = t1.getCountPosts() + t1.getCountComments();
                                Long t2Popularity = t2.getCountPosts() + t2.getCountComments();
                                int popularityComparison = t2Popularity.compareTo(t1Popularity); // Ordre décroissant

                                // Si la popularité est égale, on trie par priorityOrder
                                if (popularityComparison == 0) {
                                    // Ordre croissant pour priorityOrder (plus petit = plus prioritaire)
                                    return Double.compare(t1.getPriorityOrder(), t2.getPriorityOrder());
                                }

                                return popularityComparison;
                            });
                });
    }
}
