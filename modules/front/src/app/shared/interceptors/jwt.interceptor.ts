import { HttpErrorResponse, HttpHandler, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { TokenService } from '../services/token.service';
import { catchError, switchMap, throwError } from 'rxjs';

// Définit l'intercepteur comme une fonction compatible avec Angular 19
export const jwtInterceptor: HttpInterceptorFn = (request, next) => {
  const tokenService = inject(TokenService);

  // Exclure certaines routes de l'application de l'intercepteur
  const publicRoutes = ['/api/auth/register', '/api/auth/login', '/api/auth/refresh'];

  // Vérifier si la requête correspond à l'une des routes publiques
  if (publicRoutes.some(route => request.url.includes(route))) {
    return next(request); // Skip l'intercepteur
  }

  // Ajouter le token s'il existe
  const token = tokenService.token();
  if (token) {
    request = addTokenToRequest(request, token);
  }

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      // Si erreur 401 (Unauthorized), tenter de rafraîchir le token
      if (error.status === 401 && !request.url.includes('/api/auth/refresh')) {
        return tokenService.refreshToken().pipe(
          switchMap(newToken => {
            // Répéter la requête originale avec le nouveau token
            const clonedRequest = addTokenToRequest(request, newToken);
            return next(clonedRequest);
          }),
          catchError(refreshError => {
            // Si le rafraîchissement échoue, déconnecter l'utilisateur
            tokenService.clearToken();
            return throwError(() => refreshError);
          })
        );
      }
      return throwError(() => error);
    })
  );
};

function addTokenToRequest(request: any, token: string): any {
  return request.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`,
    },
  });
};
