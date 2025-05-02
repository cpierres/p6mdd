package com.mdd.back.services;

import com.mdd.back.models.TopicStatsDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;


/**
 * Cette classe fournit un mécanisme réactif pour émettre et diffuser des statistiques d'utilisation des sujets
 * (topics) sous forme de flux. Elle permet également de gérer les mises à jour de ces statistiques.
 * Elle utilise le framework Reactor et fournit un Sink pour stocker les données et les rejouer
 * pour les nouveaux abonnés.
 *
 * Responsabilités principales :
 * - Maintenir un flux réactif des statistiques des topics.
 * - Permettre la diffusion des mises à jour des statistiques en temps réel.
 * - Assurer la gestion automatique de la rétro-pression (backpressure) pour éviter les problèmes de surcharge.
 *
 * Méthodes principales :
 * - `getTopicStatsStream` : Permet d'obtenir un flux réactif contenant la liste des statistiques des topics.
 * - `updateTopicStats` : Met à jour les statistiques des topics et les émet sur le flux.
 *
 * Cette implémentation garantit que le dernier état des statistiques est conservé et rejoué
 * pour tout nouvel abonné. Elle est particulièrement utile pour des fonctionnalités telles que
 * le SSE (Server Sent Event), permettant de diffuser les mises à jour en temps réel à plusieurs consommateurs.
 *
 * Implémente l'interface {@link TopicStatsNotifier}, permettant de notifier des mises à jour de statistiques.
 */
@Service
public class TopicStatsEmitter implements TopicStatsNotifier {
    // Un Sink est comparable à un BehaviorSubject (cache léger réactif)
    // Sert à émettre et stocker un flux.
    // Sinks.Many indique qu'il peut émettre plusieurs éléments pour les abonnés
    private final Sinks.Many<List<TopicStatsDto>> topicStatsSink;

// KO : dépendance Autowired circulaire (remplacée par dépendance d'une interface dans PostFacade)
//    private final TopicService topicService;
//
//    private final PostFacade postFacade;
//
//    @Autowired
//    public TopicStatsEmitter(PostFacade postFacade) {
//        this.postFacade = postFacade;
//        this.topicStatsSink = Sinks.many().replay().latest();
//
//        // émettre stats initiales
//        updateTopicStats();
//    }
//
//    public void updateTopicStats() {
//        postFacade.getTopicStats()
//                .collectList()
//                .subscribe(topicStatsSink::tryEmitNext);
//    }

    public TopicStatsEmitter() {
        //Conserve le dernier élément émis (latest)
        //Le rejoue pour les nouveaux abonnés (replay)
        this.topicStatsSink = Sinks.many().replay().latest();
    }

    public Flux<List<TopicStatsDto>> getTopicStatsStream() {
        //Convertit le sink en Flux standard que les consommateurs peuvent utiliser (-> SseController)
        return topicStatsSink.asFlux();
    }

    /**
     * Met à jour les statistiques des sujets (topics) avec les données fournies et les émet
     * via un mécanisme réactif. Cette méthode utilise une gestion automatique de la rétro-pression
     * (backpressure) pour assurer un flux stable des mises à jour.
     * Le `SseController` sera chargé de transformer ce flux en événements SSE pour que les clients connectés
     * reçoivent les mises à jour en temps réel.
     * @param topicStats Liste des objets {@code TopicStatsDto} contenant les statistiques des sujets
     *                   à émettre dans le flux réactif.
     */
    @Override
    public void updateTopicStats(List<TopicStatsDto> topicStats) {
        topicStatsSink.tryEmitNext(topicStats);//gestion auto du "backpressure"
    }

}
