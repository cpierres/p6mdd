/**
 * Interface représentant les erreurs liées à des champs spécifiques.
 *
 * Chaque clé correspond à un champ du formulaire, et la valeur est
 * un message décrivant l'erreur associée à ce champ.
 *
 * @example
 * ```
 * {
 *   "email": "Cet email existe déjà",
 *   "username": "Ce nom d'utilisateur existe déjà"
 * }
 * ```
 */
export interface FieldErrors {
  [field: string]: string; // Chaque champ (clé) a un message d'erreur associé
}
