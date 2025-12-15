package com.mdd.back.config.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Initialiseur défensif pour corriger une mauvaise configuration fréquente:
 * l’utilisation de la propriété plate "spring.security.oauth2.resourceserver.jwt"
 * (chaîne) au lieu de la sous‑propriété attendue "spring.security.oauth2.resourceserver.jwt.jwk-set-uri".
 *
 * Contexte: Spring Boot 4 essaie de binder l’objet Jwt à partir du préfixe
 * "spring.security.oauth2.resourceserver.jwt". Si cette clé est renseignée avec
 * une simple chaîne (souvent une URL), le bind échoue avec un ConverterNotFoundException.
 *
 * Stratégie: avant le démarrage du contexte, on inspecte les PropertySources. Si la
 * clé fautive est trouvée avec une valeur de type chaîne correspondant vraisemblablement
 * à une URL, on transfère cette valeur vers "...jwt.jwk-set-uri" puis on supprime la
 * clé fautive afin de laisser l’auto‑config fonctionner normalement.
 */
public class SafeJwtPropertyInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger log = LoggerFactory.getLogger(SafeJwtPropertyInitializer.class);

    private static final String KEY_FLAT = "spring.security.oauth2.resourceserver.jwt";
    private static final String KEY_JWK = "spring.security.oauth2.resourceserver.jwt.jwk-set-uri";

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment env = applicationContext.getEnvironment();
        MutablePropertySources sources = env.getPropertySources();

        // On parcourt les PropertySources pour détecter une définition plate fautive
        for (Iterator<PropertySource<?>> it = sources.iterator(); it.hasNext(); ) {
            PropertySource<?> ps = it.next();
            Object val = ps.getProperty(KEY_FLAT);
            if (val instanceof String s && isProbablyUrl(s)) {
                log.warn("[Sécurité] Propriété '{}' détectée avec une chaîne. Correction automatique → '{}' = {}", KEY_FLAT, KEY_JWK, s);

                // 1) Injecter/écraser jwk-set-uri dans un PropertySource en tête
                Map<String, Object> override = new HashMap<>();
                override.put(KEY_JWK, s);
                MapPropertySource mps = new MapPropertySource("safe-jwt-fix", override);
                sources.addFirst(mps);

                // 2) Supprimer la clé fautive du PropertySource d’origine si modifiable
                if (ps instanceof MapPropertySource mpsOrig) {
                    Map<String, Object> map = new HashMap<>(mpsOrig.getSource());
                    map.remove(KEY_FLAT);
                    MapPropertySource cleaned = new MapPropertySource(mpsOrig.getName(), map);
                    sources.replace(mpsOrig.getName(), cleaned);
                } else {
                    // Si on ne peut pas modifier, on laisse tel quel; la présence du mps en tête suffira
                    log.debug("PropertySource '{}' non modifiable, la surcharge en tête sera utilisée.", ps.getName());
                }
                return; // correction appliquée, on peut sortir
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
}
