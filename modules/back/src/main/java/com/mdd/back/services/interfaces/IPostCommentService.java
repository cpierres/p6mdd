package com.mdd.back.services.interfaces;

import com.mdd.back.models.PostCommentDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service responsable de la gestion des commentaires des posts.
 */
public interface IPostCommentService {
    /**
     * Crée un nouveau commentaire pour un post.
     * @param commentDto Les informations du commentaire à créer
     * @return Un Mono contenant le DTO du commentaire créé
     */
    Mono<PostCommentDto> createComment(PostCommentDto commentDto);

    /**
     * Récupère tous les commentaires d'un post spécifique.
     * @param postId L'identifiant du post
     * @return Un Flux contenant les DTOs des commentaires du post
     */
    Flux<PostCommentDto> getCommentsByPostId(UUID postId);
}