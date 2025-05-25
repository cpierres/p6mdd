package com.mdd.back.services.interfaces;

import reactor.core.publisher.Mono;

/**
 * Interface générique pour la validation d'entités.
 * @param <T> Type de l'entité à valider
 */
public interface IValidator<T> {
    /**
     * Valide une entité.
     * @param entity L'entité à valider
     * @return Un Mono<Void> qui complète si la validation réussit, ou émet une erreur si la validation échoue
     */
    Mono<Void> validate(T entity);
}

