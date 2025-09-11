import { ApplicationConfig, provideZoneChangeDetection, provideEnvironmentInitializer, inject } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { TokenService } from './shared/services/token.service';
import {catchError, firstValueFrom, of, tap} from 'rxjs';

/**
 * Fonction d'initialisation qui tente de rafraîchir le token au démarrage de l'application
 */
function initializeApp() {
  const tokenService = inject(TokenService); // Injection manuelle d'Angular
  return async () => {
    try {
      // Ajouter un délai pour s'assurer que tout est initialisé
      //await new Promise(resolve => setTimeout(resolve, 1500));

      // Tenter de rafraîchir le token silencieusement
      await firstValueFrom(
        tokenService.refreshToken().pipe(
          tap(token => console.log("Token rafraîchi avec succès")),
          catchError(() => {
            console.log('Aucun refresh token valide disponible');
            return of(null);
          })
        )
      );
    } catch (error) {
      console.error(error);
    }
  };
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideEnvironmentInitializer(initializeApp), // Appel en tout premier du refresh token
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
  ]
};
