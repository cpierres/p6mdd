import { Injectable } from '@angular/core';
import {ValidationErrorResponse} from '../interfaces/ValidationErrorResponse';

@Injectable({
  providedIn: 'root'
})
export class ErrorHandlingService {
  private fieldErrors: { [key: string]: string } = {};

  /**
   * Traite les erreurs de validation et met à jour les erreurs associées aux champs.
   * @param error L'erreur retournée par l'API
   */
  handleValidationErrors(error: ValidationErrorResponse): { [key: string]: string } {
    if (error.fieldErrors) {
      this.fieldErrors = error.fieldErrors;
    }
    return this.fieldErrors;
  }

  /**
   * Réinitialise les erreurs associées aux champs.
   */
  resetFieldErrors(): void {
    this.fieldErrors = {};
  }

  /**
   * Retourne les erreurs associées aux champs.
   */
  getFieldErrors(): { [key: string]: string } {
    return this.fieldErrors;
  }
}

