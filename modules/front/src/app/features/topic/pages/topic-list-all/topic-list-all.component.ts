import {Component, OnInit} from '@angular/core';
import {TopicSubscribedStatus} from '../../interfaces/TopicSubscribedStatus';
import {TopicService} from '../../services/topic.service';
import {TopicListComponent} from '../../components/topic-list/topic-list.component';

@Component({
  selector: 'app-topic-list-all',
  imports: [
    TopicListComponent
  ],
  templateUrl: './topic-list-all.component.html',
  styleUrl: './topic-list-all.component.scss'
})
export class TopicListAllComponent implements OnInit {
  topics: TopicSubscribedStatus[] = [];

  constructor(private topicService: TopicService) {}

  ngOnInit(): void {
    // Souscrire à l'Observable des Topics pour avoir les mises à jour
    this.topicService.topicsWithSubscriptionStatus$.subscribe(topics => {
      this.topics = topics;
    });

    // Chargement initial des des topics
    this.loadTopics();
  }

  loadTopics(): void {
    this.topicService.getTopicsWithSubscriptionStatus().subscribe();
  }
}

