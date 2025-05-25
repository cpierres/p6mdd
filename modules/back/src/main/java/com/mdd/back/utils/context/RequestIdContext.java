package com.mdd.back.utils.context;

import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Classe utilitaire pour gérer l'identifiant de requête dans le contexte réactif.
 * <p>
 * Cette classe permet d'accéder à l'identifiant de requête depuis n'importe quel point
 * de la chaîne de traitement réactif, assurant ainsi la traçabilité des requêtes
 * à travers les différentes couches de l'application.
 */
public class RequestIdContext {

    /**
     * Clé utilisée pour stocker et récupérer l'identifiant de requête dans le contexte réactif.
     */
    public static final String REQUEST_ID_KEY = "requestId";

    /**
     * Récupère l'identifiant de requête depuis le contexte réactif.
     * <p>
     * Si aucun identifiant n'est présent dans le contexte, un nouvel UUID est généré.
     *
     * @return Un Mono contenant l'identifiant de requête
     */
    public static Mono<String> getRequestId() {
        return Mono.deferContextual(ctx ->
                ctx.hasKey(REQUEST_ID_KEY) ? Mono.just(ctx.get(REQUEST_ID_KEY)) : Mono.just(UUID.randomUUID().toString())
        );
    }
}

