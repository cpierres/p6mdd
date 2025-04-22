import {Component, EventEmitter, Input, Output} from '@angular/core';
import {MatCard, MatCardContent, MatCardHeader, MatCardModule} from '@angular/material/card';
import {CommonModule, DatePipe} from '@angular/common';
import {PostDto} from '../../interface/PostDto';
import {MatButtonModule} from '@angular/material/button';


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

  selectPost(): void {
    this.onSelect.emit(this.post);
  }
}
