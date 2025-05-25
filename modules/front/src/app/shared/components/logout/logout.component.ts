import {Component, EventEmitter, Output} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {MatMenuItem} from '@angular/material/menu';

@Component({
  selector: 'app-logout',
  imports: [
    MatButtonModule,
    MatMenuItem
  ],
  templateUrl: './logout.component.html',
  styleUrl: './logout.component.scss'
})
export class LogoutComponent {

  @Output() logout: EventEmitter<void> = new EventEmitter();

  onLogout() {
    // $event.preventDefault(); // Empêche la soumission HTML par défaut
    // $event.stopPropagation(); //sinon double soumission intempestive (avec event pour 2eme)
    this.logout.emit();
    return false;
  }
}

