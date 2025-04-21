package com.mdd.back.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Permettra de gérer la liste déroulante pour choisir un Thème lors de la création d'un post par exemple.
 * On y associe aussi les statistiques du thème (topic) pour connaitre la popularité :
 * nombre de publications et de commentaires associés.
 * Ces stats ne sont pas demandées dans les spécifications.
 * Néanmoins, cette notion de popularité est un besoin très courant de ce type d'application.
 * L'objectif sera également de gérer un SSE (Server Sent Event) pour actualiser les statistiques
 * en temps réel sur la page d'accueil pour tous les utilisateurs et quelque soit leur browser.
 * Cela permettra de démontrer un exemple de l'intérêt du choix de WebFlux particulièrement adapté pour gérer
 * des SSE avec montée en charge efficace.
 * Attributs :
 * - `id` : Identifiant unique du topic.
 * - `title` : Titre du sujet.
 * - `countPosts` : Nombre total de publications associées.
 * - `countComments` : Nombre total de commentaires associéss.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicStatsDto {
    private UUID id;
    private String title;
    private Long countPosts;
    private Long countComments;
}