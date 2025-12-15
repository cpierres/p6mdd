package com.mdd.back.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration Jackson explicite pour fournir un {@link ObjectMapper} au contexte Spring.
 *
 * Pourquoi ?
 * - Après la montée de version Spring Boot, l'auto‑configuration Jackson peut
 *   ne pas exposer de bean ObjectMapper selon la composition des starters.
 * - Certains services de l'application (ex: DataLoaderService) dépendent d'un ObjectMapper.
 *
 * Stratégie :
 * - On s'appuie sur Jackson2ObjectMapperBuilder afin de bénéficier des modules
 *   automatiquement détectés par Spring Boot (JavaTime, etc.) quand ils sont présents.
 */
@Configuration
public class JacksonConfig {

    /**
     * Déclare un bean ObjectMapper unique et partagé.
     *
     * Remarque: on enregistre explicitement le module JavaTime afin de
     * sérialiser correctement les types java.time.* en ISO-8601,
     * et on désactive l'écriture au format timestamp.
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
