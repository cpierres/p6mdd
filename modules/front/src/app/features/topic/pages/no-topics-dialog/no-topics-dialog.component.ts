import { Component } from '@angular/core';
import { MatDialogTitle, MatDialogContent, MatDialogActions, MatDialogClose } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-no-topics-dialog',
  standalone: true,
  imports: [
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatDialogClose,
    MatButtonModule
  ],
  template: `
    <h2 mat-dialog-title>Aucun thème disponible</h2>
    <mat-dialog-content>
      Pour créer un article, vous devez d'abord vous abonner à au moins un thème.<br/>
      Vous allez être redirigé vers la page des thèmes.
    </mat-dialog-content>
    <mat-dialog-actions>
      <button mat-raised-button color="primary" [mat-dialog-close]="true">OK</button>
    </mat-dialog-actions>
  `
})
export class NoTopicsDialogComponent {}
