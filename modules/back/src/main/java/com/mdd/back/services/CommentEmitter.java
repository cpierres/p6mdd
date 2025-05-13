package com.mdd.back.services;

import com.mdd.back.models.PostCommentDto;
import com.mdd.back.services.interfaces.ICommentNotifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * Service responsable de l'émission des nouveaux commentaires en temps réel.
 * Utilise un Sink réactif pour diffuser les commentaires aux clients connectés via SSE.
 */
@Service
public class CommentEmitter implements ICommentNotifier {
    // Sink pour émettre les nouveaux commentaires
    private final Sinks.Many<PostCommentDto> commentSink;

    public CommentEmitter() {
        // Configuration du sink pour conserver le dernier élément émis
        // et le rejouer pour les nouveaux abonnés
        this.commentSink = Sinks.many().replay().latest();
    }

    /**
     * Obtient le flux de commentaires pour les clients SSE
     * @return Flux de PostCommentDto
     */
    public Flux<PostCommentDto> getCommentStream() {
        return commentSink.asFlux();
    }

    /**
     * Notifie les clients connectés d'un nouveau commentaire
     * @param comment Le nouveau commentaire à diffuser
     */
    @Override
    public void notifyNewComment(PostCommentDto comment) {
        commentSink.tryEmitNext(comment);
    }
}