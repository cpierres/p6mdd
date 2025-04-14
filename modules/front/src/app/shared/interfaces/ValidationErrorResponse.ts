export interface ValidationErrorResponse {
  message: string; // Message général d'erreur
  fieldErrors?: { [key: string]: string }; // Erreurs spécifiques aux champs
}
