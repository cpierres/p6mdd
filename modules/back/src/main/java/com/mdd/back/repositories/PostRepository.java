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
}
