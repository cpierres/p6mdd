package com.mdd.back.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;


/**
 * Classe responsable de l'initialisation des données dans la base de données au démarrage de l'application.
 * Cette classe insère des enregistrements prédéfinis dans la table `topics`, uniquement si ces enregistrements
 * n'existent pas déjà pour éviter les doublons (idempotent).
 * Comportement :
 * - Vérifie si un enregistrement avec un titre spécifique ("Thème 1", "Thème 2", etc.) existe déjà dans la table.
 * - Si l'enregistrement n'existe pas, il est inséré avec une description prédéfinie.
 * La méthode d'initialisation est exécutée automatiquement grâce à l'annotation `@PostConstruct`.
 * Remarque : Cette classe utilise le client réactif fourni par R2DBC.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final DatabaseClient databaseClient;

    @PostConstruct
    public void initializeData() {
        databaseClient.sql("""
            INSERT INTO topics (title, description)
            SELECT 'Thème 1', 'Description thème 1 lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce eget.'
            WHERE NOT EXISTS (SELECT 1 FROM topics WHERE title = 'Thème 1')
        """).fetch().rowsUpdated().subscribe();

        databaseClient.sql("""
            INSERT INTO topics (title, description)
            SELECT 'Thème 2', 'Description thème 2 lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer.'
            WHERE NOT EXISTS (SELECT 1 FROM topics WHERE title = 'Thème 2')
        """).fetch().rowsUpdated().subscribe();

        databaseClient.sql("""
            INSERT INTO topics (title, description)
            SELECT 'Thème 3', 'Description thème 3 lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur.'
            WHERE NOT EXISTS (SELECT 1 FROM topics WHERE title = 'Thème 3')
        """).fetch().rowsUpdated().subscribe();

        databaseClient.sql("""
            INSERT INTO topics (title, description)
            SELECT 'Thème 4', 'Description thème 4 lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque.'
            WHERE NOT EXISTS (SELECT 1 FROM topics WHERE title = 'Thème 4')
        """).fetch().rowsUpdated().subscribe();

        databaseClient.sql("""
            INSERT INTO topics (title, description)
            SELECT 'Thème 5', 'Description thème 5 lorem ipsum dolor sit amet, consectetur adipiscing elit. Maecenas.'
            WHERE NOT EXISTS (SELECT 1 FROM topics WHERE title = 'Thème 5')
        """).fetch().rowsUpdated().subscribe();
    }
}