import {Component, HostListener, OnDestroy, OnInit} from '@angular/core';
import {MatToolbar} from '@angular/material/toolbar';
import {Router, RouterLink} from '@angular/router';
import {MatMenu, MatMenuItem, MatMenuTrigger} from '@angular/material/menu';
import {MatButtonModule, MatIconButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {Subscription} from 'rxjs';
import {SessionService} from '../../services/session-service.service';
import {NgClass, NgIf, NgOptimizedImage} from '@angular/common';
import {LogoutComponent} from '../logout/logout.component';

const MOBILE_MAX_WIDTH = 768; // Détermine largeur max pour écrans mobiles

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  imports: [
    MatToolbar,
    RouterLink,
    MatMenuItem,
    MatIconButton,
    MatIcon,
    MatMenu,
    MatButtonModule,
    MatMenuTrigger,
    NgIf,
    NgOptimizedImage,
    LogoutComponent,
    NgClass
  ],
  styleUrl: './header.component.scss'
})
export class HeaderComponent implements OnInit, OnDestroy {
  showRightMenu: boolean = false;
  private sessionSubscription: Subscription | undefined;
  isMobile: boolean = false;

  constructor(public sessionService: SessionService, private router: Router) {}

  ngOnInit(): void {
    // Gestion de la souscription pour le statut de connexion
    this.sessionSubscription = this.sessionService.$isLogged().subscribe({
      next: (isLogged) => this.showRightMenu = isLogged
    });

    // Détection initiale de la taille de l'écran
    this.updateIsMobile();
  }

  @HostListener('window:resize')
  onResize(): void {
    this.updateIsMobile();
  }

  /**
   * Met à jour le flag indiquant si l'écran est de type mobile
   * @private
   */
  private updateIsMobile(): void {
    this.isMobile = window.innerWidth <= MOBILE_MAX_WIDTH;
  }

  ngOnDestroy(): void {
    // Nettoyage de la souscription pour éviter fuites mémoire
    this.sessionSubscription?.unsubscribe();
  }

  onLogout(): void {
    this.sessionService.logOut();
    this.router.navigate(['/']);
  }
}
