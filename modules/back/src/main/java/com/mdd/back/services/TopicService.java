package com.mdd.back.services;

import com.mdd.back.entities.UserTopicSubscription;
import com.mdd.back.mappers.TopicMapper;
import com.mdd.back.models.TopicDto;
import com.mdd.back.models.TopicSubscribedForAuthUserDto;
import com.mdd.back.repositories.TopicRepository;
import com.mdd.back.repositories.UserTopicSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TopicService {
    private final TopicRepository topicRepository;
    private final TopicMapper topicMapper;
    private final AuthService authService;
    private final UserTopicSubscriptionRepository userTopicSubscriptionRepository;

    /**
     * @return Flux<TopicDto> contenant la liste des topics
     */
    public Flux<TopicDto> getAllTopics() {
        return topicRepository.findAll()
                .map(topicMapper::topicToTopicDto);
    }

     //TODO CLEAN
//    public Mono<Void> subscribeToTopic(UUID userId, UUID topicId) {
//        return userTopicSubscriptionRepository.existsByUserIdAndTopicId(userId, topicId)
//                .flatMap(exists -> {
//                    if (exists) {
//                        return Mono.empty(); // Pas d'enregistrement si déjà abonné
//                    } else {
//                        UserTopicSubscription subscription = UserTopicSubscription.builder()
//                                .id(UUID.randomUUID())
//                                .userId(userId)
//                                .topicId(topicId)
//                                .build();
//                        return userTopicSubscriptionRepository.save(subscription).then();
//                    }
//                });
//    }

    //    public Mono<Void> unsubscribeFromTopic(UUID userId, UUID topicId) {
//        return userTopicSubscriptionRepository.deleteByUserIdAndTopicId(userId, topicId).then();
//    }

    public Mono<Void> subscribeAuthenticatedUserToTopic(UUID topicId) {
        return authService.getAuthenticatedUserId()
                .flatMap(userId -> userTopicSubscriptionRepository.existsByUserIdAndTopicId(userId, topicId)
                        .flatMap(exists -> {
                            if (exists) {
                                return Mono.empty(); // utilisateur déjà abonné
                            } else {
                                UserTopicSubscription subscription = UserTopicSubscription.builder()
                                        //.id(UUID.randomUUID())//ATTENTION : si on passe une valeur, déclenche update
                                        .userId(userId)
                                        .topicId(topicId)
                                        .build();
                                return userTopicSubscriptionRepository.save(subscription).then();
                            }
                        }));
    }

    public Mono<Void> unsubscribeAuthenticatedUserFromTopic(UUID topicId) {
        return authService.getAuthenticatedUserId()
                .flatMap(userId -> userTopicSubscriptionRepository.deleteByUserIdAndTopicId(userId, topicId).then());
    }

    /**
     * Récupère tous les sujets disponibles et indique si l'utilisateur authentifié est abonné à chacun d'eux.
     * La méthode commence par récupérer les informations de l'utilisateur authentifié. Ensuite, pour chaque sujet,
     * elle vérifie si l'utilisateur y est abonné, puis construit un DTO (TopicSubscribedForAuthUserDto)
     * avec un indicateur représentant l'abonnement.
     *
     * @return Flux contenant une liste de TopicSubscribedForAuthUserDto. Chaque élément représente un Thème avec
     *         ses informations et un indicateur précisant si l'utilisateur authentifié est abonné à ce sujet.
     */
    public Flux<TopicSubscribedForAuthUserDto> getAllTopicsWithAuthUserSubscription() {
        // Récupère l'utilisateur authentifié
        return authService.getAuthenticatedUser()
                .flatMapMany(authenticatedUser -> {
                    UUID userId = authenticatedUser.getId();
                    // Récupère tous les topics puis ajoute un flag 'subscribed'
                    return topicRepository.findAll()
                            .flatMap(topic -> userTopicSubscriptionRepository.existsByUserIdAndTopicId(userId, topic.getId())
                                    .map(isSubscribed -> {
                                        TopicSubscribedForAuthUserDto dto = topicMapper.topicToTopicSubscribedForAuthUserDto(topic);
                                        dto.setSubscribed(isSubscribed); // Ajout du flag
                                        return dto;
                                    })
                            );
                });
    }

}
