package com.mdd.back.services;

import com.mdd.back.entities.Post;
import com.mdd.back.entities.Topic;
import com.mdd.back.entities.User;
import com.mdd.back.exception.ResourceNotFoundException;
import com.mdd.back.mappers.PostMapper;
import com.mdd.back.models.PostDto;
import com.mdd.back.models.TopicStatsDto;
import com.mdd.back.repositories.PostCommentRepository;
import com.mdd.back.repositories.PostRepository;
import com.mdd.back.repositories.TopicRepository;
import com.mdd.back.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.UUID;

@Service
@Slf4j
public class PostService {
    private final AuthService authService;
    private final PostRepository postRepository;
    private final TopicRepository topicRepository;
    private final PostMapper postMapper;
    private final UserRepository userRepository;
    private final PostCommentRepository commentRepository;
    //private final TopicStatsEmitter topicStatsEmitter;
    private final TopicStatsNotifier topicStatsNotifier;

    @Autowired
    public PostService(AuthService authService,
                       PostRepository postRepository,
                       TopicRepository topicRepository,
                       PostMapper postMapper,
                       UserRepository userRepository,
                       PostCommentRepository commentRepository,
                       //TopicStatsEmitter topicStatsEmitter
                       TopicStatsNotifier topicStatsNotifier
    ) {
        this.authService = authService;
        this.postRepository = postRepository;
        this.topicRepository = topicRepository;
        this.postMapper = postMapper;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        //this.topicStatsEmitter = topicStatsEmitter;
        this.topicStatsNotifier = topicStatsNotifier;
    }

    /**
     * Créer un Post
     *
     * @param postDto
     * @return
     */
    //V1 : pb de dépendance circulaire
    //    public Mono<PostDto> createPost(PostDto postDto) {
    //        return authService.getAuthenticatedUserId()
    //                .flatMap(userId -> {
    //                    Post post = postMapper.postDtoToPost(postDto);
    //                    post.setCreatedBy(userId);
    //
    //                    return topicRepository.findById(postDto.getTopicId())
    //                            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Topic non trouvé")))
    //                            .flatMap(topic -> postRepository.save(post))
    //                            .flatMap(this::enrichPostDto)
    //                            .doOnSuccess(enrichedPostDto -> topicStatsEmitter.updateTopicStats());//stats réactives
    //                });
    //    }

    //V2 : dépend de interface au lieu de injection auto mais subscribe déconseillé dans le contexte de SpringWebFlux
    //    public Mono<PostDto> createPost(PostDto postDto) {
    //        return authService.getAuthenticatedUserId()
    //                .flatMap(userId -> {
    //                    Post post = postMapper.postDtoToPost(postDto);
    //                    post.setCreatedBy(userId);
    //
    //                    return topicRepository.findById(postDto.getTopicId())
    //                            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Topic non trouvé")))
    //                            .flatMap(topic -> postRepository.save(post))
    //                            .flatMap(this::enrichPostDto)
    //                            //.doOnSuccess(enrichedPostDto -> topicStatsEmitter.updateTopicStats());//stats réactives
    //                            .doOnSuccess(enrichedPostDto ->
    //                                    getTopicStats()
    //                                    .collectList()
    //                                            .subscribe(topicStatsNotifier::updateTopicStats)
    //                            );
    //                });
    //    }

