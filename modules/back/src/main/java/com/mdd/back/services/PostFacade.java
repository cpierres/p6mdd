package com.mdd.back.services;

import com.mdd.back.mappers.PostMapper;
import com.mdd.back.models.PostCommentDto;
import com.mdd.back.models.PostDto;
import com.mdd.back.models.PostImportDto;
import com.mdd.back.models.TopicStatsDto;
import com.mdd.back.services.interfaces.IPostCommentService;
import com.mdd.back.services.interfaces.IPostService;
import com.mdd.back.services.interfaces.IPostStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
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
    private final PostMapper postMapper;
    private final FileExportService fileExportService;

    @Autowired
    public PostFacade(IPostService postService,
                      IPostCommentService postCommentService,
                      IPostStatisticsService postStatisticsService, PostMapper postMapper, FileExportService fileExportService) {
        this.postService = postService;
        this.postCommentService = postCommentService;
        this.postStatisticsService = postStatisticsService;
        this.postMapper = postMapper;
        this.fileExportService = fileExportService;
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

    /**
     * Délègue la récupération des posts d'un topic spécifique triés par thème au service spécifique.
     *
     * @param topicId L'identifiant du topic
     * @return Un Flux contenant les DTOs des posts du topic triés par thème
     */
    public Flux<PostDto> getPostsByTopicSortedByTopic(UUID topicId) {
        return postService.getPostsByTopicSortedByTopic(topicId);
    }

    /**
     * Délègue la récupération des posts d'un topic spécifique triés par auteur au service spécifique.
     *
     * @param topicId L'identifiant du topic
     * @return Un Flux contenant les DTOs des posts du topic triés par auteur
     */
    public Flux<PostDto> getPostsByTopicSortedByAuthor(UUID topicId) {
        return postService.getPostsByTopicSortedByAuthor(topicId);
    }

    /**
     * Délègue la récupération des posts d'un topic spécifique triés par date de création en ordre ascendant au service spécifique.
     *
     * @param topicId L'identifiant du topic
     * @return Un Flux contenant les DTOs des posts du topic triés par date de création
     */
    public Flux<PostDto> getPostsByTopicSortedByDateAsc(UUID topicId) {
        return postService.getPostsByTopicSortedByDateAsc(topicId);
    }

    /**
     * Exporte les posts vers un fichier JSON
     *
     * @param sortBy     Critère de tri
     * @param topicId    ID du topic (optionnel)
     * @param filename   Nom du fichier (optionnel, par défaut "posts.json")
     * @param filterType Type de filtre (optionnel, "subscribed" ou "all")
     * @return Un Mono contenant le chemin du fichier créé
     */
    public Mono<String> exportPostsToJson(String sortBy, UUID topicId, String filename, String filterType) {
        if (filename == null || filename.trim().isEmpty()) {
            filename = "posts.json";
        }

        // Utiliser le même flux que pour l'affichage des posts
        Flux<PostDto> postsFlux;

        if (topicId != null) {
            if ("topic".equals(sortBy)) {
                postsFlux = getPostsByTopicSortedByTopic(topicId);
            } else if ("author".equals(sortBy)) {
                postsFlux = getPostsByTopicSortedByAuthor(topicId);
            } else if ("date-asc".equals(sortBy)) {
                postsFlux = getPostsByTopicSortedByDateAsc(topicId);
            } else {
                postsFlux = getPostsByTopic(topicId);
            }
        } else if ("subscribed".equals(filterType)) {
            if ("topic".equals(sortBy)) {
                postsFlux = getAllPostsSubscribed()
                        .sort(Comparator.comparing(PostDto::getTopicTitle)
                                .thenComparing(PostDto::getUpdatedAt, Comparator.reverseOrder()));
            } else if ("author".equals(sortBy)) {
                postsFlux = getAllPostsSubscribed()
                        .sort(Comparator.comparing(PostDto::getCreatedByUsername)
                                .thenComparing(PostDto::getUpdatedAt, Comparator.reverseOrder()));
            } else if ("date-asc".equals(sortBy)) {
                postsFlux = getAllPostsSubscribed()
                        .sort(Comparator.comparing(PostDto::getCreatedAt));
            } else {
                postsFlux = getAllPostsSubscribed();
            }
        } else if ("all".equals(filterType) || sortBy != null) {
            if ("topic".equals(sortBy)) {
                postsFlux = getAllPostsSortedByTopic();
            } else if ("author".equals(sortBy)) {
                postsFlux = getAllPostsSortedByAuthor();
            } else if ("date-asc".equals(sortBy)) {
                postsFlux = getAllPostsSortedByDateAsc();
            } else {
                postsFlux = getAllPosts();
            }
        } else {
            // Comportement par défaut
            postsFlux = getAllPostsSubscribed();
        }

        String finalFilename = filename;
        return postsFlux
                .collectList()
                .flatMap(postDtos -> {
                    List<PostImportDto> postImportDtos = postMapper.postDtoListToPostImportDtoList(postDtos);
                    return fileExportService.exportToJson(
                            postImportDtos,
                            finalFilename,
                            "src/main/resources/demo-data"
                    );
                });
    }
}
