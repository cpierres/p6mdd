import {Injectable} from '@angular/core';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import {catchError, map, Observable, switchMap, tap, throwError} from 'rxjs';
import {RegisterRequest} from '../interfaces/registerRequest.interface';
import {AuthSuccess} from '../interfaces/authSuccess.interface';
//import {MessagesService} from '../../../shared/services/messages.service';
import {environment} from '../../../../environments/environment';
import {User} from '../../user/interfaces/user.interface';
import {SessionService} from '../../../shared/services/session-service.service';
import {UserUpdate} from '../../user/interfaces/user-update.interface';
import {MessagesService} from '../../../shared/services/messages.service';
import {ApiResult} from '../../../shared/interfaces/ApiResult';
import {ResponseDetails} from '../../../shared/interfaces/ResponseDetails';
import {LoginRequest} from '../interfaces/loginRequest.interface';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private pathService = environment.baseUrl + 'auth';

  constructor(private http: HttpClient,
              private sessionService: SessionService,
              private messagesService: MessagesService) {
  }

  public register(registerRequest: RegisterRequest): Observable<AuthSuccess> {
    // utilisation d'un pipe pour traiter le flux dans le service avant utilisation par le composant.
    // le routage se fera plutôt dans le composant appelant (SOLID : SRP)
    // Dans le composant register, subscribe du projet 3 est déprécié
    return this.http.post<ApiResult<AuthSuccess>>(`${this.pathService}/register`, registerRequest).pipe(
      tap((apiResult: ApiResult<AuthSuccess>) => {
        //en cas de succès, on authentifie directement le nouvel utilisateur
        if (apiResult.data && apiResult.data.token) {
          localStorage.setItem('token', apiResult.data.token);
        }
      }),
      // Utiliser switchMap pour enchaîner l'appel à me() à l'observable principal
      // afin que l'observable ne se termine pas tant que l'utilisateur n'est pas complètement connecté
      switchMap((apiResult: ApiResult<AuthSuccess>) => {
        return this.me().pipe(
          tap((user: User) => {
            this.sessionService.logIn(user);
          }),
          // Retourner la réponse originale (AuthSuccess)
          map(() => apiResult.data as AuthSuccess)
        );
      }),
      catchError(error => {
        return this.handleValidationErrors(error);
      })
    );
  }

  private handleValidationErrors(error: HttpErrorResponse) {
    if (error.error && typeof error.error === 'object') {
      const apiResult = error.error as ApiResult<ResponseDetails>;

      if (apiResult.data && apiResult.data.fieldErrors) {
        // Retourner directement l'erreur pour gestion des erreurs backend
        return throwError(() => apiResult);
      }
    }

    // Erreur générale
    this.messagesService.showMessage(
      'Erreur lors de l\'inscription : ' + (error.error?.message || 'Une erreur est survenue'),
      "error"
    );
    return throwError(() => error);
  }

  public me(): Observable<User> {
    return this.http.get<ApiResult<User>>(`${this.pathService}/me`).pipe(
      map((apiResult: ApiResult<User>) => {
        if (!apiResult.data) {
          throw new Error('User data not found in API response');
        }
        return apiResult.data;
      })
    );
  }

  // public updateMe(userUpdate: UserUpdate): Observable<AuthSuccess> {
  //   return this.http.put<AuthSuccess>(`${this.pathService}/me`, userUpdate).pipe(
  //     tap((response: AuthSuccess) => {
  //       //on se ré-authentifie au cas où l'utilisateur aurait changé son email pour actualiser le contexte de sécurité
  //       console.log(
  //         'AuthService.updateMe - token :',
  //         localStorage.getItem('token'))
  //       // ZZX TODO VERIFIER SI CA MARCHE A NOUVEAU
  //       // this.me().subscribe((user: User) => {
  //       //   this.sessionService.logIn(user);
  //       // });
  //       //this.sessionService.logOut();
  //     }),
  //     catchError(error => {
  //       return this.handleValidationErrors(error);
  //     })
  //   );
  // }
  public updateMe(userUpdate: UserUpdate): Observable<AuthSuccess> {
    return this.http.put<ApiResult<AuthSuccess>>(`${this.pathService}/me`, userUpdate).pipe(
      tap((apiResult: ApiResult<AuthSuccess>) => {
        // Stocker le nouveau token
        if (apiResult.data && apiResult.data.token) {
          localStorage.setItem('token', apiResult.data.token);
          // Rafraîchir les informations utilisateur
          this.me().subscribe((user: User) => {
            this.sessionService.logIn(user);
          });
        }
      }),
      // Transformer ApiResult<AuthSuccess> en AuthSuccess
      map((apiResult: ApiResult<AuthSuccess>) => apiResult.data as AuthSuccess),
      catchError(error => {
        return this.handleValidationErrors(error);
      })
    );
  }

  public login(request: LoginRequest): Observable<AuthSuccess> {
    return this.http.post<ApiResult<AuthSuccess>>(`${this.pathService}/login`, request).pipe(
      tap((apiResult: ApiResult<AuthSuccess>) => {
        // Stocker le token JWT retourné par le backend
        if (apiResult.data && apiResult.data.token) {
          localStorage.setItem('token', apiResult.data.token);
        }
      }),
      // Utiliser switchMap pour enchaîner l'appel à me() à l'observable principal
      switchMap((apiResult: ApiResult<AuthSuccess>) => {
        return this.me().pipe(
          tap((user: User) => {
            this.sessionService.logIn(user);
          }),
          // Retourner la réponse originale (AuthSuccess)
          map(() => apiResult.data as AuthSuccess)
        );
      }),
      catchError(error => {
        // Gestion des erreurs
        if (error.status === 401) {
          //erreur volontairement floue pour ne pas donner d'indication à un hacker
          this.messagesService.showMessage('Identifiant (email ou nom) ou mot de passe incorrect', 'error');
        } else {
          this.messagesService.showMessage('Une erreur est survenue lors de la connexion', 'error');
        }
        return throwError(() => error);
      })
    );
  }

}
