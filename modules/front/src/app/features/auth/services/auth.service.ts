import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {catchError, Observable, tap, throwError} from 'rxjs';
import {RegisterRequest} from '../interfaces/registerRequest.interface';
import {AuthSuccess} from '../interfaces/authSuccess.interface';
//import {MessagesService} from '../../../shared/services/messages.service';
import {environment} from '../../../../environments/environment';
import {User} from '../../user/interfaces/user.interface';
import {SessionService} from '../../../shared/services/session-service.service';
import {UserUpdate} from '../../user/interfaces/user-update.interface';
import {MessagesService} from '../../../shared/services/messages.service';
import {ValidationErrorResponse} from '../../../shared/interfaces/ValidationErrorResponse';

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
        this.me().subscribe((user: User) => {
          this.sessionService.logIn(user);
        });
      }),
      catchError(error => {
        if (error.status === 409 && error.error.fieldErrors) {
          // Retourner directement ValidationErrorResponse pour gestion des erreurs backend
          return throwError(() => error.error as ValidationErrorResponse);
        }
        //erreur générale (message simple sans détail par champ) affichée via message réactif en entête de page
        this.messagesService.showMessage(
          'Erreur lors de l\'inscription : ' + error.error.message,
          "error" //niveau de l'erreur (c 1 type)
        );
        return throwError(() => error);
      })
    );
  }

  public me(): Observable<User> {
    return this.http.get<User>(`${this.pathService}/me`);
  }

  public updateMe(userUpdate: UserUpdate): Observable<AuthSuccess> {
    return this.http.put<AuthSuccess>(`${this.pathService}/me`, userUpdate).pipe(
      tap((response: AuthSuccess) => {
        //console.log("updateMe - token; " + response.token)
      }),
      catchError(error => {
        if (error.status === 409 && error.error.fieldErrors) {
          // Retourner directement ValidationErrorResponse pour gestion des erreurs backend
          return throwError(() => error.error as ValidationErrorResponse);
        }
        //erreur générale (message simple sans détail par champ) affichée via message réactif en entête de page
        this.messagesService.showMessage(
          'Erreur lors de l\'inscription : ' + error.error.message,
          "error" //niveau de l'erreur (c 1 type)
        );
        return throwError(() => error);
      })
    );
  }

}
