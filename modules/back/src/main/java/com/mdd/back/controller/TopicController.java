package com.mdd.back.controller;

import com.mdd.back.config.ApiResponseExamples;
import com.mdd.back.models.ApiResult;
import com.mdd.back.models.ResponseDetails;
import com.mdd.back.models.TopicDto;
import com.mdd.back.models.TopicSubscribedForAuthUserDto;
import com.mdd.back.services.TopicService;
import com.mdd.back.utils.context.RequestIdContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
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
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des topics récupérée avec succès.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ApiResult.class, subTypes = {TopicDto.class},
                                    example = """
                                            {
                                              "message": "Liste des topics récupérée avec succès.",
                                              "status": 200,
                                              "data": [
                                                {
                                                  "id": "ab12cd34-ef56-78gh-ij90-klmnopqrstuv",
                                                  "title": "Introduction à Spring",
                                                  "description": "Un guide complet sur l'utilisation du Framework Spring pour le développement d'applications Java."
                                                },
                                                {
                                                  "id": "wxyz1234-5678-abcd-efgh-ijklmnopqrst",
                                                  "title": "Découverte de Jakarta EE",
                                                  "description": "Tutoriel sur l'utilisation des fonctionnalités de Jakarta EE pour les applications d'entreprise."
                                                }
                                              ],
                                              "timestamp": "2025-05-05T14:30:00Z",
                                              "requestId": "123e4567-e89b-12d3-a456-426614174000"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "204", description = "Aucun topic disponible.",
                    content = @Content()),
            @ApiResponse(responseCode = "401", description = "L'utilisateur n'est pas authentifié ou non autorisé.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class, example = ApiResponseExamples.UNAUTHORIZED_EXAMPLE))),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
    })
    @GetMapping
    public Mono<ResponseEntity<ApiResult<List<TopicDto>>>> getAllTopics(ServerWebExchange exchange) {
        // Récupérer l'ID de requête depuis les attributs d'échange
        String requestId = (String) exchange.getAttributes().get(RequestIdContext.REQUEST_ID_KEY);

        return topicService.getAllTopics()
                .collectList() // Convertir Flux<TopicDto> en List<TopicDto>
                .map(topics -> {
                    ApiResult<List<TopicDto>> apiResult = new ApiResult<>(
                            topics,
                            "Liste des topics récupérée avec succès.",
                            HttpStatus.OK.value(),
                            requestId
                    );
                    return ResponseEntity.ok(apiResult);
                }) // Retourner une réponse 200 avec le corps
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body(new ApiResult<>(
                                null,
                                "Aucun topic disponible.",
                                HttpStatus.NO_CONTENT.value(),//204
                                requestId
                        ))
                );
    }

    @Operation(
            summary = "Abonner l'utilisateur authentifié à un topic (thème)",
            description = "Abonner l'utilisateur authentifié à un topic donné en spécifiant l'ID de ce topic.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utilisateur abonné avec succès du topic",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class,
                                    example = """
                                            {
                                                "message": "Utilisateur abonné avec succès.",
                                                "status": 200,
                                                "data": null,
                                                "timestamp": "2025-05-05T14:25:34.726938Z",
                                                "requestId": "60f7396f-df28-4b64-888e-a1932adfe12a"
                                            }
                                            """))),
            @ApiResponse(responseCode = "400", description = "Topic non trouvé.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class,
                                    example = """
                                            {
                                              "message": "Requête invalide",
                                              "status": 400,
                                              "data": {
                                                "message": "Le topic avec l'identifiant fourni n'existe pas.",
                                                "severity": "ERROR"
                                              },
                                              "timestamp": "2025-05-05T14:25:34.726938Z",
                                              "requestId": "60f7396f-df28-4b64-888e-a1932adfe12a"
                                            }
                                            """))),
            @ApiResponse(responseCode = "401", description = "L'utilisateur n'est pas authentifié ou non autorisé.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class, example = ApiResponseExamples.UNAUTHORIZED_EXAMPLE)))
    })
    @PostMapping("/{topicId}/subscribe")
    public Mono<ResponseEntity<ApiResult<ResponseDetails>>> subscribeAuthenticatedUser(@PathVariable UUID topicId, ServerWebExchange exchange) {
        // Récupérer l'ID de requête depuis les attributs d'échange
        String requestId = (String) exchange.getAttributes().get(RequestIdContext.REQUEST_ID_KEY);

        return topicService.subscribeAuthenticatedUserToTopic(topicId)
                .then(Mono.just(ResponseEntity.ok(
                        new ApiResult<>(
                                null,
                                "Utilisateur abonné avec succès au topic.",
                                HttpStatus.OK.value(),
                                requestId
                        )
                )));
    }

    @Operation(
            summary = "Désabonner l'utilisateur authentifié à un topic (thème)",
            description = "Cet endpoint permet de désabonner l'utilisateur authentifié à un topic donné en spécifiant l'ID du topic.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "L'utilisateur a été désabonné avec succès du topic (pas de contenu)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class,
                                    example = """
                                            {
                                                "message": "Utilisateur désabonné avec succès.",
                                                "status": 200,
                                                "data": null,
                                                "timestamp": "2025-05-05T14:25:34.726938Z",
                                                "requestId": "60f7396f-df28-4b64-888e-a1932adfe12a"
                                            }
                                            """))),
            @ApiResponse(responseCode = "404", description = "Le topic ou l'abonnement utilisateur n'a pas été trouvé.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class, example = ApiResponseExamples.NOT_FOUND_EXAMPLE))),
            @ApiResponse(responseCode = "401", description = "L'utilisateur n'est pas authentifié ou non autorisé.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class, example = ApiResponseExamples.UNAUTHORIZED_EXAMPLE)))
    })
    @DeleteMapping("/{topicId}/unsubscribe")
    public Mono<ResponseEntity<ApiResult<ResponseDetails>>> unsubscribeAuthenticatedUser(@PathVariable UUID topicId, ServerWebExchange exchange) {
        // Récupérer l'ID de requête depuis les attributs d'échange
        String requestId = (String) exchange.getAttributes().get(RequestIdContext.REQUEST_ID_KEY);

        return topicService.unsubscribeAuthenticatedUserFromTopic(topicId)
                .then(Mono.just(ResponseEntity.ok(
                        new ApiResult<>(
                                null,
                                "Utilisateur désabonné du topic avec succès.",
                                HttpStatus.OK.value(),
                                requestId
                        )
                )));
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
                            schema = @Schema(
                                    implementation = ApiResult.class,
                                    example = """
                                            {
                                              "message": "Liste des topics avec statut d'abonnement récupérée avec succès.",
                                              "status": 200,
                                              "data": [
                                                {
                                                  "id": "fd41eda6-d869-4fb9-b77b-f41599fa670d",
                                                  "title": "Spring WebFlux",
                                                  "description": "Framework réactif et non-bloquant pour le développement d'applications web.",
                                                  "subscribed": true,
                                                  "countPosts": 42,
                                                  "countComments": 76,
                                                  "priorityOrder": 1.5
                                                },
                                                {
                                                  "id": "0c0a7f16-2502-411a-8bb4-ba4a1d597c88",
                                                  "title": "Spring Data JPA",
                                                  "description": "Simplifie l'accès aux bases de données relationnelles.",
                                                  "subscribed": false,
                                                  "countPosts": 12,
                                                  "countComments": 8,
                                                  "priorityOrder": 2.0
                                                }
                                              ],
                                              "timestamp": "2025-05-05T18:28:17.507305200Z",
                                              "requestId": "688c69a9-d06f-4867-b609-2180cc7657bb"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "204", description = "Aucun topic ou abonnement trouvé."),
            @ApiResponse(responseCode = "401", description = "L'utilisateur n'est pas authentifié ou non autorisé.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class, example = ApiResponseExamples.UNAUTHORIZED_EXAMPLE)))
    })
    @GetMapping("/with-subscription-status")
    public Mono<ResponseEntity<ApiResult<List<TopicSubscribedForAuthUserDto>>>> getAllTopicsWithSubscriptionStatus(ServerWebExchange exchange) {
        // Récupérer l'ID de requête depuis les attributs d'échange
        String requestId = (String) exchange.getAttributes().get(RequestIdContext.REQUEST_ID_KEY);

        return topicService.getAllTopicsWithAuthUserSubscription()
                .collectList()
                .map(topics -> {
                    ApiResult<List<TopicSubscribedForAuthUserDto>> apiResult = new ApiResult<>(
                            topics,
                            "Liste des topics avec statut d'abonnement récupérée avec succès.",
                            HttpStatus.OK.value(),
                            requestId
                    );
                    return ResponseEntity.ok(apiResult);
                })
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body(new ApiResult<>(
                                null,
                                "Aucun topic ou abonnement trouvé.",
                                HttpStatus.NO_CONTENT.value(),
                                requestId
                        ))
                );
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
                            schema = @Schema(implementation = ApiResult.class, subTypes = {TopicDto.class}))
            ),
//            @ApiResponse(
//                    responseCode = "404",
//                    description = "Topic non trouvé.",
//                    content = @Content(mediaType = "application/json",
//                            schema = @Schema(implementation = ApiResult.class))
//            ),
            @ApiResponse(responseCode = "401", description = "L'utilisateur n'est pas authentifié ou non autorisé.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class, example = ApiResponseExamples.UNAUTHORIZED_EXAMPLE))),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
    })
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResult<TopicDto>>> getTopicById(@PathVariable UUID id, ServerWebExchange exchange) {
        // Récupérer l'ID de requête depuis les attributs d'échange
        String requestId = (String) exchange.getAttributes().get(RequestIdContext.REQUEST_ID_KEY);

        return topicService.getTopicById(id)
                .map(topic -> {
                    ApiResult<TopicDto> apiResult = new ApiResult<>(
                            topic,
                            "Topic trouvé avec succès.",
                            HttpStatus.OK.value(),
                            requestId
                    );
                    return ResponseEntity.ok(apiResult);
                })
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResult<>(
                                null,
                                "Topic non trouvé.",
                                HttpStatus.NOT_FOUND.value(),
                                requestId
                        ))
                );
    }
}
