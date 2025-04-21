package com.mdd.back.services;

import com.mdd.back.models.TopicStatsDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;


/**
 * Service permettant d'émettre et de diffuser en continu les statistiques des topics.
 * Les statistiques des topics comprennent les informations sur leur popularité via le nombre de publications
 * et de commentaires associés. Ce service est particulièrement conçu pour gérer les besoins en temps réel,
 * comme les mises à jour dynamiques via SSE (Server-Sent Events).
 * Ce service utilise un mécanisme réactif avec `Sinks.Many` pour gérer un flux de données et permet
 * de diffuser ces mises à jour à travers des abonnés. Il agit en tant que cache léger réactif,
 * similaire au `BehaviorSubject` en RxJs.
 * Constructeur :
 * - À l'initialisation, les statistiques actuelles des topics sont chargées via `PostService` et émises dans le flux.
 * Méthodes fournies :
 * - `getTopicStatsStream()` : Retourne un flux réactif (`Flux`) qui permet aux abonnés de recevoir les mises à jour
 * des statistiques des topics en temps réel.
 * - `updateTopicStats()` : Met à jour les statistiques en récupérant les dernières données depuis `PostService`,
 * puis émet ces nouvelles statistiques dans le flux.
 */
@Service
public class TopicStatsEmitter implements TopicStatsNotifier {
    // Un Sink est comparable à un BehaviorSubject (cache léger réactif)
    private final Sinks.Many<List<TopicStatsDto>> topicStatsSink;

// KO : dépendance Autowired circulaire (remplacée par dépendance d'une interface dans PostService)
//    private final TopicService topicService;
//
//    private final PostService postService;
//
//    @Autowired
//    public TopicStatsEmitter(PostService postService) {
//        this.postService = postService;
//        this.topicStatsSink = Sinks.many().replay().latest();
//
//        // émettre stats initiales
//        updateTopicStats();
//    }
//
//    public void updateTopicStats() {
//        postService.getTopicStats()
//                .collectList()
//                .subscribe(topicStatsSink::tryEmitNext);
//    }

    public TopicStatsEmitter() {
        this.topicStatsSink = Sinks.many().replay().latest();
    }

    public Flux<List<TopicStatsDto>> getTopicStatsStream() {
        return topicStatsSink.asFlux();
    }

    @Override
    public void updateTopicStats(List<TopicStatsDto> topicStats) {
        topicStatsSink.tryEmitNext(topicStats);
    }

}
