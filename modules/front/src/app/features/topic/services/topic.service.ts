import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject, Observable, map, tap} from 'rxjs';
import {TopicSubscribedStatus} from '../interfaces/TopicSubscribedStatus';
import {environment} from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class TopicService {
  private apiUrl = environment.baseUrl + 'topics';
  // BehaviorSubject pour suivre les modifications dans les abonnements aux topic
  private topicsWithSubscriptionStatusSubject = new BehaviorSubject<TopicSubscribedStatus[]>([]);
  public topicsWithSubscriptionStatus$ = this.topicsWithSubscriptionStatusSubject.asObservable();

  constructor(private http: HttpClient) {
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

}
