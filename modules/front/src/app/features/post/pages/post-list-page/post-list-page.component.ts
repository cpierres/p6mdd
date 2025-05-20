import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {Router} from '@angular/router';
import {MatOption, MatSelect} from '@angular/material/select';
import {MatFormFieldModule} from '@angular/material/form-field';
import {NgForOf, NgIf} from '@angular/common';
import {PostService} from '../../services/post.service';
import {TopicStatsDto} from '../../interface/TopicStatsDto';
import {TopicStatsService} from '../../../topic/services/topic-stats.service';
import {PostDto} from '../../interface/PostDto';
import {FormsModule} from '@angular/forms';
import {MatGridList, MatGridTile} from '@angular/material/grid-list';
import {PostListComponent} from '../../components/post-list/post-list.component';
import {BreakpointObserver, Breakpoints} from '@angular/cdk/layout';
import {PostEventService} from '../../services/post-event.service';
import {Subscription} from 'rxjs';
import {MatMenu, MatMenuModule, MatMenuTrigger} from '@angular/material/menu';
import {MatIcon, MatIconModule} from '@angular/material/icon';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ExportDialogComponent} from '../export-dialog/export-dialog.component';

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
    NgIf,
    MatMenuModule,
    MatIconModule,
  ],
  templateUrl: './post-list-page.component.html',
  styleUrl: './post-list-page.component.scss'
})
export class PostListPageComponent implements OnInit, OnDestroy {
  topics: TopicStatsDto[] = [];
  posts: PostDto[] = [];
  selectedTopicId: string = 'subscribed';
  sortCriteria: string = 'date'; // Par défaut: tri par date (récent d'abord)
  cols: number = 2; // Nb cols par défaut sur grand écran
  gutterSize: string = '16px'; // Espacement par défaut entre les cartes
  private subscription: Subscription = new Subscription();
  @ViewChild(MatMenuTrigger) menuTrigger!: MatMenuTrigger;

  constructor(private router: Router,
              private postService: PostService,
              private topicStatsService: TopicStatsService,
              private postEventService: PostEventService,
              private breakpointObserver: BreakpointObserver,
              private dialog: MatDialog,
              private snackBar: MatSnackBar
  ) {
  }

  ngOnInit(): void {
    this.loadTopics();
    this.loadPosts();
    this.initBreakpointObserver();

    // Utiliser le service SSE pour mettre à jour les statistiques des topics
    this.subscription.add(
      this.topicStatsService.getTopicStats().subscribe(topics => {
        this.topics = topics;
      })
    );

    // S'abonner au flux de nouveaux posts
    this.subscription.add(
      this.postEventService.getNewPostStream().subscribe(newPost => {
        if (newPost) {
          // Vérifier si le post correspond aux critères de filtrage actuels
          if (this.shouldAddPost(newPost)) {
            // Ajouter le nouveau post au début de la liste
            this.posts = [newPost, ...this.posts];
          }
        }
      })
    );
  }

  ngOnDestroy(): void {
    // Nettoyer les abonnements
    this.subscription.unsubscribe();
  }

  /**
   * Détermine si un post doit être ajouté à la liste en fonction des filtres actuels
   */
  private shouldAddPost(post: PostDto): boolean {
    // Si on affiche tous les posts
    if (this.selectedTopicId === 'all') {
      return true;
    }

    // Si on filtre par topic spécifique
    if (this.selectedTopicId !== 'subscribed' && this.selectedTopicId !== 'all') {
      return post.topicId === this.selectedTopicId;
    }

    // Pour 'subscribed', on ne peut pas déterminer facilement si l'utilisateur est abonné au topic
    // On pourrait soit recharger la liste complète, soit maintenir une liste des IDs des topics auxquels l'utilisateur est abonné
    // Pour simplifier, on recharge la liste complète quand un nouveau post arrive et qu'on est en mode 'subscribed'
    if (this.selectedTopicId === 'subscribed') {
      this.loadPosts();
      return false; // On ne l'ajoute pas manuellement puisqu'on recharge la liste
    }

    return false;
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

      // Ajustement des colonnes et de l'espacement
      if (result.breakpoints[Breakpoints.XSmall]) {
        this.cols = 1; // 1 colonne pour mobile (< 600px)
        this.gutterSize = '8px'; // Espacement réduit et constant pour mobile
      } else {
        this.cols = 2; // 2 colonnes pour tous les écrans plus grands que mobile
        this.gutterSize = '16px'; // Espacement standard
      }
    });
  }

  /**
   * Gère le clic droit sur le bouton "Créer un article"
   */
  onRightClick(event: MouseEvent): void {
    event.preventDefault();
    // Ouvrir le menu contextuel à la position du clic
    if (this.menuTrigger) {
      this.menuTrigger.openMenu();
    }
  }

  /**
   * Exporte les posts avec le nom de fichier par défaut (posts.json)
   */
  exportPosts(): void {
    this.postService.exportPostsToJson(this.sortCriteria, this.selectedTopicId)
      .subscribe({
        next: (filePath) => {
          this.snackBar.open(`Posts exportés avec succès vers ${filePath}`, 'Fermer', {
            duration: 5000,
          });
        },
        error: (error) => {
          console.error('Erreur lors de l\'exportation des posts', error);
          this.snackBar.open('Erreur lors de l\'exportation des posts', 'Fermer', {
            duration: 5000,
          });
        }
      });
  }

  /**
   * Ouvre une boîte de dialogue pour saisir un nom de fichier personnalisé
   */
  exportPostsWithCustomFilename(): void {
    const dialogRef = this.dialog.open(ExportDialogComponent, {
      width: '400px',
      data: { filename: 'posts.json' }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.postService.exportPostsToJson(this.sortCriteria, this.selectedTopicId, result)
          .subscribe({
            next: (filePath) => {
              this.snackBar.open(`Posts exportés avec succès vers ${filePath}`, 'Fermer', {
                duration: 5000,
              });
            },
            error: (error) => {
              console.error('Erreur lors de l\'exportation des posts', error);
              this.snackBar.open('Erreur lors de l\'exportation des posts', 'Fermer', {
                duration: 5000,
              });
            }
          });
      }
    });
  }

}
