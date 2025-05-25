package com.mdd.back.repositories;

import com.mdd.back.entities.Post;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface PostRepository extends ReactiveCrudRepository<Post, UUID> {

    //compter les posts par topicId (pour alimenter TopicStatDto)
    @Query("SELECT COUNT(*) FROM mddsocial.post WHERE topic_id = :topicId")
    Mono<Long> countByTopicId(UUID topicId);

    Flux<Post> findAllByOrderByUpdatedAtDesc();

    Flux<Post> findAllByTopicIdOrderByUpdatedAtDesc(UUID topicId);

    // Trouver les posts dont le topic_id est dans la liste fournie, triés par date de mise à jour décroissante
//    @Query("SELECT * FROM mddsocial.post WHERE topic_id = ANY(:topicIds) ORDER BY updated_at DESC")
//    Flux<Post> findAllByTopicIdInOrderByUpdatedAtDesc(List<UUID> topicIds);

    // récupérer les posts des topics auxquels l'utilisateur est abonné
    @Query("SELECT p.* FROM mddsocial.post p " +
            "JOIN mddsocial.user_topic_subscription uts ON p.topic_id = uts.topic_id " +
            "WHERE uts.user_id = :userId " +
            "ORDER BY p.updated_at DESC")
    Flux<Post> findAllByUserSubscriptions(UUID userId);
}
