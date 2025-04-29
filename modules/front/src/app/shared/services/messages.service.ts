import {Injectable, signal} from "@angular/core";
import {Message} from "../models/message.model";
import {MessageSeverity} from '../models/MessageSeverity';
import {MatSnackBar, MatSnackBarConfig} from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root'
})
/**
 * Service de messages utilisant MatSnackBar pour l'affichage.
 * Dans le projet 2 OCR, j'avais créé un service messages réactif basé sur BehaviorSubject.
 * Celui-ci est basé sur Signal.
 */
export class MessagesService {
  // Injection du service MatSnackBar
  constructor(private snackBar: MatSnackBar) {
  }

  // Signal privé pour stocker le message actuel
  #messageSignal = signal<Message | null>(null);

  // Signal public en lecture seule
  message = this.#messageSignal.asReadonly();

  /**
   * Affiche un message avec la sévérité spécifiée.
   * @param text Le texte du message à afficher
   * @param severity La sévérité du message (error, warning, info, success)
   * @param duration Durée d'affichage en millisecondes (optionnel)
   */
  showMessage(text: string, severity: MessageSeverity = 'warning', duration?: number) {
    // Mise à jour du signal pour compatibilité avec le code existant
    this.#messageSignal.set({
      text, severity
    });

    // Configuration du SnackBar en fonction de la sévérité
    const config: MatSnackBarConfig = {
      // Durée par défaut en fonction de la sévérité
      duration: duration || this.getDurationBySeverity(severity),
      // Classes CSS pour le style
      panelClass: [`snackbar-${severity}`, 'preserve-newlines'],
      // Position en haut au centre (comme votre composant actuel)
      horizontalPosition: 'center',
      verticalPosition: 'top',
    };

    // Ouverture du SnackBar avec le texte et un bouton de fermeture
    this.snackBar.open(text, 'Fermer', config);
  }

  /**
   * Efface le message actuel.
   */
  clear() {
    // Réinitialisation du signal
    this.#messageSignal.set(null);
    // Fermeture du SnackBar
    this.snackBar.dismiss();
  }

  /**
   * Détermine la durée d'affichage en fonction de la sévérité.
   * @param severity La sévérité du message
   * @returns La durée en millisecondes
   */
  private getDurationBySeverity(severity: MessageSeverity): number {
    switch (severity) {
      case 'error':
        return 8000; // Les erreurs restent plus longtemps
      case 'warning':
        return 5000;
      case 'info':
        return 4000;
      case 'success':
        return 3000;
      default:
        return 5000;
    }
  }
}
