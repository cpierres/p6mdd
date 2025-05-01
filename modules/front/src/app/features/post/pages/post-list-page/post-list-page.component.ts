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
    const breakpointNames = {
      [Breakpoints.XSmall]: 'XSmall (mobile)',
      [Breakpoints.Small]: 'Small (tablette)',
      [Breakpoints.Medium]: 'Medium (desktop)',
      [Breakpoints.Large]: 'Large (grand écran)',
      [Breakpoints.XLarge]: 'XLarge (grande taille d\'écran)'
    };

    this.breakpointObserver.observe(Object.keys(breakpointNames)).subscribe(result => {
      // Log des breakpoints actifs
      for (const query of Object.keys(breakpointNames)) {
        if (result.breakpoints[query]) {
          console.log(`Breakpoint actif : ${breakpointNames[query]}, largeur : ${window.innerWidth}px`);
        }
      }

      // Ajustement des colonnes
      if (result.breakpoints[Breakpoints.XSmall]) {
        this.cols = 1; // 1 colonne pour mobile (< 600px)
      } else if (result.breakpoints[Breakpoints.Small]) {
        this.cols = 2; // 2 colonnes pour tablettes (600px - 959px)
      } else if (result.breakpoints[Breakpoints.Medium]) {
        this.cols = 3; // 3 colonnes (960px - 1279px)
      } else {
        this.cols = 4; // 4 colonnes pour grands écrans (≥ 1280px)
      }
    });
  }

}
