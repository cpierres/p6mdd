package com.mdd.auth.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Ajoute un requestId dans les attributs de l'échange pour alimenter ApiResult.
 */
@Component
public class RequestIdFilter implements WebFilter {

    public static final String REQUEST_ID_KEY = "requestId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestId = exchange.getRequest().getHeaders().getFirst("X-Request-Id");
        if (requestId == null || requestId.trim().isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }
        exchange.getAttributes().put(REQUEST_ID_KEY, requestId);
        return chain.filter(exchange);
    }
}
