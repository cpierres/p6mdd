import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {catchError, Observable, tap, throwError} from 'rxjs';
import {RegisterRequest} from '../interfaces/registerRequest.interface';
import {AuthSuccess} from '../interfaces/authSuccess.interface';
//import {MessagesService} from '../../../shared/services/messages.service';
import {environment} from '../../../../environments/environment';
import {User} from '../../user/interfaces/user.interface';
import {SessionService} from '../../../shared/services/session-service.service';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private pathService = environment.baseUrl + 'auth';

  constructor(private http: HttpClient,
              private sessionService: SessionService,
              ) {// private messagesService: MessagesService) {

  }

  public register(registerRequest: RegisterRequest): Observable<AuthSuccess> {
    // TODO MENTOR2 :j'ai modifié le code du projet 3 frontend pour éviter deprecatead v19
    // utilisation d'un pipe pour traiter le flux dans le service avant utilisation par le composant.
    // le routage se fera plutôt dans le composant appelant (SOLID : SRP)
    // Dans le composant register, subscribe du projet 3 est déprécié
    return this.http.post<AuthSuccess>(`${this.pathService}/register`, registerRequest).pipe(
      tap((response: AuthSuccess) => {
        localStorage.setItem('token', response.token);
        this.me().subscribe((user: User) => {
          this.sessionService.logIn(user);
        });
      }),
      catchError(error => {
        //TODO MENTOR2 correct ? comment mieux traiter les erreurs ?
        console.error('Erreur lors de l\'inscription :', error);
        return throwError(() => error);
      })
    );
  }

  public me(): Observable<User> {
    return this.http.get<User>(`${this.pathService}/me`);
  }

}
