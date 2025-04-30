import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {Router} from '@angular/router';
import {BackComponent} from '../../../../shared/components/back/back.component';
import {PostService} from '../../services/post.service';
import {PostDto} from '../../interface/PostDto';
import {TopicService} from '../../../topic/services/topic.service';
import {TopicSubscribedStatus} from '../../../topic/interfaces/TopicSubscribedStatus';
import {NoTopicsDialogComponent} from '../../../topic/pages/no-topics-dialog/no-topics-dialog.component';
import {MatDialog, MatDialogModule} from '@angular/material/dialog';
import {CdkTextareaAutosize, TextFieldModule} from '@angular/cdk/text-field';

@Component({
  selector: 'app-post-create',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    BackComponent,
    MatDialogModule,
    CdkTextareaAutosize,
    TextFieldModule
  ],
  templateUrl: './post-create.component.html',
  styleUrls: ['./post-create.component.scss']
})
export class PostCreateComponent implements OnInit {
  postForm: FormGroup;
  topics: TopicSubscribedStatus[] = [];

  constructor(
    private fb: FormBuilder,
    private postService: PostService,
    private topicService: TopicService,
    private router: Router,
    private dialog: MatDialog
  ) {
    this.postForm = this.fb.group({
      topicId: ['', Validators.required],
      title: ['', Validators.required],
      content: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadTopics();
  }

  loadTopics(): void {
    this.topicService.getSubscribedTopics().subscribe(topics => {
      this.topics = topics;
      if (topics.length === 0) {
        // Afficher une boîte de dialogue ou un message
        this.showNoTopicsDialog();
      }
    });
  }

  createPost(): void {
    if (this.postForm.valid) {
      const newPost: PostDto = {
        id: '',
        title: this.postForm.value.title,
        topicId: this.postForm.value.topicId,
        topicTitle: '',
        content: this.postForm.value.content,
        createdAt: '',
        updatedAt: '',
        createdBy: '',
        createdByUsername: '',
        updatable: false
      };

      this.postService.createPost(newPost).subscribe(post => {
        // Redirection vers la liste des posts après création
        this.router.navigate(['/posts']);
      });
    }
  }

  showNoTopicsDialog(): void {
    // Utiliser MatDialog pour afficher une boîte de dialogue
    const dialogRef = this.dialog.open(NoTopicsDialogComponent);

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        // Si l'utilisateur clique sur "S'abonner", rediriger vers la page des topics
        this.router.navigate(['/topics']);
      }
    });
  }

}
