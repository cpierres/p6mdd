package com.mdd.back.services.interfaces;

import com.mdd.back.models.TopicStatsDto;
import reactor.core.publisher.Flux;

/**
 * Service responsable du calcul et de la gestion des statistiques des posts.
 */
public interface IPostStatisticsService {
    /**
     * Récupère les statistiques des topics (nombre de posts et de commentaires).
     * @return Un Flux contenant les statistiques des topics, triées par popularité
     */
    Flux<TopicStatsDto> getTopicStats();

    /**
     * Met à jour les statistiques des topics et notifie les abonnés.
     * Cette méthode est appelée après la création d'un post ou d'un commentaire.
     * @return Un Flux contenant les statistiques mises à jour
     */
    Flux<TopicStatsDto> updateAndNotifyTopicStats();
}