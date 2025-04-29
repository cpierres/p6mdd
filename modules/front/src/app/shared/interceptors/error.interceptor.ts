import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { ErrorHandlingService } from '../services/error-handling-service.service';
import { MessagesService } from '../services/messages.service';
import { catchError, throwError } from 'rxjs';
import { ErrorDetails } from '../interfaces/ErrorDetails';
import { MessageSeverity } from '../models/MessageSeverity';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  // Injection des services
  const errorHandlingService = inject(ErrorHandlingService);
  const messagesService = inject(MessagesService);

  // Réinitialiser les erreurs avant chaque requête
  errorHandlingService.resetErrors();

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 400 || error.status === 409) {
        // Erreurs de validation / conflit
        if (error.error) {
          const errorDetails = error.error as ErrorDetails;

          // Gestion des erreurs de validation avec la nouvelle structure
          if (errorDetails.fieldErrors && errorDetails.fieldErrors.length > 0) {
            errorHandlingService.handleValidationErrors(errorDetails);
          }

          // Affichage du message général avec la sévérité appropriée
          if (errorDetails.message) {
            const severity = errorDetails.severity || 'error';
            messagesService.showMessage(errorDetails.message, severity as MessageSeverity);
          }
        }
      } else if (error.status === 401) {
        // Erreurs d'authentification
        messagesService.showMessage('Vous devez vous connecter pour accéder à cette ressource', 'warning');
        // Ajouter ici une redirection vers la page de login si nécessaire
      } else if (error.status === 403) {
        // Erreurs d'autorisation
        messagesService.showMessage('Vous n\'avez pas les droits nécessaires pour accéder à cette ressource', 'warning');
      } else {
        // Autres erreurs (500, etc.)
        const errorDetails = error.error as ErrorDetails;
        const errorMessage = errorDetails?.message || 'Une erreur est survenue';
        const severity = errorDetails?.severity || 'error';

        messagesService.showMessage(errorMessage, severity as MessageSeverity);
      }

      // Retransmettre l'erreur pour que les composants puissent la traiter si nécessaire
      return throwError(() => error);
    })
  );
};
