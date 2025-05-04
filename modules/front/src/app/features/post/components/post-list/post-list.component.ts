import {Component, EventEmitter, Input, Output} from '@angular/core';
import {MatCard, MatCardContent, MatCardHeader, MatCardModule} from '@angular/material/card';
import {CommonModule, DatePipe} from '@angular/common';
import {PostDto} from '../../interface/PostDto';
import {MatButtonModule} from '@angular/material/button';
import {
  TopicDescriptionDialogComponent
} from '../../../topic/components/topic-description-dialog/topic-description-dialog.component';
import {MatDialog} from '@angular/material/dialog';
import {TopicService} from '../../../topic/services/topic.service';


@Component({
  selector: 'app-post-list',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, DatePipe, MatCard, MatCardHeader, MatCardContent, DatePipe],
  templateUrl: './post-list.component.html',
  styleUrls: ['./post-list.component.scss']
})
export class PostListComponent {
  @Input() post!: PostDto;
  @Input() overflowPoints = false;
  @Output() onSelect = new EventEmitter<PostDto>();

  constructor(
    private dialog: MatDialog,
    private topicService: TopicService
  ) {}

  selectPost(): void {
    this.onSelect.emit(this.post);
  }

  /**
   * Affiche la description complète du thème dans un dialogue
   * @param event L'événement de clic
   */
  showTopicDescription(event: Event): void {
    event.stopPropagation(); // Empêche la sélection du post

    this.topicService.getTopicById(this.post.topicId).subscribe(topic => {
      this.dialog.open(TopicDescriptionDialogComponent, {
        width: '500px',
        data: {
          title: topic.title,
          description: topic.description
        }
      });
    });
  }

}
