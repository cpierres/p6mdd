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
import {ValidationErrorResponse} from '../../../shared/interfaces/ValidationErrorResponse';
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
    return this.http.post<AuthSuccess>(`${this.pathService}/register`, registerRequest).pipe(
      tap((response: AuthSuccess) => {
        //en cas de succès, on authentifie directement le nouvel utilisateur
        localStorage.setItem('token', response.token);
      }),
      // Utiliser switchMap pour enchaîner l'appel à me() à l'observable principal
      // afin que l'observable ne se termine pas tant que l'utilisateur n'est pas complètement connecté
      switchMap((response: AuthSuccess) => {
        return this.me().pipe(
          tap((user: User) => {
            this.sessionService.logIn(user);
          }),
          // Retourner la réponse originale
          map(() => response)
        );
      }),
      catchError(error => {
        return this.handleValidationErrors(error);
      })
    );
  }

  private handleValidationErrors(error: HttpErrorResponse) {
    if (error.status === 409 && error.error.fieldErrors) {
      // Retourner directement ValidationErrorResponse pour gestion des erreurs backend
      return throwError(() => error.error as ValidationErrorResponse);
    } else if (error.status === 400 && error.error.fieldErrors) {
      return throwError(() => error.error as ValidationErrorResponse);
    }
    //erreur générale (message simple sans détail par champ) affichée via message réactif en entête de page
    this.messagesService.showMessage(
      'Erreur lors de l\'inscription : ' + error.error.message,
      "error" //niveau de l'erreur (c 1 type)
    );
    return throwError(() => error);
  }

  public me(): Observable<User> {
    return this.http.get<User>(`${this.pathService}/me`);
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
    return this.http.put<AuthSuccess>(`${this.pathService}/me`, userUpdate).pipe(
      tap((response: AuthSuccess) => {
        // Stocker le nouveau token
        localStorage.setItem('token', response.token);
        // Rafraîchir les informations utilisateur
        this.me().subscribe((user: User) => {
          this.sessionService.logIn(user);
        });
      }),
      catchError(error => {
        return this.handleValidationErrors(error);
      })
    );
  }

  public login(request: LoginRequest): Observable<AuthSuccess> {
    return this.http.post<AuthSuccess>(`${this.pathService}/login`, request).pipe(
      tap((response: AuthSuccess) => {
        // Stocker le token JWT retourné par le backend
        localStorage.setItem('token', response.token);
      }),
      // Utiliser switchMap pour enchaîner l'appel à me() à l'observable principal
      switchMap((response: AuthSuccess) => {
        return this.me().pipe(
          tap((user: User) => {
            this.sessionService.logIn(user);
          }),
          // Retourner la réponse originale
          map(() => response)
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
