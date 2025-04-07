package com.mdd.back.services;

import com.mdd.back.entities.UserTopicSubscription;
import com.mdd.back.mappers.TopicMapper;
import com.mdd.back.models.TopicDto;
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
}
