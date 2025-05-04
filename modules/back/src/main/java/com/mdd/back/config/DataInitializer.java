package com.mdd.back.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class DataInitializer {

    private final DatabaseClient databaseClient;

    @PostConstruct
    public void initializeData() {
        databaseClient.sql("""
                            INSERT INTO mddsocial.topics (title, description, priority_order)
                            SELECT :title, 
                                   'Spring Data JPA simplifie l''accès aux bases de données relationnelles en utilisant Java Persistence API (JPA). Il fournit une interface basée sur des repositories, réduit le code de configuration et prend en charge les fonctionnalités avancées comme les requêtes dérivées et la pagination.',  
                                   10.0
                            WHERE NOT EXISTS (SELECT 1 FROM mddsocial.topics WHERE title = :title)
                        """)
                .bind("title",
                        "Spring Data JPA")
                .fetch().rowsUpdated()
                .doOnSuccess(count -> {
                    if (count > 0) {
                        log.debug("Données Spring Data JPA insérées avec succès !");
                    } else {
                        log.debug("Aucune donnée Spring Data JPA insérée (déjà existante).");
                    }
                })
                .doOnError(error -> {
                    log.error("Erreur lors de l'insertion Spring Data JPA : " + error.getMessage());
                })
                .subscribe();

        databaseClient.sql("""
                            INSERT INTO mddsocial.topics (title, description, priority_order)
                            SELECT :title, 
                                   'Framework réactif et non-bloquant pour le développement d''applications web avec Spring. Introduit dans Spring 5, il permet de gérer des flux de données de manière asynchrone et est conçu pour exploiter efficacement les architectures de type réactif et les processeurs multi-coeurs.', 
                                   20.0
                            WHERE NOT EXISTS (SELECT 1 FROM mddsocial.topics WHERE title = :title)
                        """)
                .bind("title", "Spring WebFlux")
                .fetch().rowsUpdated()
                .doOnSuccess(count -> {
                    if (count > 0) {
                        log.debug("Données Spring WebFlux insérées avec succès !");
                    } else {
                        log.debug("Aucune donnée Spring WebFlux insérée (déjà existante).");
                    }
                })
                .doOnError(error -> {
                    log.error("Erreur lors de l'insertion Spring WebFlux : " + error.getMessage());
                })
                .subscribe();

        databaseClient.sql("""
                            INSERT INTO mddsocial.topics (title, description, priority_order)
                            SELECT :title, 
                                   'Alternative fonctionnelle au modèle basé sur les annotations dans le framework Spring WebFlux. Il permet de définir des routes et des gestionnaires de manière fluide et concise, en suivant le paradigme de la programmation fonctionnelle. Cette approche offre une flexibilité accrue pour les développeurs qui souhaitent éviter les annotations ou qui recherchent un style plus déclaratif.', 
                                   30
                            WHERE NOT EXISTS (SELECT 1 FROM mddsocial.topics WHERE title = :title)
                        """)
                .bind("title", "Spring WebFlux.fn")
                .fetch().rowsUpdated()
                .doOnSuccess(count -> {
                    if (count > 0) {
                        log.debug("Données Spring WebFlux.fn insérées avec succès !");
                    } else {
                        log.debug("Aucune donnée Spring WebFlux.fn insérée (déjà existante).");
                    }
                })
                .doOnError(error -> {
                    log.error("Erreur lors de l'insertion Spring WebFlux.fn : " + error.getMessage());
                })
                .subscribe();

        databaseClient.sql("""
                            INSERT INTO mddsocial.topics (title, description, priority_order)
                            SELECT :title, 
                                   'R2DBC (Reactive Relational Database Connectivity) est une spécification conçue pour permettre une communication réactive et non-bloquante avec les bases de données relationnelles. Contrairement aux pilotes JDBC traditionnels, R2DBC est optimisé pour les applications réactives, offrant une meilleure utilisation des ressources et une latence réduite.', 
                                   40
                            WHERE NOT EXISTS (SELECT 1 FROM mddsocial.topics WHERE title = :title)
                        """)
                .bind("title", "Databases R2DBC")
                .fetch().rowsUpdated().subscribe();

        databaseClient.sql("""
                            INSERT INTO mddsocial.topics (title, description, priority_order)
                            SELECT :title, 
                                   'Les microservices sont une architecture logicielle qui décompose les applications en services indépendants, autonomes et faiblement couplés. Chaque service est conçu pour accomplir une fonction métier spécifique et communique avec d''autres services via des interfaces légères, telles que les API REST.', 
                                   45
                            WHERE NOT EXISTS (SELECT 1 FROM mddsocial.topics WHERE title = :title)
                        """)
                .bind("title", "Microservices")
                .fetch().rowsUpdated().subscribe();

        databaseClient.sql("""
                            INSERT INTO mddsocial.topics (title, description, priority_order)
                            SELECT :title, 
                                   'Evolutions amenées par chaque nouvelle version (standalone, Signal, etc..)', 
                                   50
                            WHERE NOT EXISTS (SELECT 1 FROM mddsocial.topics WHERE title = :title)
                        """)
                .bind("title", "Angular Nouveautés")
                .fetch().rowsUpdated().subscribe();

        databaseClient.sql("""
                            INSERT INTO mddsocial.topics (title, description, priority_order)
                            SELECT :title, 
                                   'Projet OpenClassrooms : objectifs et planification des projets',
                                   60
                            WHERE NOT EXISTS (SELECT 1 FROM mddsocial.topics WHERE title = :title)
                        """)
                .bind("title", "Projets OCLR : Vue d'ensemble")
                .fetch().rowsUpdated().subscribe();

        databaseClient.sql("""
                            INSERT INTO mddsocial.topics (title, description, priority_order)
                            SELECT :title, 
                                   'Projet OpenClassrooms n°1 : objectifs et planification des projets',
                                   70
                            WHERE NOT EXISTS (SELECT 1 FROM mddsocial.topics WHERE title = :title)
                        """)
                .bind("title", "Projet OCLR 1 : Objectifs")
                .fetch().rowsUpdated().subscribe();

        databaseClient.sql("""
                            INSERT INTO mddsocial.topics (title, description, priority_order)
                            SELECT :title, 
                                   'Projet OpenClassrooms n°2 Télésport : Développement Frontend Angular Télésport (Jeux olympiques) ; backend minimaliste simulé',
                                   80
                            WHERE NOT EXISTS (SELECT 1 FROM mddsocial.topics WHERE title = :title)
                        """)
                .bind("title", "Projet OCLR 2 Télésport : Front Angular")
                .fetch().rowsUpdated().subscribe();

        databaseClient.sql("""
                            INSERT INTO mddsocial.topics (title, description, priority_order)
                            SELECT :title, 
                                   'Projet OpenClassrooms n°3 : Développement du Backend (Spring Data JPA) + sécurité token pour un Frontend Angular existant',
                                   90
                            WHERE NOT EXISTS (SELECT 1 FROM mddsocial.topics WHERE title = :title)
                        """)
                .bind("title", "Projet OCLR 3 ChâTop : Back Spring Data JPA et Securité token")
                .fetch().rowsUpdated().subscribe();

        databaseClient.sql("""
                            INSERT INTO mddsocial.topics (title, description, priority_order)
                            SELECT :title, 
                                   'Projet OpenClassrooms n°4 : Développement Frontend Angular (avec flux RSS) déployé sur dscloud.me',
                                   100
                            WHERE NOT EXISTS (SELECT 1 FROM mddsocial.topics WHERE title = :title)
                        """)
                .bind("title", "Projet OCLR 4 : Veille techno autour des architectures, de Spring et d'Angular")
                .fetch().rowsUpdated().subscribe();

        databaseClient.sql("""
                            INSERT INTO mddsocial.topics (title, description, priority_order)
                            SELECT :title, 
                                   'Projet OpenClassrooms n°5 : Développement du testing complet (unitaires, intégration, E2E) pour un Backend Spring Data JPA et un Frontend Angular existants avec sécurité token',
                                   110
                            WHERE NOT EXISTS (SELECT 1 FROM mddsocial.topics WHERE title = :title)
                        """)
                .bind("title", "Projet OCLR 5 : Tests Back et Front")
                .fetch().rowsUpdated().subscribe();

        databaseClient.sql("""
                            INSERT INTO mddsocial.topics (title, description, priority_order)
                        SELECT :title, 
                               'Projet OpenClassrooms n°6 : Développement Front et Back d''un mini réseau social dédié aux développeurs (libre choix des technos !)',
                               120
                        WHERE NOT EXISTS (SELECT 1 FROM mddsocial.topics WHERE title = :title)                        """)
                .bind("title", "Projet OCLR 6 MDD : front et back (liberté de choix des technos !)")
                .fetch().rowsUpdated().subscribe();

        databaseClient.sql("""
                            INSERT INTO mddsocial.topics (title, description, priority_order)
                        SELECT :title, 
                               'Projet OpenClassrooms n°7 : Point d''étapes',
                               130
                        WHERE NOT EXISTS (SELECT 1 FROM mddsocial.topics WHERE title = :title)                        """)
                .bind("title", "Projet OCLR 7 : Point d''étapes")
                .fetch().rowsUpdated().subscribe();

        databaseClient.sql("""
                            INSERT INTO mddsocial.topics (title, description, priority_order)
                        SELECT :title, 
                               'Projet OpenClassrooms n°8 : Audit et cahier des charges (Spéc. fonctionnelles et techniques)',
                               140
                        WHERE NOT EXISTS (SELECT 1 FROM mddsocial.topics WHERE title = :title)                        """)
                .bind("title", "Projet OCLR 8 : Audit et cahier des charges")
                .fetch().rowsUpdated().subscribe();

        databaseClient.sql("""
                            INSERT INTO mddsocial.topics (title, description, priority_order)
                        SELECT :title, 
                               'Projet OpenClassrooms n°9 : Cadrage',
                               150
                        WHERE NOT EXISTS (SELECT 1 FROM mddsocial.topics WHERE title = :title)                        """)
                .bind("title", "Projet OCLR 9 : Cadrage")
                .fetch().rowsUpdated().subscribe();

        databaseClient.sql("""
                            INSERT INTO mddsocial.topics (title, description, priority_order)
                        SELECT :title, 
                               'Projet OpenClassrooms n°10 : Intégration et CI/CD (Maven, Jenkins, Docker)',
                               160
                        WHERE NOT EXISTS (SELECT 1 FROM mddsocial.topics WHERE title = :title)                        """)
                .bind("title", "Projet OCLR 10 : CI/CD")
                .fetch().rowsUpdated().subscribe();

        databaseClient.sql("""
                            INSERT INTO mddsocial.topics (title, description, priority_order)
                        SELECT :title, 
                               'Projet OpenClassrooms n°11 : Encadrer',
                               170
                        WHERE NOT EXISTS (SELECT 1 FROM mddsocial.topics WHERE title = :title)                        """)
                .bind("title", "Projet OCLR 11 : Encadrer")
                .fetch().rowsUpdated().subscribe();

        databaseClient.sql("""
                            INSERT INTO mddsocial.topics (title, description, priority_order)
                        SELECT :title, 'Projet OpenClassrooms n°12 : Planification',180
                        WHERE NOT EXISTS (SELECT 1 FROM mddsocial.topics WHERE title = :title)                        """)
                .bind("title", "Projet OCLR 12 : Planification")
                .fetch().rowsUpdated().subscribe();

        databaseClient.sql("""
                            INSERT INTO mddsocial.topics (title, description, priority_order)
                        SELECT :title, 
                               'Projet OpenClassrooms n°13 : Proposition d''architecture et de solutions',
                               190
                        WHERE NOT EXISTS (SELECT 1 FROM mddsocial.topics WHERE title = :title)                        """)
                .bind("title", "Projet OCLR 13 : Architecture et solutions")
                .fetch().rowsUpdated().subscribe();

    }
}