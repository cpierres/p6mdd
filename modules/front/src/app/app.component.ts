import {Component, OnInit} from '@angular/core';
import {NavigationEnd, Router, RouterOutlet} from '@angular/router';
import {MatButtonModule} from '@angular/material/button';
import {HeaderComponent} from './shared/components/header/header.component';
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, MatButtonModule, HeaderComponent, NgIf],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  title = 'MDD - Monde Du Développement';
  isHeaderVisible: boolean = false;

  // Constante pour les routes pour lesquelles le header doit être caché
  private readonly hiddenHeaderRoutes: string[] = ['/', '/home'];

  constructor(private router: Router) {}

  ngOnInit(): void {
    this.setHeaderVisibility(this.router.url);
    this.subscribeToRouterEvents();
  }

  /**
   * Abonne aux événements de navigation du Router
   */
  private subscribeToRouterEvents(): void {
    this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.setHeaderVisibility(event.url);
      }
    });
  }

  // Modifie la visibilité du header
  private setHeaderVisibility(url: string): void {
    this.isHeaderVisible = !this.isRouteHidden(url);
  }

  // Vérifie si la route fait partie des routes cachant le header
  private isRouteHidden(url: string): boolean {
    return this.hiddenHeaderRoutes.includes(url);
  }

}
