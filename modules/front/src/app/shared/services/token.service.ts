import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError, switchMap, tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResult } from '../interfaces/ApiResult';
import { AuthSuccess } from '../../features/auth/interfaces/authSuccess.interface';

/**
 * Service de gestion des tokens d'authentification
 * Utilise les Signals d'Angular pour stocker le token en mémoire uniquement
 * et les cookies HttpOnly pour le refresh token
 */
@Injectable({
  providedIn: 'root'
})
export class TokenService {
  // Utilisation de Signal pour stocker le token en mémoire uniquement
  private accessToken = signal<string | null>(null);

  // Expose un accessToken en lecture seule
  public readonly token = this.accessToken.asReadonly();

  // Indicateur de rafraîchissement en cours
  private refreshInProgress = false;

  constructor(private http: HttpClient) {}

  /**
   * Définit le token d'accès et le stocke en mémoire
   */
  setToken(token: string): void {
    this.accessToken.set(token);
  }

  /**
   * Efface le token d'accès
   */
  clearToken(): void {
    this.accessToken.set(null);
    // Supprimer également le cookie de refresh token
    document.cookie = 'refresh_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
  }

  /**
   * Rafraîchit le token d'accès en utilisant le refresh token stocké dans un cookie HttpOnly
   */
  refreshToken(): Observable<string> {
    if (this.refreshInProgress) {
      return throwError(() => new Error('Refresh already in progress'));
    }

    this.refreshInProgress = true;

    return this.http.post<ApiResult<AuthSuccess>>(`${environment.backendUrl}auth/refresh`, {}, {
      withCredentials: true // Important pour envoyer le cookie HttpOnly
    }).pipe(
      tap(response => {
        if (response.data && response.data.token) {
          this.setToken(response.data.token);
        }
        this.refreshInProgress = false;
      }),
      switchMap(response => {
        if (response.data && response.data.token) {
          return of(response.data.token);
        }
        return throwError(() => new Error('No token in refresh response'));
      }),
      catchError(error => {
        this.refreshInProgress = false;
        this.clearToken();
        return throwError(() => error);
      })
    );
  }

  /**
   * Vérifie si un token est présent
   */
  hasToken(): boolean {
    return !!this.accessToken();
  }
}
