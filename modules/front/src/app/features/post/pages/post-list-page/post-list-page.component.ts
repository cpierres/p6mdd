import {Component, OnInit} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {Router} from '@angular/router';
import {MatOption, MatSelect} from '@angular/material/select';
import {MatFormFieldModule} from '@angular/material/form-field';
import {NgForOf} from '@angular/common';
import {PostService} from '../../services/post.service';
import {TopicStatsDto} from '../../interface/TopicStatsDto';
import {TopicStatsService} from '../../../topic/services/topic-stats.service';

@Component({
  selector: 'app-post-list-page',
  imports: [
    MatButtonModule,
    MatFormFieldModule,
    MatSelect,
    MatOption,
    NgForOf,
  ],
  templateUrl: './post-list-page.component.html',
  styleUrl: './post-list-page.component.scss'
})
export class PostListPageComponent implements OnInit {
  topics: TopicStatsDto[] = [];

  constructor(private router: Router,
              private postService: PostService,
              private topicStatsService: TopicStatsService) {
  }

  ngOnInit(): void {
    this.loadTopics();

    // Utiliser le service SSE pour mettre à jour les statistiques des topics
    this.topicStatsService.getTopicStats().subscribe(topics => {
      this.topics = topics;
    });
  }

  openCreatePost() {
    // Navigue vers la page de création de post
    this.router.navigate(['/post/create']);
  }

  loadTopics(): void {
    this.postService.getTopicStats()
      .subscribe(topics => {
        this.topics = topics;
      });
  }

}
