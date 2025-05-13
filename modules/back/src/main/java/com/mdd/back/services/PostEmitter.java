package com.mdd.back.services;

import com.mdd.back.models.PostDto;
import com.mdd.back.services.interfaces.IPostNotifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * Service responsable de l'émission des nouveaux posts en temps réel.
 * Utilise un Sink réactif pour diffuser les posts aux clients connectés via SSE.
 */
@Service
public class PostEmitter implements IPostNotifier {
    // Sink pour émettre les nouveaux posts
    private final Sinks.Many<PostDto> postSink;

    public PostEmitter() {
        // Configuration du sink pour conserver le dernier élément émis
        // et le rejouer pour les nouveaux abonnés
        this.postSink = Sinks.many().replay().latest();
    }

    /**
     * Obtient le flux de posts pour les clients SSE
     * @return Flux de PostDto
     */
    public Flux<PostDto> getPostStream() {
        return postSink.asFlux();
    }

    /**
     * Notifie les clients connectés d'un nouveau post
     * @param post Le nouveau post à diffuser
     */
    @Override
    public void notifyNewPost(PostDto post) {
        postSink.tryEmitNext(post);
    }
}