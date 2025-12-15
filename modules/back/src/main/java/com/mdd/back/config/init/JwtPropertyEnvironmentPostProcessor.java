package com.mdd.back.config.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * EnvironmentPostProcessor exécuté TÔT (avant le binding des propriétés) pour corriger
 * une mauvaise configuration fréquente avec Spring Security Resource Server en Boot 4:
 * la clé plate "spring.security.oauth2.resourceserver.jwt" renseignée comme une chaîne
 * (souvent une URL) au lieu de la sous-propriété attendue
 * "spring.security.oauth2.resourceserver.jwt.jwk-set-uri".
 *
 * Cette classe supprime la clé plate fautive si présente et injecte la valeur dans
 * la bonne propriété `...jwk-set-uri`, en ajoutant un PropertySource prioritaire.
 */
public class JwtPropertyEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtPropertyEnvironmentPostProcessor.class);

    private static final String KEY_FLAT = "spring.security.oauth2.resourceserver.jwt";
    private static final String KEY_JWK = "spring.security.oauth2.resourceserver.jwt.jwk-set-uri";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        MutablePropertySources sources = environment.getPropertySources();

        for (Iterator<PropertySource<?>> it = sources.iterator(); it.hasNext(); ) {
            PropertySource<?> ps = it.next();
            Object val = ps.getProperty(KEY_FLAT);
            if (val instanceof String s && isProbablyUrl(s)) {
                // 1) Supprimer la clé fautive du PropertySource courant si possible
                if (ps instanceof MapPropertySource mps) {
                    Map<String, Object> copy = new HashMap<>(mps.getSource());
                    copy.remove(KEY_FLAT);
                    sources.replace(mps.getName(), new MapPropertySource(mps.getName(), copy));
                } else {
                    log.debug("PropertySource '{}' non modifiable, on ajoutera une surcharge prioritaire.", ps.getName());
                }

                // 2) Injecter la valeur correcte dans un PropertySource en tête (prioritaire)
                Map<String, Object> override = new HashMap<>();
                override.put(KEY_JWK, s);
                sources.addFirst(new MapPropertySource("jwt-flat-to-jwk-fix", override));

                log.warn("[Sécurité] Clé plate '{}' détectée et réécrite automatiquement → '{}' = {} (source: {})",
                        KEY_FLAT, KEY_JWK, s, ps.getName());
                return; // correction appliquée une fois
            }
        }
    }

    private boolean isProbablyUrl(String s) {
        try {
            URI u = URI.create(s.trim());
            return u.getScheme() != null && u.getHost() != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int getOrder() {
        // Exécuter tôt, mais laisser la place aux post-processeurs de Boot si nécessaire
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }
}
