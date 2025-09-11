import { Injectable, OnInit } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { User } from '../../features/user/interfaces/user.interface';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { ApiResult } from '../interfaces/ApiResult';
import { TokenService } from './token.service';
import { catchError, tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class SessionService implements OnInit {

  public isLogged = false;
  public user: User | undefined;

  private isLoggedSubject = new BehaviorSubject<boolean>(this.isLogged);

  constructor(private http: HttpClient, private tokenService: TokenService) {
    // Appeler checkToken immédiatement dans le constructeur
    this.checkToken();
  }

  ngOnInit(): void {
    // Vérifier également lors de l'initialisation du service
    this.checkToken();
  }

  //pour gérer le cas d'un refresh du browser (on perdait le menu)
  private checkToken(): void {
    if (this.tokenService.hasToken()) {
      // Si un token existe, récupérer les informations de l'utilisateur
      this.http.get<ApiResult<User>>(`${environment.backendUrl}auth/me`, {
        withCredentials: true // Important pour envoyer le cookie HttpOnly
      }).subscribe({
        next: (apiResult: ApiResult<User>) => {
          if (apiResult.data) {
            this.logIn(apiResult.data);
          } else {
            // Si data est null, tenter de rafraîchir le token
            this.refreshTokenAndGetUser();
          }
        },
        error: () => {
          // En cas d'erreur, tenter de rafraîchir le token
          this.refreshTokenAndGetUser();
        }
      });
    }
  }

  /**
   * Tente de rafraîchir le token et de récupérer les informations utilisateur
   */
  private refreshTokenAndGetUser(): void {
    this.tokenService.refreshToken().pipe(
      tap(() => {
        // Si le rafraîchissement réussit, récupérer les informations utilisateur
        this.http.get<ApiResult<User>>(`${environment.backendUrl}auth/me`, {
          withCredentials: true
        }).subscribe({
          next: (apiResult: ApiResult<User>) => {
            if (apiResult.data) {
              this.logIn(apiResult.data);
            } else {
              this.logOut();
            }
          },
          error: () => this.logOut()
        });
      }),
      catchError(() => {
        // En cas d'échec du rafraîchissement, déconnecter l'utilisateur
        this.logOut();
        return of(null);
      })
    ).subscribe();
  }

  public $isLogged(): Observable<boolean> {
    return this.isLoggedSubject.asObservable();
  }

  public logIn(user: User): void {
    this.user = user;
    this.isLogged = true;
    //console.log('SessionService.logIn - isLogged :', this.isLogged, 'user :', this.user);
    this.next();
  }

  public logOut(): void {
    this.tokenService.clearToken();
    this.user = undefined;
    this.isLogged = false;
    this.next();
  }

  private next(): void {
    this.isLoggedSubject.next(this.isLogged);
  }
}
