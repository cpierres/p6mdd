package com.mdd.back.controller;

import com.mdd.back.config.ApiResponseExamples;
import com.mdd.back.models.*;
import com.mdd.back.models.ResponseDetails;
import com.mdd.back.utils.context.RequestIdContext;
import com.mdd.back.services.PostFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@Tag(name = "post-controller", description = "API pour la gestion des posts (articles) et commentaires")
public class PostController {
    private final PostFacade postFacade;

    public PostController(PostFacade postFacade) {
        this.postFacade = postFacade;
    }

    @PostMapping("/posts")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Créer un post (article)",
            description = "Crée un post avec l'utilisateur authentifié comme auteur",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Post créé avec succès",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostDto.class))),
                    @ApiResponse(responseCode = "400", description = "Données de requête invalides",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseDetails.class))),
                    @ApiResponse(responseCode = "401", description = "L'utilisateur n'est pas authentifié ou non autorisé.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ApiResult.class, example = ApiResponseExamples.UNAUTHORIZED_EXAMPLE))),
                    @ApiResponse(responseCode = "500", description = "Erreur interne du serveur",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseDetails.class,
                                            description = "Pour les erreurs 500, seuls les attributs 'message' et 'severity' sont utilisés, l'attribut 'fieldErrors' n'est pas inclus.")))
            }
    )
    public Mono<ResponseEntity<ApiResult<PostDto>>> createPost(@Valid @RequestBody PostDto postDto, ServerWebExchange exchange) {
        // Récupérer l'ID de requête depuis les attributs d'échange
        String requestId = (String) exchange.getAttributes().get(RequestIdContext.REQUEST_ID_KEY);

        return postFacade.createPost(postDto)
                .map(post -> {
                    ApiResult<PostDto> apiResult = new ApiResult<>(
                            post,
                            "Post créé avec succès.",
                            HttpStatus.CREATED.value(),
                            requestId
                    );
                    return ResponseEntity.status(HttpStatus.CREATED).body(apiResult);
                });
    }

    @GetMapping("/topics/stats")
    @Operation(
            summary = "Liste des topics avec statistiques",
            description = "Récupère la liste des topics (thèmes) avec leurs statistiques de popularité, notamment le nombre de publications (posts) et de commentaires associés. Ce point d'accès permet de consulter les tendances actuelles des thèmes (topics) en termes de popularité.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Statistiques des topics récupérées avec succès",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TopicStatsDto.class)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "L'utilisateur n'est pas authentifié ou non autorisé.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ApiResult.class, example = ApiResponseExamples.UNAUTHORIZED_EXAMPLE))),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Erreur interne du serveur",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseDetails.class,
                                            description = "Pour les erreurs 500, seuls les attributs 'message' et 'severity' sont utilisés, l'attribut 'fieldErrors' n'est pas inclus."))
                    )
            }
    )
    public Flux<TopicStatsDto> getTopicStats() {
        return postFacade.getTopicStats();
    }

    @GetMapping("/posts")
    @Operation(
            summary = "Récupérer tous les posts",
            description = "Récupère tous les posts avec un tri personnalisable et optionnel par topic ou auteur. "
                    + "Par défaut, les résultats sont filtrés pour afficher les posts des topics auxquels l'utilisateur est abonné, triés par date décroissante.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Liste des posts récupérée avec succès.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PostDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Requête invalide, par exemple, si le paramètre UUID est mal formé.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseDetails.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Erreur interne du serveur.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseDetails.class,
                                            description = "Pour les erreurs 500, seuls les attributs 'message' et 'severity' sont utilisés, l'attribut 'fieldErrors' n'est pas inclus."))
                    )
            }
    )
    public Flux<PostDto> getAllPosts(
            @RequestParam(required = false)
            @Schema(description = "Tri des résultats : `topic` pour trier par topic, `author` pour trier par auteur, `all` pour tous les posts, null pour le filtre par défaut (posts des topics auxquels l'utilisateur est abonné).",
                    example = "topic")
            String sortBy,
            @RequestParam(required = false)
            @Schema(description = "Filtrage par l'identifiant d'un topic (UUID). Si spécifié, seuls les posts appartenant à ce topic sont retournés.",
                    example = "d1a27f64-403d-4c27-9fb7-1b54168a546d")
            UUID topicId) {
        if (topicId != null) {
            return postFacade.getPostsByTopic(topicId);
        }

        if ("topic".equals(sortBy)) {
            return postFacade.getAllPostsSortedByTopic();
        } else if ("author".equals(sortBy)) {
            return postFacade.getAllPostsSortedByAuthor();
        } else if ("all".equals(sortBy)) {
            return postFacade.getAllPosts();
        } else {
            return postFacade.getAllPostsSubscribed();
        }
    }

    @GetMapping("/posts/{id}")
    @Operation(
            summary = "Récupérer un post par son ID",
            description = "Récupère un post spécifique avec ses commentaires",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Post récupéré avec succès",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ApiResult.class, subTypes = PostDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Post non trouvé",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ApiResult.class, subTypes = ResponseDetails.class, example = ApiResponseExamples.NOT_FOUND_EXAMPLE))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Erreur interne du serveur",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ApiResult.class, subTypes = ResponseDetails.class, example = ApiResponseExamples.INTERNAL_SERVER_ERROR_EXAMPLE))
                    )
            }
    )
    public Mono<ResponseEntity<ApiResult<PostDto>>> getPostById(@PathVariable UUID id, ServerWebExchange exchange) {
        // Récupérer l'ID de requête depuis les attributs d'échange
        String requestId = (String) exchange.getAttributes().get(RequestIdContext.REQUEST_ID_KEY);

        return postFacade.getPostWithComments(id)
                .map(post -> {
                    ApiResult<PostDto> apiResult = new ApiResult<>(
                            post,
                            "Post récupéré avec succès.",
                            HttpStatus.OK.value(),
                            requestId
                    );
                    return ResponseEntity.ok(apiResult);
                })
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResult<>(
                                null,
                                "Post non trouvé.",
                                HttpStatus.NOT_FOUND.value(),
                                requestId
                        ))
                );
    }

    @PostMapping("/comments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Créer un commentaire",
            description = "Ajoute un commentaire à un post existant",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Commentaire créé avec succès",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = PostCommentDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Données de requête invalides",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseDetails.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "L'utilisateur n'est pas authentifié ou non autorisé.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ApiResult.class, example = ApiResponseExamples.UNAUTHORIZED_EXAMPLE))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Post non trouvé",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ApiResult.class, example = ApiResponseExamples.NOT_FOUND_EXAMPLE))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Erreur interne du serveur",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ApiResult.class, subTypes = ResponseDetails.class, example = ApiResponseExamples.INTERNAL_SERVER_ERROR_EXAMPLE,
                                            description = "Pour les erreurs 500, seuls les attributs 'message' et 'severity' sont utilisés, l'attribut 'fieldErrors' n'est pas inclus."))
                    )
            }
    )
    public Mono<ResponseEntity<ApiResult<PostCommentDto>>> createComment(@Valid @RequestBody PostCommentDto commentDto, ServerWebExchange exchange) {
        // Récupérer l'ID de requête depuis les attributs d'échange
        String requestId = (String) exchange.getAttributes().get(RequestIdContext.REQUEST_ID_KEY);

        return postFacade.createComment(commentDto)
                .map(comment -> {
                    ApiResult<PostCommentDto> apiResult = new ApiResult<>(
                            comment,
                            "Commentaire créé avec succès.",
                            HttpStatus.CREATED.value(),
                            requestId
                    );
                    return ResponseEntity.status(HttpStatus.CREATED).body(apiResult);
                });
    }
}
