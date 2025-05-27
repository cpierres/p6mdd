package com.mdd.back.config.security;

import org.springframework.http.HttpCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Convertisseur qui extrait le token JWT du cookie "auth-token" et le transforme
 * en un objet BearerTokenAuthenticationToken que Spring Security peut utiliser pour l'authentification.
 * 
 * Cette classe est utilisée par la configuration de sécurité pour permettre l'authentification
 * via des cookies HttpOnly au lieu de l'en-tête Authorization.
 */
@Component
public class ServerCookieAuthenticationConverter implements ServerAuthenticationConverter {

    private static final String AUTH_COOKIE_NAME = "auth-token";

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getCookies().getFirst(AUTH_COOKIE_NAME))
                .map(HttpCookie::getValue)
                .filter(token -> !token.isEmpty())
                .map(BearerTokenAuthenticationToken::new);
    }
}