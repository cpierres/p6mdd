package com.mdd.back.services.interfaces;

import com.mdd.back.models.PostCommentDto;

/**
 * Interface pour la notification des nouveaux commentaires.
 * Permet de découpler l'émission des événements de commentaires de leur traitement.
 */
public interface ICommentNotifier {
    /**
     * Notifie les clients connectés d'un nouveau commentaire
     * @param comment Le nouveau commentaire à diffuser
     */
    void notifyNewComment(PostCommentDto comment);
}