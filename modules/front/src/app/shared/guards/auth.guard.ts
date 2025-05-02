import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { SessionService } from '../services/session-service.service';
import {map, take} from 'rxjs';

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

  return sessionService.$isLogged().pipe(
    take(1),
    map(isLogged => {
      if (isLogged) {
        return true;
      }
      return router.parseUrl('/auth/login');
    })
  );
};
