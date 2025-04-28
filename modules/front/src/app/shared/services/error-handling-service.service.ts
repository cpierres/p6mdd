import {Injectable, signal} from '@angular/core';
import {ValidationErrorResponse} from '../interfaces/ValidationErrorResponse';
import {FieldErrors} from '../interfaces/FieldErrors';

@Injectable({
  providedIn: 'root'
})
export class ErrorHandlingService {
  // Signal pour les erreurs de champs
  private fieldErrorsSignal = signal<FieldErrors>({});

  // Signal pour les messages d'erreur généraux
  private generalErrorSignal = signal<string | null>(null);

  // Signals en lecture seule exposés publiquement
  readonly fieldErrors = this.fieldErrorsSignal.asReadonly();
  readonly generalError = this.generalErrorSignal.asReadonly();

  // Traite les erreurs de validation
  handleValidationErrors(error: ValidationErrorResponse): void {
    if (error.fieldErrors) {
      this.fieldErrorsSignal.set(error.fieldErrors);
    }

    if (error.message) {
      this.generalErrorSignal.set(error.message);
    }
  }

  // Méthode pour récupérer uniquement les erreurs liées aux champs (facultatif en fonction des besoins)
  getFieldErrors(): FieldErrors {
    return this.fieldErrors();
  }

  // Réinitialise les erreurs
  resetErrors(): void {
    this.fieldErrorsSignal.set({});
    this.generalErrorSignal.set(null);
  }
}
