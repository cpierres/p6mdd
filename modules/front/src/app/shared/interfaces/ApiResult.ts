/**
 * Interface représentant la structure standard des réponses d'API.
 *
 * Cette interface correspond à la classe ApiResult du backend et est utilisée pour
 * standardiser les réponses d'API, qu'il s'agisse de succès ou d'erreurs.
 */
export interface ApiResult<T> {
  /**
   * Message général décrivant la réponse.
   */
  message: string;

  /**
   * Code HTTP associé à cette réponse.
   */
  status: number;

  /**
   * Données de la réponse. Peut être null en cas d'erreur.
   */
  data: T | null;

  /**
   * Horodatage de la réponse au format ISO 8601.
   */
  timestamp: string;

  /**
   * Identifiant unique de la requête, utile pour le suivi et le débogage.
   */
  requestId: string;
}
