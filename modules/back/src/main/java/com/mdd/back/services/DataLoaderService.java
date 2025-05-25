package com.mdd.back.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdd.back.models.PostImportDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataLoaderService {
    private final ObjectMapper objectMapper;

    /**
     * Charge les données depuis un fichier JSON.
     *
     * @param filePath Chemin du fichier JSON dans le classpath
     * @param typeReference Type de référence pour la désérialisation
     * @param <T> Type des objets à désérialiser
     * @return Flux d'objets désérialisés
     */
    public <T> Flux<T> loadDataFromFile(String filePath, TypeReference<List<T>> typeReference) {
        try {
            ClassPathResource resource = new ClassPathResource(filePath);
            if (!resource.exists()) {
                log.warn("Le fichier {} n'existe pas", filePath);
                return Flux.empty();
            }

            try (InputStream inputStream = resource.getInputStream()) {
                List<T> data = objectMapper.readValue(inputStream, typeReference);
                return Flux.fromIterable(data);
            }
        } catch (IOException e) {
            log.error("Erreur lors du chargement des données depuis le fichier {}: {}", filePath, e.getMessage());
            return Flux.empty();
        }
    }

    /**
     * Charge les posts depuis le fichier JSON.
     *
     * @return Flux de posts
     */
    public Flux<PostImportDto> loadPostsDemo() {
        return loadDataFromFile("demo-data/posts.json", new TypeReference<List<PostImportDto>>() {});
    }

}
