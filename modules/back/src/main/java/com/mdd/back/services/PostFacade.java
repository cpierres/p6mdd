package com.mdd.back.services;

import com.mdd.back.models.PostCommentDto;
import com.mdd.back.models.PostDto;
import com.mdd.back.models.TopicStatsDto;
import com.mdd.back.services.interfaces.IPostCommentService;
import com.mdd.back.services.interfaces.IPostService;
import com.mdd.back.services.interfaces.IPostStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Classe façade qui délègue les appels aux services spécifiques.
 * Cette classe est maintenue pour assurer la compatibilité avec le code existant.
 */
@Service
@Slf4j
public class PostFacade {
    private final IPostService postService;
    private final IPostCommentService postCommentService;
    private final IPostStatisticsService postStatisticsService;

    @Autowired
    public PostFacade(IPostService postService,
                      IPostCommentService postCommentService,
                      IPostStatisticsService postStatisticsService) {
        this.postService = postService;
        this.postCommentService = postCommentService;
        this.postStatisticsService = postStatisticsService;
    }

    /**
     * Délègue la création d'un post au service spécifique.
     *
     * @param postDto Les informations du post à créer
     * @return Un Mono contenant le DTO du post créé
     */
    public Mono<PostDto> createPost(PostDto postDto) {
        return postService.createPost(postDto);
    }


    /**
     * Délègue la récupération des statistiques des topics au service spécifique.
     *
     * @return Un Flux contenant les statistiques des topics, triées par popularité
     */
    public Flux<TopicStatsDto> getTopicStats() {
        return postStatisticsService.getTopicStats();
    }

    /**
     * Délègue la récupération de tous les posts au service spécifique.
     *
     * @return Un Flux contenant les DTOs des posts
     */
    public Flux<PostDto> getAllPosts() {
        return postService.getAllPosts();
    }

    /**
     * Délègue la récupération des posts triés par thème au service spécifique.
     *
     * @return Un Flux contenant les DTOs des posts triés
     */
    public Flux<PostDto> getAllPostsSortedByTopic() {
        return postService.getAllPostsSortedByTopic();
    }

    /**
     * Délègue la récupération des posts triés par auteur au service spécifique.
     *
     * @return Un Flux contenant les DTOs des posts triés
     */
    public Flux<PostDto> getAllPostsSortedByAuthor() {
        return postService.getAllPostsSortedByAuthor();
    }

    /**
     * Délègue la récupération des posts d'un topic spécifique au service spécifique.
     *
     * @param topicId L'identifiant du topic
     * @return Un Flux contenant les DTOs des posts du topic
     */
    public Flux<PostDto> getPostsByTopic(UUID topicId) {
        return postService.getPostsByTopic(topicId);
    }

    /**
     * Délègue la récupération des posts des topics auxquels l'utilisateur est abonné au service spécifique.
     *
     * @return Un Flux contenant les DTOs des posts des topics auxquels l'utilisateur est abonné
     */
    public Flux<PostDto> getAllPostsSubscribed() {
        return postService.getAllPostsSubscribed();
    }

    /**
     * Délègue la création d'un commentaire au service spécifique.
     *
     * @param commentDto Les informations du commentaire à créer
     * @return Un Mono contenant le DTO du commentaire créé
     */
    public Mono<PostCommentDto> createComment(PostCommentDto commentDto) {
        return postCommentService.createComment(commentDto);
    }

    /**
     * Délègue la récupération d'un post avec ses commentaires au service spécifique.
     *
     * @param postId L'identifiant du post
     * @return Un Mono contenant le DTO du post avec ses commentaires
     */
    public Mono<PostDto> getPostWithComments(UUID postId) {
        return postService.getPostWithComments(postId);
    }

    /**
     * Délègue la récupération des posts triés par date de création en ordre ascendant au service spécifique.
     *
     * @return Un Flux contenant les DTOs des posts triés par date de création
     */
    public Flux<PostDto> getAllPostsSortedByDateAsc() {
        return postService.getAllPostsSortedByDateAsc();
    }

}
