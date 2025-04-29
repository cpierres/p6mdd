import {FieldErrors} from '../models/FieldErrors';
import {MessageSeverity} from '../models/MessageSeverity';

/**
 * Représente une réponse indiquant la présence d'erreurs de validation.
 *
 * Cette interface est utilisée pour transmettre des informations sur les erreurs
 * qui se produisent lors de la validation des données, y compris un message d'erreur
 * général et des détails spécifiques sur les champs en erreur.
 *
 * Cette structure correspond à la structuration des erreurs définie côté backend.
 *
 * Propriétés :
 * - `message` : Contient un message d'erreur général décrivant la nature du problème.
 * - `fieldErrors` : Optionnel. Contient une collection d'erreurs spécifiques aux champs,
 *   où chaque clé représente un champ spécifique et sa valeur associe le détail de l'erreur.
 */
export interface ValidationErrorResponse {
  message: string; // Message général d'erreur
  severity?: MessageSeverity;
  fieldErrors?: FieldErrors; // Erreurs spécifiques aux champs
}
