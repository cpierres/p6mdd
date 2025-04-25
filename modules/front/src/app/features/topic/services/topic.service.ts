import {Injectable, OnDestroy} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject, Observable, Subscription, map, tap} from 'rxjs';
import {TopicSubscribedStatus} from '../interfaces/TopicSubscribedStatus';
import {environment} from '../../../../environments/environment';
import {TopicStatsService} from './topic-stats.service';
import {TopicStatsDto} from '../../post/interface/TopicStatsDto';

@Injectable({
  providedIn: 'root'
})
export class TopicService implements OnDestroy {
  private apiUrl = environment.baseUrl + 'topics';
  // BehaviorSubject pour suivre les modifications dans les abonnements aux topic
  private topicsWithSubscriptionStatusSubject = new BehaviorSubject<TopicSubscribedStatus[]>([]);
  public topicsWithSubscriptionStatus$ = this.topicsWithSubscriptionStatusSubject.asObservable();
  private topicStatsSubscription: Subscription;

  constructor(
    private http: HttpClient,
    private topicStatsService: TopicStatsService
  ) {
    // S'abonner aux mises à jour des statistiques des topics via SSE
    this.topicStatsSubscription = this.topicStatsService.getTopicStats().subscribe(
      (topicStats: TopicStatsDto[]) => this.updateTopicsWithStats(topicStats)
    );
  }

  /**
   * Obtenir tous les topics avec le statut d'abonnement pour l'utilisateur authentifié
   */
  getTopicsWithSubscriptionStatus(): Observable<TopicSubscribedStatus[]> {
    return this.http.get<TopicSubscribedStatus[]>(`${this.apiUrl}/with-subscription-status`)
      .pipe(
        tap(topics => this.topicsWithSubscriptionStatusSubject.next(topics))
      );
  }

  /**
   * Abonner l'utilisateur authentifié à un topic
   * @param topicId
   */
  subscribeToTopic(topicId: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${topicId}/subscribe`, {})
      .pipe(
        tap(() => {
          // mettre à jour l'état local après l'abonnement
          const currentTopics = this.topicsWithSubscriptionStatusSubject.value;
          const updatedTopics = currentTopics.map(topic =>
            topic.id === topicId ? {...topic, subscribed: true} : topic
          );
          this.topicsWithSubscriptionStatusSubject.next(updatedTopics);
        })
      );
  }

  /**
   * Désabonner l'utilisateur authentifié d'un Topic
   */
  unsubscribeFromTopic(topicId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${topicId}/unsubscribe`)
      .pipe(
        tap(() => {
          // mettre à jour l'état local après l'abonnement
          const currentTopics = this.topicsWithSubscriptionStatusSubject.value;
          const updatedTopics = currentTopics.map(topic =>
            topic.id === topicId ? {...topic, subscribed: false} : topic
          );
          this.topicsWithSubscriptionStatusSubject.next(updatedTopics);
        })
      );
  }

  /**
   * Obtenir uniquement les topics auxquels l'utilisateur authentifié est abonné
   */
  getSubscribedTopics(): Observable<TopicSubscribedStatus[]> {
    return this.getTopicsWithSubscriptionStatus().pipe(
      map(topics => topics.filter(topic => topic.subscribed))
    );
  }

  /**
   * Met à jour les topics avec les statistiques reçues du flux SSE
   * @param topicStats Les statistiques des topics reçues du flux SSE
   */
  private updateTopicsWithStats(topicStats: TopicStatsDto[]): void {
    // Récupérer les topics actuels
    const currentTopics = this.topicsWithSubscriptionStatusSubject.value;

    if (currentTopics.length === 0) {
      // Si aucun topic n'est encore chargé, ne rien faire
      return;
    }

    // Mettre à jour les statistiques des topics
    const updatedTopics = currentTopics.map(topic => {
      // Chercher les statistiques correspondantes pour ce topic
      const stats = topicStats.find(stat => stat.id === topic.id);

      if (stats) {
        // Mettre à jour les statistiques du topic
        return {
          ...topic,
          countPosts: stats.countPosts,
          countComments: stats.countComments
        };
      }

      // Si aucune statistique n'est trouvée, retourner le topic inchangé
      return topic;
    });

    // Trier les topics par popularité (somme des posts et commentaires) en ordre décroissant
    const sortedTopics = [...updatedTopics].sort((t1, t2) => {
      const t1Popularity = t1.countPosts + t1.countComments;
      const t2Popularity = t2.countPosts + t2.countComments;
      return t2Popularity - t1Popularity; // Ordre décroissant
    });

    // Émettre les topics mis à jour
    this.topicsWithSubscriptionStatusSubject.next(sortedTopics);
  }

  /**
   * Nettoie les abonnements lors de la destruction du service
   */
  ngOnDestroy(): void {
    if (this.topicStatsSubscription) {
      this.topicStatsSubscription.unsubscribe();
    }
  }
}
