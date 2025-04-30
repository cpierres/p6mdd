import {Component, OnInit} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {Router} from '@angular/router';
import {MatOption, MatSelect} from '@angular/material/select';
import {MatFormFieldModule} from '@angular/material/form-field';
import {NgForOf} from '@angular/common';
import {PostService} from '../../services/post.service';
import {TopicStatsDto} from '../../interface/TopicStatsDto';
import {TopicStatsService} from '../../../topic/services/topic-stats.service';
import {PostDto} from '../../interface/PostDto';
import {FormsModule} from '@angular/forms';
import {MatGridList, MatGridTile} from '@angular/material/grid-list';
import {PostListComponent} from '../../components/post-list/post-list.component';
import {BreakpointObserver, Breakpoints} from '@angular/cdk/layout';

@Component({
  selector: 'app-post-list-page',
  imports: [
    MatButtonModule,
    MatFormFieldModule,
    MatSelect,
    MatOption,
    NgForOf,
    FormsModule,
    MatGridList,
    MatGridTile,
    PostListComponent,
  ],
  templateUrl: './post-list-page.component.html',
  styleUrl: './post-list-page.component.scss'
})
export class PostListPageComponent implements OnInit {
  topics: TopicStatsDto[] = [];
  posts: PostDto[] = [];
  selectedTopicId: string = 'subscribed';
  sortCriteria: string = 'date'; // Par défaut: tri par date (récent d'abord)
  cols: number = 2; // Nb cols par défaut sur grand écran

  constructor(private router: Router,
              private postService: PostService,
              private topicStatsService: TopicStatsService,
              private breakpointObserver: BreakpointObserver) {
  }

  ngOnInit(): void {
    this.loadTopics();
    this.loadPosts();
    this.initBreakpointObserver();

    // Utiliser le service SSE pour mettre à jour les statistiques des topics
    this.topicStatsService.getTopicStats().subscribe(topics => {
      this.topics = topics;
    });
  }

  openCreatePost() {
    // Navigue vers la page de création de post
    this.router.navigate(['/posts/new']);
  }

  loadTopics(): void {
    this.postService.getTopicStats()
      .subscribe(topics => {
        this.topics = topics;
      });
  }

  loadPosts(): void {
    this.postService.getPosts(this.sortCriteria, this.selectedTopicId)
      .subscribe(posts => {
        this.posts = posts;
      });
  }

  onSortChange(): void {
    this.loadPosts();
  }

  onTopicFilterChange(): void {
    this.loadPosts();
  }

  onSelectPost(post: PostDto): void {
    this.router.navigate(['/posts', post.id]);
  }

  private initBreakpointObserver(): void {
    this.breakpointObserver.observe([
      Breakpoints.Handset,      // Ecran mobile
      Breakpoints.Tablet,       // Ecran moyen
      Breakpoints.Web           // Ecran large
    ]).subscribe((result) => {
      if (result.matches) {
        if (this.breakpointObserver.isMatched(Breakpoints.Handset)) {
          this.cols = 1; // 1 colonne sur mobile
        } else if (this.breakpointObserver.isMatched(Breakpoints.Tablet)) {
          this.cols = 2; // 2 colonnes sur tablette
        } else {
          this.cols = 3; // 3 colonnes par défaut pour grand écran
        }
      }
    });


  }
}
