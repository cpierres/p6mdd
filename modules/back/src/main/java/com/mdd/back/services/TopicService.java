package com.mdd.back.services;

import com.mdd.back.mappers.TopicMapper;
import com.mdd.back.models.TopicDto;
import com.mdd.back.models.TopicSubscribedForAuthUserDto;
import com.mdd.back.repositories.TopicRepository;
import com.mdd.back.services.interfaces.ISubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service responsable de la gestion des topics.
 * Sert de façade par rapport au service d'abonnement aux Topics
 */
@Service
public class TopicService {
    private final TopicRepository topicRepository;
    private final TopicMapper topicMapper;
    private final ISubscriptionService subscriptionService;

    @Autowired
    public TopicService(TopicRepository topicRepository, 
                       TopicMapper topicMapper, 
                       ISubscriptionService subscriptionService) {
        this.topicRepository = topicRepository;
        this.topicMapper = topicMapper;
        this.subscriptionService = subscriptionService;
    }

    /**
     * Récupère tous les topics.
     * @return Flux<TopicDto> contenant la liste des topics
     */
    public Flux<TopicDto> getAllTopics() {
        return topicRepository.findAll()
                .map(topicMapper::topicToTopicDto);
    }

    /**
     * Abonne l'utilisateur authentifié à un topic.
     * Cette méthode délègue l'opération au SubscriptionService.
     * @param topicId L'identifiant du topic auquel s'abonner
     * @return Un Mono<Void> qui complète si l'abonnement est réussi, ou émet une erreur si l'abonnement échoue
     */
    public Mono<Void> subscribeAuthenticatedUserToTopic(UUID topicId) {
        return subscriptionService.subscribeAuthenticatedUserToTopic(topicId);
    }

    /**
     * Désabonne l'utilisateur authentifié d'un topic.
     * Cette méthode délègue l'opération au SubscriptionService.
     * @param topicId L'identifiant du topic duquel se désabonner
     * @return Un Mono<Void> qui complète si le désabonnement est réussi, ou émet une erreur si le désabonnement échoue
     */
    public Mono<Void> unsubscribeAuthenticatedUserFromTopic(UUID topicId) {
        return subscriptionService.unsubscribeAuthenticatedUserFromTopic(topicId);
    }

    /**
     * Récupère tous les topics avec l'information d'abonnement pour l'utilisateur authentifié.
     * Cette méthode délègue l'opération au SubscriptionService.
     * @return Un Flux de TopicSubscribedForAuthUserDto contenant les informations des topics et si l'utilisateur y est abonné
     */
    public Flux<TopicSubscribedForAuthUserDto> getAllTopicsWithAuthUserSubscription() {
        return subscriptionService.getAllTopicsWithAuthUserSubscription();
    }
}
