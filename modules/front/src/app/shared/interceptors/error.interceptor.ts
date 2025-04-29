import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { ErrorHandlingService } from '../services/error-handling-service.service';
import { catchError, throwError } from 'rxjs';
import { ValidationErrorResponse } from '../interfaces/ValidationErrorResponse';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  // Injection du service de gestion d'erreurs
  const errorHandlingService = inject(ErrorHandlingService);

  // Réinitialiser les erreurs avant chaque requête
  errorHandlingService.resetErrors();

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 400 || error.status === 409) {
        // Erreurs de validation / conflit
        if (error.error && (error.error.fieldErrors || error.error.message)) {
          errorHandlingService.handleValidationErrors(error.error as ValidationErrorResponse);
        }
      } else if (error.status === 401) {
        // Erreurs d'authentification
        // ajouter ici une redirection vers la page de login
        // ou une logique pour gérer la déconnexion
      } else if (error.status === 403) {
        // Erreurs d'autorisation
        // Gérer les erreurs d'accès non autorisé
      } else {
        // Autres erreurs (500, etc.)
        // Gérer les erreurs serveur génériques
        const errorMessage = error.error?.message || 'Une erreur est survenue';
        errorHandlingService.handleValidationErrors({
          message: errorMessage
        });
      }

      // Retransmettre l'erreur pour que les composants puissent la traiter si nécessaire
      return throwError(() => error);
    })
  );
};