    //V3
    public Mono<PostDto> createPost(PostDto postDto) {
        return authService.getAuthenticatedUserId()
                .flatMap(userId -> {
                    Post post = postMapper.postDtoToPost(postDto);
                    post.setCreatedBy(userId);

                    return topicRepository.findById(postDto.getTopicId())
                            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Topic non trouvé")))
                            .flatMap(topic -> postRepository.save(post))
                            .flatMap(this::enrichPostDto)
                            //.doOnSuccess(enrichedPostDto -> topicStatsEmitter.updateTopicStats());//stats réactives
                            .flatMap(enrichedPostDto ->
                                    getTopicStats()
                                            .collectList()
                                            .doOnSuccess(topicStatsNotifier::updateTopicStats)
                                            .then(Mono.just(enrichedPostDto)) // Retourne enrichedPostDto après notification
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

        //vérifier que la réf du Topic existe bien
        Mono<Topic> topicMono = topicRepository.findById(post.getTopicId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Topic lié à ce post non trouvés")));

        Mono<User> userMono = userRepository.findById(post.getCreatedBy())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Utilisateur à ce post non trouvé")));

        //déterminer si l'utilisateur connecté à la capacité de modifier (surtout utile pour update)
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

    /**
     * Récupère une liste de topics avec des statistiques associées, triée par popularité.
     * La popularité est déterminée en fonction du nombre total de publications et de commentaires.
     * Le tri est effectué en priorité sur le nombre de publications, puis sur le nombre de commentaires
     * en cas d'égalité.
     * Cette liste statistiques aura 2 usages :
     * <ul>
     * <li>liste de référence lors de la création d'un post (l'affichage de la popularité est sympa pour le user)</li>
     * <li>affichage en temps réél de la popularité des thèmes (avec un SSE)</li>
     * </ul>
     *
     * @return Flux<TopicStatsDto> contenant la liste des statistiques des topics, triés par ordre décroissant
     * de popularité (nombre de publications et commentaires).
     */
    public Flux<TopicStatsDto> getTopicStats() {
        return topicRepository.findAll()
                .flatMap(topic -> {
                    Mono<Long> postCount = postRepository.countByTopicId(topic.getId());//nb de posts pour topic
                    Mono<Long> commentCount = commentRepository.countByTopicId(topic.getId());//nb commentaires pour topic

                    //on combine plusieurs sources réactives en une seule grâce à Mono.zip
                    return Mono.zip(postCount, commentCount)
                            .map(tuple -> TopicStatsDto.builder()
                                    .id(topic.getId())
                                    .title(topic.getTitle())
                                    .countPosts(tuple.getT1())
                                    .countComments(tuple.getT2())
                                    .build());
                })
                .sort(Comparator.comparing(TopicStatsDto::getCountPosts)
                        .thenComparing(TopicStatsDto::getCountComments)
                        .reversed());
    }

    /**
     * Récupère tous les posts triés par date de mise à jour décroissante.
     * (tri en base)
     *
     * @return un flux contenant les objets PostDto enrichis.
     */
    public Flux<PostDto> getAllPosts() {
        return postRepository.findAllByOrderByUpdatedAtDesc()
                //.doOnNext(post -> log.info("Post du repository: {}", post))
                .flatMap(this::enrichPostDto);
    }

    /**
     * Récupère les posts triés par thème, puis par date de mise à jour en ordre décroissant.
     * (tri en mémoire comme peu d'enregistrements)
     *
     * @return un Flux<PostDto> contenant les posts enrichis, triés par thème (titre) en ordre croissant
     * puis par date de mise à jour en ordre décroissant.
     */
    public Flux<PostDto> getAllPostsSortedByTopic() {
        return postRepository.findAll()
                .flatMap(this::enrichPostDto)
                .sort(Comparator.comparing(PostDto::getTopicTitle)
                        .thenComparing(PostDto::getUpdatedAt, Comparator.reverseOrder()));
    }

    /**
     * Récupère les posts triés par auteur.
     * Les posts sont d'abord triés par le nom d'utilisateur de l'auteur (ordre croissant),
     * puis par la date de mise à jour (ordre décroissant).
     *
     * @return un flux de PostDto contenant les posts triés par auteur.
     */
    public Flux<PostDto> getAllPostsSortedByAuthor() {
        return postRepository.findAll()
                .flatMap(this::enrichPostDto)
                .sort(Comparator.comparing(PostDto::getCreatedByUsername)
                        .thenComparing(PostDto::getUpdatedAt, Comparator.reverseOrder()));
    }

    public Flux<PostDto> getPostsByTopic(UUID topicId) {
        return postRepository.findAllByTopicIdOrderByUpdatedAtDesc(topicId)
                .flatMap(this::enrichPostDto);
    }
}
