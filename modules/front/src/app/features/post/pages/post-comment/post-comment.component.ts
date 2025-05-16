import {Component, OnDestroy, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatIconModule} from '@angular/material/icon';
import {MatDividerModule} from '@angular/material/divider';
import {PostListComponent} from '../../components/post-list/post-list.component';
import {BackComponent} from '../../../../shared/components/back/back.component';
import {PostDto} from '../../interface/PostDto';
import {ActivatedRoute, Router} from '@angular/router';
import {PostService} from '../../services/post.service';
import {PostCommentDto} from '../../interface/PostCommentDto';
import {CommentEventService} from '../../services/comment-event.service';
import {Subscription} from 'rxjs';
import {SessionService} from '../../../../shared/services/session-service.service';

@Component({
  selector: 'app-post-comment',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatDividerModule,
    PostListComponent,
    BackComponent
  ],
  templateUrl: './post-comment.component.html',
  styleUrls: ['./post-comment.component.scss']
})
export class PostCommentComponent implements OnInit, OnDestroy {
  post: PostDto | null = null;
  commentForm: FormGroup;
  private subscription: Subscription = new Subscription();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private postService: PostService,
    private commentEventService: CommentEventService,
    private fb: FormBuilder,
    private sessionService: SessionService
  ) {
    this.commentForm = this.fb.group({
      comment: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    const postId = this.route.snapshot.paramMap.get('id');
    if (postId) {
      this.loadPost(postId);

      // S'abonner au flux de nouveaux commentaires
      this.subscription.add(
        this.commentEventService.getNewCommentStream().subscribe(newComment => {
          if (newComment && this.post && newComment.postId === this.post.id) {
            // Vérifier si le commentaire a été créé par l'utilisateur actuel
            if (this.sessionService.user && newComment.createdBy === this.sessionService.user.id) {
              // Ignorer les commentaires créés par l'utilisateur actuel
              // car ils ont déjà été ajoutés dans submitComment()
              return;
            }

            // Ajouter le nouveau commentaire au début de la liste si le post est chargé
            // et que le commentaire appartient à ce post
            if (!this.post.comments) {
              this.post.comments = [];
            }

            // Vérifier si le commentaire existe déjà pour éviter les doublons
            const commentExists = this.post.comments.some(c => c.id === newComment.id);
            if (!commentExists) {
              this.post.comments.unshift(newComment);
            }
          }
        })
      );
    } else {
      this.router.navigate(['/posts']);
    }
  }

  ngOnDestroy(): void {
    // Nettoyer les abonnements
    this.subscription.unsubscribe();
  }

  loadPost(postId: string): void {
    this.postService.getPostById(postId).subscribe(post => {
      this.post = post;
    });
  }

  submitComment(): void {
    if (this.commentForm.valid && this.post) {
      const newComment: PostCommentDto = {
        id: '',
        postId: this.post.id,
        comment: this.commentForm.value.comment,
        createdAt: '',
        updatedAt: '',
        createdBy: '',
        createdByUsername: ''
      };

      this.postService.createComment(newComment).subscribe(comment => {
        // Ajouter le nouveau commentaire à la liste
        if (this.post && this.post.comments) {
          this.post.comments.unshift(comment);
        }

        // Réinitialiser le formulaire
        this.commentForm.reset();
      });
    }
  }
}
