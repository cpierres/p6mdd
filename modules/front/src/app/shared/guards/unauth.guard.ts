import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { SessionService } from '../services/session-service.service';

/**
 * Guard qui empêche les utilisateurs déjà connectés d'accéder à certaines pages
 *
 * Ce guard est utilisé pour protéger les routes qui ne devraient pas être accessibles
 * aux utilisateurs authentifiés, comme le login ou le register.
 * Si l'utilisateur est déjà connecté, il est redirigé vers la page d'accueil.
 *
 * @returns true si l'utilisateur n'est PAS connecté, sinon redirige vers la page d'accueil
 */
export const unauthGuard: CanActivateFn = (route, state) => {
  const sessionService = inject(SessionService);
  const router = inject(Router);

  // Si l'utilisateur n'est PAS connecté, autoriser l'accès
  if (!sessionService.isLogged) {
    return true;
  }

  // Si l'utilisateur est déjà connecté, rediriger vers la page d'accueil
  return router.parseUrl('/home');
};
