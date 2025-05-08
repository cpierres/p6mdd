import {Injectable, OnDestroy} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {TopicStatsDto} from '../../post/interface/TopicStatsDto';
import {environment} from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class TopicStatsService implements OnDestroy {
  private eventSource: EventSource | null = null;
  private topicStatsSubject = new BehaviorSubject<TopicStatsDto[]>([]);

  constructor() {
    this.connectToSSE();
  }

  private connectToSSE(): void {
    this.eventSource = new EventSource(environment.backendUrl+'topics/stats/stream');

    this.eventSource.addEventListener('topic-stats-update', (event: MessageEvent) => {
      const stats: TopicStatsDto[] = JSON.parse(event.data);
      this.topicStatsSubject.next(stats);
    });

    this.eventSource.onerror = (error) => {
      console.error('SSE error:', error);
      this.disconnectFromSSE();
      // Tentative de reconnexion après 5 secondes
      setTimeout(() => this.connectToSSE(), 5000);
    };
  }

  getTopicStats(): Observable<TopicStatsDto[]> {
    return this.topicStatsSubject.asObservable();
  }

  ngOnDestroy(): void {
    this.disconnectFromSSE();
  }

  private disconnectFromSSE(): void {
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
    }
  }

}
