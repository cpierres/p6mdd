import { Injectable, signal } from '@angular/core';
import { ErrorDetails } from '../interfaces/ErrorDetails';
import { FieldErrorDetail } from '../interfaces/FieldErrorDetail';
import { MessageSeverity } from '../models/MessageSeverity';

@Injectable({
  providedIn: 'root'
})
export class ErrorHandlingService {
  // Signal pour les erreurs de champs
  private fieldErrorsSignal = signal<FieldErrorDetail[]>([]);

  // Signal pour les messages d'erreur généraux
  private generalErrorSignal = signal<string | null>(null);

  // Signal pour la sévérité du message général
  private generalErrorSeveritySignal = signal<MessageSeverity>('error');

  // Signals en lecture seule exposés publiquement
  readonly fieldErrors = this.fieldErrorsSignal.asReadonly();
  readonly generalError = this.generalErrorSignal.asReadonly();
  readonly generalErrorSeverity = this.generalErrorSeveritySignal.asReadonly();

  // Traite les erreurs de validation
  handleValidationErrors(error: ErrorDetails): void {
    if (error.fieldErrors && error.fieldErrors.length > 0) {
      this.fieldErrorsSignal.set(error.fieldErrors);
    }

    if (error.message) {
      this.generalErrorSignal.set(error.message);
      this.generalErrorSeveritySignal.set(error.severity || 'error');
    }
  }

  // Méthode pour récupérer uniquement les erreurs liées aux champs
  getFieldErrors(): FieldErrorDetail[] {
    return this.fieldErrors();
  }

  // Méthode pour récupérer l'erreur d'un champ spécifique
  getFieldError(fieldName: string): FieldErrorDetail | undefined {
    return this.fieldErrors().find(error => error.field === fieldName);
  }

  // Réinitialise les erreurs
  resetErrors(): void {
    this.fieldErrorsSignal.set([]);
    this.generalErrorSignal.set(null);
    this.generalErrorSeveritySignal.set('error');
  }

  clearFieldError(fieldName: string): void {
    const currentErrors = this.fieldErrors();
    const updatedErrors = currentErrors.filter(error => error.field !== fieldName);
    this.fieldErrorsSignal.set(updatedErrors);
  }

}
