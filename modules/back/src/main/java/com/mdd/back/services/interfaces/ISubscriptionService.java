package com.mdd.back.services.interfaces;

import com.mdd.back.models.TopicSubscribedForAuthUserDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service responsable de la gestion des abonnements aux topics.
 */
public interface ISubscriptionService {
    /**
     * Abonne l'utilisateur authentifié à un topic.
     * @param topicId L'identifiant du topic auquel s'abonner
     * @return Un Mono<Void> qui complète si l'abonnement est réussi, ou émet une erreur si l'abonnement échoue
     */
    Mono<Void> subscribeAuthenticatedUserToTopic(UUID topicId);

    /**
     * Désabonne l'utilisateur authentifié d'un topic.
     * @param topicId L'identifiant du topic duquel se désabonner
     * @return Un Mono<Void> qui complète si le désabonnement est réussi, ou émet une erreur si le désabonnement échoue
     */
    Mono<Void> unsubscribeAuthenticatedUserFromTopic(UUID topicId);

    /**
     * Vérifie si l'utilisateur authentifié est abonné à un topic spécifique.
     * @param topicId L'identifiant du topic à vérifier
     * @return Un Mono<Boolean> qui émet true si l'utilisateur est abonné, false sinon
     */
    Mono<Boolean> isAuthenticatedUserSubscribedToTopic(UUID topicId);

    /**
     * Récupère tous les topics avec l'information d'abonnement pour l'utilisateur authentifié.
     * @return Un Flux de TopicSubscribedForAuthUserDto contenant les informations des topics et si l'utilisateur y est abonné
     */
    Flux<TopicSubscribedForAuthUserDto> getAllTopicsWithAuthUserSubscription();
}