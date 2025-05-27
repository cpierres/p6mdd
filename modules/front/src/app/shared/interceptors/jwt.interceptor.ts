import { HttpHandler, HttpInterceptorFn } from '@angular/common/http';

// Définit l'intercepteur comme une fonction compatible avec Angular 19
export const jwtInterceptor: HttpInterceptorFn = (request, next) => {
  // Les cookies HttpOnly sont envoyés automatiquement
  // Pas besoin d'ajouter manuellement le token

  // Ajouter withCredentials: true à toutes les requêtes
  if (!request.withCredentials) {
    request = request.clone({
      withCredentials: true
    });
  }

  return next(request);
};
