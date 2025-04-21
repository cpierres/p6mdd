package com.mdd.back.repositories;

import com.mdd.back.entities.PostComment;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface PostCommentRepository extends ReactiveCrudRepository<PostComment, UUID> {

    //compter les commentaires par un TopicId (pour alimenter TopicStatDto))
    @Query("SELECT COUNT(*) FROM mddsocial.post_comment pc JOIN mddsocial.post p ON pc.post_id = p.id WHERE p.topic_id = :topicId")
    Mono<Long> countByTopicId(UUID topicId);

}