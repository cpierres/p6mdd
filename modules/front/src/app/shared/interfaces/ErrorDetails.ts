import { MessageSeverity } from '../models/MessageSeverity';
import { FieldErrorDetail } from './FieldErrorDetail';

/**
 * Représente les détails d'une erreur dans l'application.
 *
 * Cette interface correspond à la classe ErrorDetails du backend et est utilisée pour
 * transmettre des informations détaillées sur les erreurs survenues, notamment un message
 * général, le niveau de sévérité et les erreurs spécifiques associées aux champs.
 */
export interface ErrorDetails {
  /**
   * Message général décrivant l'erreur.
   */
  message: string;

  /**
   * Niveau de sévérité de l'erreur.
   */
  severity: MessageSeverity;

  /**
   * Liste des champs en erreur (facultatif).
   * Chaque entrée contient des informations détaillées telles que le nom du champ,
   * un message décrivant le problème, et une sévérité (optionnelle).
   */
  fieldErrors?: FieldErrorDetail[];
}
