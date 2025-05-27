import { APP_INITIALIZER, ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { TokenService } from './shared/services/token.service';
import { catchError, firstValueFrom, of } from 'rxjs';

/**
 * Fonction d'initialisation qui tente de rafraîchir le token au démarrage de l'application
 * @param tokenService Service de gestion des tokens
 * @returns Une fonction qui tente de rafraîchir le token
 */
function initializeApp(tokenService: TokenService) {
  return () => {
    // Tenter de rafraîchir le token silencieusement
    return firstValueFrom(
      tokenService.refreshToken().pipe(
        catchError(() => {
          // Échec silencieux - l'utilisateur n'est simplement pas connecté
          console.log('Aucun refresh token valide disponible');
          return of(null);
        })
      )
    );
  };
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    {
      provide: APP_INITIALIZER,
      useFactory: initializeApp,
      deps: [TokenService],
      multi: true
    }
  ]
};
