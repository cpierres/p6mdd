package com.mdd.back.controller;

import com.mdd.back.models.PostDto;
import com.mdd.back.models.ResponseDetails;
import com.mdd.back.models.TopicStatsDto;
import com.mdd.back.services.PostEmitter;
import com.mdd.back.services.TopicStatsEmitter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "sse-controller", description = "Gestion et consultation des statistiques et posts en temps réels (SSE : Server Side Events).")
public class SseController {
    private final TopicStatsEmitter topicStatsEmitter;
    private final PostEmitter postEmitter;

    @Autowired
    public SseController(TopicStatsEmitter topicStatsEmitter, PostEmitter postEmitter) {
        this.topicStatsEmitter = topicStatsEmitter;
        this.postEmitter = postEmitter;
    }

    @Operation(
            summary = "Obtenir un flux en temps réel des statistiques des topics",
            description = "Ce endpoint expose une connexion Server-Sent Events (SSE) permettant de recevoir les mises à jour des statistiques des topics en temps réel.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Flux en temps réel des statistiques des topics",
                            content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = TopicStatsDto.class)
                            ))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Utilisateur non authentifié ou non autorisé",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseDetails.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Erreur interne du serveur",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseDetails.class,
                                            description = "Pour les erreurs 500, seuls les attributs 'message' et 'severity' sont utilisés, l'attribut 'fieldErrors' n'est pas inclus."))
                    )
            }
    )
    @GetMapping(value = "/topics/stats/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @SecurityRequirement(name = "Bearer Authentication")
    public Flux<ServerSentEvent<List<TopicStatsDto>>> streamTopicStats() {
        //transforme le flux des statistiques en flux de ServerSentEvent<List<TopicStatsDto>>
        return topicStatsEmitter.getTopicStatsStream()
                .map(stats -> ServerSentEvent.<List<TopicStatsDto>>builder()
                        .data(stats)
                        .event("topic-stats-update")
                        .build());
    }

    @Operation(
            summary = "Obtenir un flux en temps réel des nouveaux posts",
            description = "Ce endpoint expose une connexion Server-Sent Events (SSE) permettant de recevoir les nouveaux posts en temps réel.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Flux en temps réel des nouveaux posts",
                            content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                                    schema = @Schema(implementation = PostDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Utilisateur non authentifié ou non autorisé",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseDetails.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Erreur interne du serveur",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseDetails.class))
                    )
            }
    )
    @GetMapping(value = "/posts/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @SecurityRequirement(name = "Bearer Authentication")
    public Flux<ServerSentEvent<PostDto>> streamPosts() {
        return postEmitter.getPostStream()
                .map(post -> ServerSentEvent.<PostDto>builder()
                        .data(post)
                        .event("post-created")
                        .build());
    }
}
