import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { PostCommentDto } from '../interface/PostCommentDto';
import { environment } from '../../../../environments/environment';

/**
 * Service responsable de la réception des nouveaux commentaires en temps réel via SSE.
 * Permet aux composants de s'abonner à un flux de nouveaux commentaires.
 */
@Injectable({
  providedIn: 'root'
})
export class CommentEventService implements OnDestroy {
  private eventSource: EventSource | null = null;
  private newCommentSubject = new BehaviorSubject<PostCommentDto | null>(null);

  constructor() {
    this.connectToSSE();
  }

  /**
   * Établit la connexion SSE avec le backend
   */
  private connectToSSE(): void {
    this.eventSource = new EventSource(environment.backendUrl + 'comments/stream');

    this.eventSource.addEventListener('comment-created', (event: MessageEvent) => {
      const comment: PostCommentDto = JSON.parse(event.data);
      this.newCommentSubject.next(comment);
    });

    this.eventSource.onerror = (error) => {
      console.error('SSE error:', error);
      this.disconnectFromSSE();
      // Tentative de reconnexion après 5 secondes
      setTimeout(() => this.connectToSSE(), 5000);
    };
  }

  /**
   * Obtient le flux de nouveaux commentaires
   * @returns Observable émettant les nouveaux commentaires
   */
  getNewCommentStream(): Observable<PostCommentDto | null> {
    return this.newCommentSubject.asObservable();
  }

  ngOnDestroy(): void {
    this.disconnectFromSSE();
  }

  /**
   * Ferme la connexion SSE
   */
  private disconnectFromSSE(): void {
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
    }
  }
}
