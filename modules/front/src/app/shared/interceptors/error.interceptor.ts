import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { ErrorHandlingService } from '../services/error-handling-service.service';
import { MessagesService } from '../services/messages.service';
import { catchError, throwError } from 'rxjs';
import { ApiResult } from '../interfaces/ApiResult';
import { ResponseDetails } from '../interfaces/ResponseDetails';
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
          const apiResult = error.error as ApiResult<ResponseDetails>;

          // Si nous avons un ApiResult avec des données
          if (apiResult.data) {
            const responseDetails = apiResult.data;

            // Gestion des erreurs de validation avec la nouvelle structure
            if (responseDetails.fieldErrors && responseDetails.fieldErrors.length > 0) {
              errorHandlingService.handleValidationErrors(responseDetails);
            }

            // Affichage du message général avec la sévérité appropriée
            if (responseDetails.message) {
              const severity = responseDetails.severity || 'error';
              messagesService.showMessage(responseDetails.message, severity as MessageSeverity);
            }
          } else {
            // Si nous avons un ApiResult sans données, utiliser le message général
            if (apiResult.message) {
              messagesService.showMessage(apiResult.message, 'error');
            }
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
        if (error.error && typeof error.error === 'object') {
          const apiResult = error.error as ApiResult<ResponseDetails>;

          if (apiResult.data) {
            const responseDetails = apiResult.data;
            const errorMessage = responseDetails.message || 'Une erreur est survenue';
            const severity = responseDetails.severity || 'error';

            messagesService.showMessage(errorMessage, severity as MessageSeverity);
          } else {
            // Si nous avons un ApiResult sans données, utiliser le message général
            messagesService.showMessage(apiResult.message || 'Une erreur est survenue', 'error');
          }
        } else {
          // Fallback pour les erreurs non structurées
          messagesService.showMessage('Une erreur est survenue', 'error');
        }
      }

      // Retransmettre l'erreur pour que les composants puissent la traiter si nécessaire
      return throwError(() => error);
    })
  );
};
