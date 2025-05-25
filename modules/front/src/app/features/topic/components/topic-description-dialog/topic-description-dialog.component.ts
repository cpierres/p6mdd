import { Component, Inject } from '@angular/core';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef
} from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';


@Component({
  selector: 'app-topic-description-dialog',
  imports: [
    MatDialogModule,
    MatButtonModule,
  ],
  templateUrl: './topic-description-dialog.component.html',
  styleUrl: './topic-description-dialog.component.scss'
})
export class TopicDescriptionDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<TopicDescriptionDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { title: string, description: string }
  ) {}
}
