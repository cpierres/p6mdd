package com.mdd.back.config;

import com.mdd.back.utils.context.RequestIdContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import java.util.UUID;

/**
 * Filtre WebFlux qui gère l'identifiant de requête.
 * <p>
 * Ce filtre intercepte toutes les requêtes HTTP entrantes et:
 * <ul>
 *   <li>Extrait l'identifiant de requête des en-têtes HTTP ou en génère un nouveau</li>
 *   <li>Stocke cet identifiant dans les attributs d'échange</li>
 *   <li>Ajoute l'identifiant aux en-têtes de la réponse</li>
 *   <li>Propage l'identifiant dans le contexte réactif</li>
 * </ul>
 * <p>
 * L'annotation @Order avec HIGHEST_PRECEDENCE garantit que ce filtre est exécuté
 * avant tous les autres filtres, assurant ainsi que l'identifiant de requête est
 * disponible pour tous les traitements ultérieurs.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter implements WebFilter {

    /**
     * Nom de l'en-tête HTTP utilisé pour l'identifiant de requête.
     */
    private static final String REQUEST_ID_HEADER = "X-Request-ID";

    /**
     * Intercepte la requête HTTP, extrait ou génère l'identifiant de requête,
     * et le propage dans le contexte réactif.
     *
     * @param exchange L'échange serveur contenant la requête et la réponse
     * @param chain    La chaîne de filtres à exécuter
     * @return Un Mono qui complète lorsque le traitement de la requête est terminé
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Extraire l'ID de requête des en-têtes ou en générer un nouveau
        String requestId = exchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }

        // Stocker l'ID dans les attributs d'échange pour y accéder plus tard
        exchange.getAttributes().put(RequestIdContext.REQUEST_ID_KEY, requestId);

        // Ajouter l'ID à la réponse
        exchange.getResponse().getHeaders().add(REQUEST_ID_HEADER, requestId);

        // Propager l'ID dans le contexte réactif
        final String finalRequestId = requestId;
        return chain.filter(exchange)
                .contextWrite(ctx -> ctx.put(RequestIdContext.REQUEST_ID_KEY, finalRequestId));
    }
}
