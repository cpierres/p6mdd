package com.mdd.back.services;

import com.mdd.back.entities.Post;
import com.mdd.back.entities.Topic;
import com.mdd.back.entities.User;
import com.mdd.back.exception.ResourceNotFoundException;
import com.mdd.back.mappers.PostMapper;
import com.mdd.back.models.PostDto;
import com.mdd.back.repositories.PostRepository;
import com.mdd.back.repositories.TopicRepository;
import com.mdd.back.repositories.UserRepository;
import com.mdd.back.services.interfaces.IPostCommentService;
import com.mdd.back.services.interfaces.IPostService;
import com.mdd.back.services.interfaces.IPostStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.UUID;

/**
 * Implémentation du service responsable des opérations CRUD de base sur les posts.
 */
@Service
@Slf4j
public class PostService implements IPostService {
    private final AuthService authService;
    private final PostRepository postRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final PostMapper postMapper;
    private final IPostCommentService postCommentService;
    private final IPostStatisticsService postStatisticsService;

    @Autowired
    public PostService(AuthService authService,
                       PostRepository postRepository,
                       TopicRepository topicRepository,
                       UserRepository userRepository,
                       PostMapper postMapper,
                       IPostCommentService postCommentService,
                       IPostStatisticsService postStatisticsService) {
        this.authService = authService;
        this.postRepository = postRepository;
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
        this.postMapper = postMapper;
        this.postCommentService = postCommentService;
        this.postStatisticsService = postStatisticsService;
    }

    @Override
    public Mono<PostDto> createPost(PostDto postDto) {
        return authService.getAuthenticatedUserId()
                .flatMap(userId -> {
                    Post post = postMapper.postDtoToPost(postDto);
                    post.setCreatedBy(userId);

                    return topicRepository.findById(postDto.getTopicId())
                            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Topic non trouvé")))
                            .flatMap(topic -> postRepository.save(post))
                            .flatMap(this::enrichPostDto)
                            .flatMap(enrichedPostDto ->
                                    postStatisticsService.updateAndNotifyTopicStats()
                                            .collectList()
                                            .then(Mono.just(enrichedPostDto))
                            );
                });
    }

    @Override
    public Flux<PostDto> getAllPosts() {
        return postRepository.findAllByOrderByUpdatedAtDesc()
                .flatMap(this::enrichPostDto);
    }

    @Override
    public Flux<PostDto> getAllPostsSortedByTopic() {
        return postRepository.findAll()
                .flatMap(this::enrichPostDto)
                .sort(Comparator.comparing(PostDto::getTopicTitle)
                        .thenComparing(PostDto::getUpdatedAt, Comparator.reverseOrder()));
    }

    @Override
    public Flux<PostDto> getAllPostsSortedByAuthor() {
        return postRepository.findAll()
                .flatMap(this::enrichPostDto)
                .sort(Comparator.comparing(PostDto::getCreatedByUsername)
                        .thenComparing(PostDto::getUpdatedAt, Comparator.reverseOrder()));
    }

    @Override
    public Flux<PostDto> getPostsByTopic(UUID topicId) {
        return postRepository.findAllByTopicIdOrderByUpdatedAtDesc(topicId)
                .flatMap(this::enrichPostDto);
    }

    @Override
    public Flux<PostDto> getAllPostsSubscribed() {
        return authService.getAuthenticatedUserId()
                .flatMapMany(userId -> postRepository.findAllByUserSubscriptions(userId)
                        .flatMap(this::enrichPostDto))
                .switchIfEmpty(Flux.empty());
    }

    @Override
    public Mono<PostDto> getPostWithComments(UUID postId) {
        return postRepository.findById(postId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Article/Post non trouvé")))
                .flatMap(post -> {
                    Mono<PostDto> enrichedPostDto = enrichPostDto(post);
                    
                    return enrichedPostDto.flatMap(postDto ->
                            postCommentService.getCommentsByPostId(post.getId())
                                    .collectList()
                                    .map(commentList -> {
                                        postDto.setComments(commentList);
                                        return postDto;
                                    })
                    );
                });
    }

    /**
     * Enrichit un objet PostDto en ajoutant les informations supplémentaires issues des entités associées
     * comme le titre du topic, le nom de l'utilisateur qui a créé le post, et une indication si le post
     * est modifiable par l'utilisateur actuellement connecté.
     *
     * @param post l'entité Post source pour créer et enrichir un PostDto
     * @return un Mono<PostDto> contenant l'objet PostDto enrichi
     */
    private Mono<PostDto> enrichPostDto(Post post) {
        PostDto dto = postMapper.postToPostDto(post);

        Mono<Topic> topicMono = topicRepository.findById(post.getTopicId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Topic lié à ce post non trouvés")));

        Mono<User> userMono = userRepository.findById(post.getCreatedBy())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Utilisateur à ce post non trouvé")));

        Mono<Boolean> isUpdatableMono = authService.getAuthenticatedUserId()
                .map(userId -> userId.equals(post.getCreatedBy()))
                .defaultIfEmpty(false);

        return Mono.zip(topicMono, userMono, isUpdatableMono)
                .map(tuple -> {
                    dto.setTopicTitle(tuple.getT1().getTitle());
                    dto.setCreatedByUsername(tuple.getT2().getUsername());
                    dto.setUpdatable(tuple.getT3());
                    return dto;
                });
    }
}