import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { PostDto } from '../interface/PostDto';
import { environment } from '../../../../environments/environment';

/**
 * Service responsable de la réception des nouveaux posts en temps réel via SSE.
 * Permet aux composants de s'abonner à un flux de nouveaux posts.
 */
@Injectable({
  providedIn: 'root'
})
export class PostEventService implements OnDestroy {
  private eventSource: EventSource | null = null;
  private newPostSubject = new BehaviorSubject<PostDto | null>(null);

  constructor() {
    this.connectToSSE();
  }

  /**
   * Établit la connexion SSE avec le backend
   */
  private connectToSSE(): void {
    this.eventSource = new EventSource(environment.backendUrl + 'posts/stream');

    this.eventSource.addEventListener('post-created', (event: MessageEvent) => {
      const post: PostDto = JSON.parse(event.data);
      this.newPostSubject.next(post);
    });

    this.eventSource.onerror = (error) => {
      console.error('SSE error:', error);
      this.disconnectFromSSE();
      // Tentative de reconnexion après 5 secondes
      setTimeout(() => this.connectToSSE(), 5000);
    };
  }

  /**
   * Obtient le flux de nouveaux posts
   * @returns Observable émettant les nouveaux posts
   */
  getNewPostStream(): Observable<PostDto | null> {
    return this.newPostSubject.asObservable();
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
