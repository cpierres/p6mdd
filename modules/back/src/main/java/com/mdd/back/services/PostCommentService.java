package com.mdd.back.services;

import com.mdd.back.entities.PostComment;
import com.mdd.back.exception.ResourceNotFoundException;
import com.mdd.back.mappers.PostCommentMapper;
import com.mdd.back.models.PostCommentDto;
import com.mdd.back.repositories.PostCommentRepository;
import com.mdd.back.repositories.PostRepository;
import com.mdd.back.repositories.UserRepository;
import com.mdd.back.services.interfaces.IPostCommentService;
import com.mdd.back.services.interfaces.IPostStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Implémentation du service responsable de la gestion des commentaires des posts.
 */
@Service
public class PostCommentService implements IPostCommentService {
    private final AuthService authService;
    private final PostCommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostCommentMapper commentMapper;
    private final IPostStatisticsService postStatisticsService;

    @Autowired
    public PostCommentService(AuthService authService,
                              PostCommentRepository commentRepository,
                              PostRepository postRepository,
                              UserRepository userRepository,
                              PostCommentMapper commentMapper,
                              IPostStatisticsService postStatisticsService) {
        this.authService = authService;
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentMapper = commentMapper;
        this.postStatisticsService = postStatisticsService;
    }

    @Override
    public Mono<PostCommentDto> createComment(PostCommentDto commentDto) {
        return authService.getAuthenticatedUserId()
                .flatMap(userId -> {
                    PostComment comment = commentMapper.commentDtoToComment(commentDto);
                    comment.setCreatedBy(userId);

                    return postRepository.findById(commentDto.getPostId())
                            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Article/Post non trouvé")))
                            .flatMap(post -> commentRepository.save(comment)
                                    .flatMap(this::enrichCommentDto)
                                    .flatMap(enrichedCommentDto ->
                                            postStatisticsService.updateAndNotifyTopicStats()
                                                    .collectList()
                                                    .then(Mono.just(enrichedCommentDto))
                                    )
                            );
                });
    }

    @Override
    public Flux<PostCommentDto> getCommentsByPostId(UUID postId) {
        return commentRepository.findAllByPostIdOrderByUpdatedAtDesc(postId)
                .flatMap(this::enrichCommentDto);
    }

    /**
     * Enrichit un CommentDto avec des informations supplémentaires comme le nom de l'utilisateur qui a créé le commentaire.
     *
     * @param comment l'entité PostComment source pour créer et enrichir un PostCommentDto
     * @return un Mono<PostCommentDto> contenant l'objet PostCommentDto enrichi
     */
    private Mono<PostCommentDto> enrichCommentDto(PostComment comment) {
        PostCommentDto dto = commentMapper.commentToCommentDto(comment);

        return userRepository.findById(comment.getCreatedBy())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Utilisateur du commentaire non trouvé")))
                .map(user -> {
                    dto.setCreatedByUsername(user.getUsername());
                    return dto;
                });
    }
}