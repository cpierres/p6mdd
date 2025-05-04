import {Component, Input} from '@angular/core';
import {BreakpointObserver} from '@angular/cdk/layout';
import {TopicSubscribedStatus} from '../../interfaces/TopicSubscribedStatus';
import {async, map, Observable} from 'rxjs';
import {Breakpoints} from '@angular/cdk/layout';
import {TopicService} from '../../services/topic.service';
import {AsyncPipe} from '@angular/common';
import {MatCard, MatCardActions, MatCardContent, MatCardHeader, MatCardModule} from '@angular/material/card';
import {MatButtonModule} from '@angular/material/button';

@Component({
  selector: 'app-topic-list',
  imports: [
    AsyncPipe,
    MatCard,
    MatCardHeader,
    MatCardContent,
    MatCardActions,
    MatCardModule,
    MatButtonModule,
  ],
  templateUrl: './topic-list.component.html',
  styleUrl: './topic-list.component.scss'
})
export class TopicListComponent {
  @Input() topics: TopicSubscribedStatus[] = [];
  @Input() showSubscribeButton = true;
  @Input() showUnsubscribeButton = true;
  @Input() filterSubscribed = false;

  // Observable pour le nbre de colonnes selon la taille écran
  cols$: Observable<number>;

  constructor(private topicService: TopicService, private breakpointObserver: BreakpointObserver) {
    // Observable BreakpointObservers pour traquer "automatiquement" la taille d'écran pour le responsive
    this.cols$ = this.breakpointObserver
      .observe([Breakpoints.XSmall, Breakpoints.Small, Breakpoints.Medium, Breakpoints.Large])
      .pipe(
        map(result => {
          if (result.breakpoints[Breakpoints.XSmall]) {
            return 1; // 1 colonne pour mobile
          } else if (result.breakpoints[Breakpoints.Small]) {
            return 2; // 2 colonnes pour tablettes
          } else if (result.breakpoints[Breakpoints.Medium]) {
            return 2; // finalement 2 colonnes pour petits écrans... pour conformité stricte à maquette
          }
          return 2; // 2 colonnes aussi pour grande taille... pour conformité stricte à maquette
        })
      );
  }


  /**
   * Tronque la description si elle est trop longue (avec points de suite ...).
   * @param description
   */
  truncateDescription(description: string): string {
    const maxLines = 5;
    const approximateCharsPerLine = 50; //approximation
    const maxChars = maxLines * approximateCharsPerLine;

    if (description.length > maxChars) {
      return description.substring(0, maxChars) + '...';
    }
    return description;
  }

  /**
   * S'abonner à un topic
   * @param topicId string représentant l'UUID
   * @param event
   */
  subscribeTopic(topicId: string, event: Event): void {
    event.stopPropagation(); // empêche le bubbling
    this.topicService.subscribeToTopic(topicId).subscribe();
  }

  /**
   * Se désabonner d'un topic'
   * @param topicId string représentant l'UUID
   * @param event
   */
  unsubscribeTopic(topicId: string, event: Event): void {
    event.stopPropagation(); // empêche le bubbling
    this.topicService.unsubscribeFromTopic(topicId).subscribe();
  }

  /**
   * Filtrer les topics en fonction de l'input filterSubscribed
   * @returns
   */
  get filteredTopics(): TopicSubscribedStatus[] {
    if (this.filterSubscribed) {
      return this.topics.filter(topic => topic.subscribed);
    }
    return this.topics;
  }

  protected readonly async = async;
}
