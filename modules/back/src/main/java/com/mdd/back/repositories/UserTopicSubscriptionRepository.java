package com.mdd.back.repositories;

import com.mdd.back.entities.UserTopicSubscription;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserTopicSubscriptionRepository extends ReactiveCrudRepository<UserTopicSubscription, UUID> {

    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END " +
            "FROM mddsocial.user_topic_subscription " +
            "WHERE user_id = :userId AND topic_id = :topicId")
    Mono<Boolean> existsByUserIdAndTopicId(UUID userId, UUID topicId);

    // Supprimer un abonnement spécifique
    Mono<Void> deleteByUserIdAndTopicId(UUID userId, UUID topicId);

//    @Query("INSERT INTO mddsocial.user_topic_subscription (user_id, topic_id) VALUES (:userId, :topicId)")
//    Mono<Void> createSubscription(@Param("userId") UUID userId, @Param("topicId") UUID topicId);

}

