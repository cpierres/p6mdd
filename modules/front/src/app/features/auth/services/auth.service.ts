import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {catchError, Observable, of, pipe, tap, throwError} from 'rxjs';
import {RegisterRequest} from '../interfaces/registerRequest.interface';
import {AuthSuccess} from '../interfaces/authSuccess.interface';
//import {MessagesService} from '../../../shared/services/messages.service';
import {environment} from '../../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private pathService = environment.baseUrl+'auth';

  constructor(private http: HttpClient) {// private messagesService: MessagesService) {
    console.log('AuthService constructor');
  }

  public register(registerRequest: RegisterRequest): Observable<AuthSuccess> {
    // TODO MENTOR2 :j'ai modifié le code du projet 3 frontend (ton code ;-)
    // utilisation d'un pipe pour traiter le flux dans le service avant utilisation par le composant. Correct ?
    // Dans le composant register, subscribe du projet 3 est déprécié
    return this.http.post<AuthSuccess>(`${this.pathService}/register`, registerRequest).pipe(
      tap((response: AuthSuccess) => {
        localStorage.setItem('token', response.token);
      }),
      catchError(error => {
        //TODO MENTOR2 correct ? comment mieux traiter les erreurs ?
        console.error('Erreur lors de l\'inscription :', error);
        return throwError(() => error);
      })
    );
  }

}
