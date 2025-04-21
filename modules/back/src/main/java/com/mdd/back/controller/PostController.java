package com.mdd.back.controller;

import com.mdd.back.models.PostDto;
import com.mdd.back.models.TopicStatsDto;
import com.mdd.back.services.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@Tag(name = "post-controller", description = "API pour la gestion des posts (articles) et commentaires")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
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
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié ou non autorisé."),
                    @ApiResponse(responseCode = "500", description = "Erreur interne du serveur",
                            content = @Content(mediaType = "application/json"))
            }
    )
    public Mono<PostDto> createPost(@Valid @RequestBody PostDto postDto) {
        return postService.createPost(postDto);
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
                    @ApiResponse(
                            responseCode = "401",
                            description = "L'utilisateur n'est pas authentifié ou ne possède pas les droits nécessaires",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Erreur interne du serveur",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public Flux<TopicStatsDto> getTopicStats() {
        return postService.getTopicStats();
    }

}
