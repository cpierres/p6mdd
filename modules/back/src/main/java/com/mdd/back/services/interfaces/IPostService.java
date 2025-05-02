package com.mdd.back.services.interfaces;

import com.mdd.back.models.PostDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service responsable des opérations CRUD de base sur les posts.
 */
public interface IPostService {
    /**
     * Crée un nouveau post.
     * @param postDto Les informations du post à créer
     * @return Un Mono contenant le DTO du post créé
     */
    Mono<PostDto> createPost(PostDto postDto);

    /**
     * Récupère tous les posts triés par date de mise à jour décroissante.
     * @return Un Flux contenant les DTOs des posts
     */
    Flux<PostDto> getAllPosts();

    /**
     * Récupère les posts triés par thème, puis par date de mise à jour en ordre décroissant.
     * @return Un Flux contenant les DTOs des posts triés
     */
    Flux<PostDto> getAllPostsSortedByTopic();

    /**
     * Récupère les posts triés par auteur, puis par date de mise à jour en ordre décroissant.
     * @return Un Flux contenant les DTOs des posts triés
     */
    Flux<PostDto> getAllPostsSortedByAuthor();

    /**
     * Récupère les posts d'un topic spécifique.
     * @param topicId L'identifiant du topic
     * @return Un Flux contenant les DTOs des posts du topic
     */
    Flux<PostDto> getPostsByTopic(UUID topicId);

    /**
     * Récupère les posts des topics auxquels l'utilisateur authentifié est abonné.
     * @return Un Flux contenant les DTOs des posts des topics auxquels l'utilisateur est abonné
     */
    Flux<PostDto> getAllPostsSubscribed();

    /**
     * Récupère un post spécifique avec ses commentaires.
     * @param postId L'identifiant du post
     * @return Un Mono contenant le DTO du post avec ses commentaires
     */
    Mono<PostDto> getPostWithComments(UUID postId);
}