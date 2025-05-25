import { MessageSeverity } from '../models/MessageSeverity';
/**
 * Détail d'une erreur pour un champ donné, incluant un niveau de sévérité.
 */
export interface FieldErrorDetail {
  field: string;    // Nom du champ, ex : "email", "password"
  message: string;  // Message associé à l'erreur, ex : "L'adresse email n'est pas valide."
  severity: MessageSeverity; // Niveau de sévérité : error, warning, info, ...
}
