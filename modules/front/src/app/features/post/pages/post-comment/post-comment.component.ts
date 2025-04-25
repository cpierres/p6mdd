import {Component, OnInit} from '@angular/core';
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
export class PostCommentComponent implements OnInit {
  post: PostDto | null = null;
  commentForm: FormGroup;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private postService: PostService,
    private fb: FormBuilder
  ) {
    this.commentForm = this.fb.group({
      comment: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    const postId = this.route.snapshot.paramMap.get('id');
    if (postId) {
      this.loadPost(postId);
    } else {
      this.router.navigate(['/posts']);
    }
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

