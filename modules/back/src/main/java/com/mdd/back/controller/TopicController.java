package com.mdd.back.controller;

import com.mdd.back.models.TopicDto;
import com.mdd.back.models.TopicSubscribedForAuthUserDto;
import com.mdd.back.services.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "topic-controller",
        description = """
                Permet de gérer la liste des Topics (Thèmes), l'abonnement, le désabonnement de l'utilisateur authentifié.
                """
)
@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @Operation(
            summary = "Obtenir tous les topics",
            description = "Permet de récupérer la liste de tous les topics disponibles (api publique)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des topics récupérée avec succès.",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = TopicDto.class)))),
            @ApiResponse(responseCode = "204", description = "Aucun topic disponible."),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur.")
    })
    @GetMapping
    public Mono<ResponseEntity<List<TopicDto>>> getAllTopics() {
        return topicService.getAllTopics()
                .collectList() // Convertir Flux<TopicDto> en List<TopicDto>
                .map(ResponseEntity::ok) // Retourner une réponse 200 avec le corps
                .defaultIfEmpty(ResponseEntity.noContent().build());
    }

    @Operation(
            summary = "Abonner l'utilisateur authentifié à un topic (thème)",
            description = "Abonner l'utilisateur authentifié à un topic donné en spécifiant l'ID de ce topic.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Utilisateur abonné avec succès du topic (pas de contenu)"),
            @ApiResponse(responseCode = "404", description = "Topic non trouvé."),
            @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié ou non autorisé.")
    })
    @PostMapping("/{topicId}/subscribe")
    public Mono<ResponseEntity<Void>> subscribeAuthenticatedUser(@PathVariable UUID topicId) {
        return topicService.subscribeAuthenticatedUserToTopic(topicId)
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    @Operation(
            summary = "Abonner l'utilisateur authentifié à un topic (thème)",
            description = "Cet endpoint permet de désabonner l'utilisateur authentifié à un topic donné en spécifiant l'ID du topic.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "L'utilisateur a été désabonné avec succès du topic (pas de contenu)"),
            @ApiResponse(responseCode = "404", description = "Le topic ou l'abonnement utilisateur n'a pas été trouvé."),
            @ApiResponse(responseCode = "401", description = "L'utilisateur n'est pas authentifié ou non autorisé.")
    })
    @DeleteMapping("/{topicId}/unsubscribe")
    public Mono<ResponseEntity<Void>> unsubscribeAuthenticatedUser(@PathVariable UUID topicId) {
        return topicService.unsubscribeAuthenticatedUserFromTopic(topicId)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    // Obtenir les topics avec le flag d'abonnement pour l'utilisateur authentifié
    @Operation(
            summary = "Obtenir les topics avec le flag d'abonnement pour l'utilisateur authentifié",
            description = "Récupère la liste de tous les topics et indique si l'utilisateur authentifié est abonné ou non à chacun.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des topics avec l'état d'abonnement pour l'utilisateur authentifié.",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = TopicSubscribedForAuthUserDto.class)))
            ),
            @ApiResponse(responseCode = "204", description = "Aucun topic ou abonnement trouvé."),
            @ApiResponse(responseCode = "401", description = "L'utilisateur n'est pas authentifié ou autorisé.")
    })
    @GetMapping("/with-subscription-status")
    public Mono<ResponseEntity<List<TopicSubscribedForAuthUserDto>>> getAllTopicsWithSubscriptionStatus() {
        return topicService.getAllTopicsWithAuthUserSubscription()
                .collectList()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.noContent().build());
    }

    @Operation(
            summary = "Obtenir un topic par son identifiant",
            description = "Récupère les détails d'un topic spécifique en utilisant son identifiant UUID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Topic trouvé avec succès.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TopicDto.class))
            ),
            @ApiResponse(
                    responseCode = "404", 
                    description = "Topic non trouvé.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500", 
                    description = "Erreur interne du serveur.",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    public Mono<ResponseEntity<TopicDto>> getTopicById(@PathVariable UUID id) {
        return topicService.getTopicById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
