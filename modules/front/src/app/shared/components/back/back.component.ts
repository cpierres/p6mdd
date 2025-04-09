import {Component} from '@angular/core';
import {Location} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatIconButton} from '@angular/material/button';

@Component({
  selector: 'app-back',
  imports: [
    MatIconModule,
    MatIconButton,
  ],
  templateUrl: './back.component.html',
  styleUrl: './back.component.css'
})
export class BackComponent {
  constructor(private location: Location) {
  }

  goBack(): void {
    this.location.back(); // Retourne à la page précédente dans l'historique
  }

}
