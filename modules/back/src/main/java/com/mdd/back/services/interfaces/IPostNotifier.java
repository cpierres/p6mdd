package com.mdd.back.services.interfaces;

import com.mdd.back.models.PostDto;

/**
 * Interface pour la notification des nouveaux posts.
 * Permet de découpler l'émission des événements de posts de leur traitement.
 */
public interface IPostNotifier {
    /**
     * Notifie les clients connectés d'un nouveau post
     * @param post Le nouveau post à diffuser
     */
    void notifyNewPost(PostDto post);
}