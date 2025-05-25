package com.mdd.back.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileExportService {

    private final ObjectMapper objectMapper;

    /**
     * Exporte une liste d'objets vers un fichier JSON
     *
     * @param data Les données à exporter
     * @param filename Le nom du fichier (sans le chemin)
     * @param directory Le répertoire où sauvegarder le fichier
     * @return Un Mono qui émet le chemin du fichier créé
     */
    public Mono<String> exportToJson(Object data, String filename, String directory) {
        return Mono.fromCallable(() -> {
            // Créer le répertoire s'il n'existe pas
            Path directoryPath = Paths.get(directory);
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
                log.info("Répertoire créé : {}", directoryPath);
            }

            // Configurer l'ObjectMapper pour une sortie plus lisible
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

            // Créer le fichier
            Path filePath = directoryPath.resolve(filename);
            objectMapper.writeValue(filePath.toFile(), data);

            log.info("Fichier exporté avec succès : {}", filePath);
            return filePath.toString();
        });
    }
}