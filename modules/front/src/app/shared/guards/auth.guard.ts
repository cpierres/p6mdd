import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { SessionService } from '../services/session-service.service';

/**
 * Guard d'authentification qui vérifie si l'utilisateur est connecté
 *
 * Ce guard est utilisé pour protéger les routes qui nécessitent une authentification.
 * Si l'utilisateur n'est pas connecté, il est redirigé vers la page de connexion.
 *
 * @returns true si l'utilisateur est connecté, sinon redirige vers la page de connexion
 */
export const authGuard: CanActivateFn = (route, state) => {
  const sessionService = inject(SessionService);
  const router = inject(Router);

  // Vérifier si l'utilisateur est connecté
  if (sessionService.isLogged) {
    return true;
  }

  // Si l'utilisateur n'est pas connecté, rediriger vers la page de connexion
  return router.parseUrl('/auth/login');
};
