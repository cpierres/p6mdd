import { MessageSeverity } from '../models/MessageSeverity';
import { FieldErrorDetail } from './FieldErrorDetail';

/**
 * Représente les détails d'une réponse dans l'application.
 *
 * Cette interface correspond à la classe ResponseDetails du backend et est utilisée pour
 * transmettre des informations détaillées sur les réponses, qu'il s'agisse d'erreurs ou
 * d'informations utiles. Elle remplace les anciennes interfaces ErrorDetails et ValidationErrorResponse.
 */
export interface ResponseDetails {
  /**
   * Message général décrivant la réponse.
   */
  message: string;

  /**
   * Niveau de sévérité de la réponse.
   */
  severity: MessageSeverity;

  /**
   * Liste des champs en erreur (facultatif).
   * Chaque entrée contient des informations détaillées telles que le nom du champ,
   * un message décrivant le problème, et une sévérité (optionnelle).
   */
  fieldErrors?: FieldErrorDetail[];
}
