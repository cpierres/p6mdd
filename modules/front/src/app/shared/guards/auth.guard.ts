import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { SessionService } from '../services/session-service.service';
import {catchError, map, of, switchMap, take} from 'rxjs';
import {TokenService} from '../services/token.service';

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
  const tokenService = inject(TokenService);
  const router = inject(Router);

  return sessionService.$isLogged().pipe(
    take(1),
    switchMap(isLogged => {
      if (isLogged) {
        return of(true);
      }

      // Si l'utilisateur n'est pas connecté, tenter de rafraîchir le token
      return tokenService.refreshToken().pipe(
        // Si le rafraîchissement réussit, autoriser l'accès
        map(() => true),
        catchError(() => {
          // Si le rafraîchissement échoue, rediriger vers la page de connexion
          return of(router.parseUrl('/auth/login'));
        })
      );
    })
  );
};

