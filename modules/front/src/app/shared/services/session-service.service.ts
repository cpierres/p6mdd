import { Injectable, OnInit } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { User } from '../../features/user/interfaces/user.interface';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { ApiResult } from '../interfaces/ApiResult';

@Injectable({
  providedIn: 'root'
})
export class SessionService implements OnInit {

  public isLogged = false;
  public user: User | undefined;

  private isLoggedSubject = new BehaviorSubject<boolean>(this.isLogged);

  constructor(private http: HttpClient) {
    // Appeler checkToken immédiatement dans le constructeur
    this.checkToken();
  }

  ngOnInit(): void {
    // Vérifier également lors de l'initialisation du service
    this.checkToken();
  }

  //pour gérer le cas d'un refresh du browser (on perdait le menu)
  private checkToken(): void {
    const token = localStorage.getItem('token');
    if (token) {
      // Si un token existe, récupérer les informations de l'utilisateur
      // Avec la nouvelle structure ApiResult, nous devons extraire l'utilisateur du champ data
      this.http.get<ApiResult<User>>(`${environment.backendUrl}auth/me`).subscribe({
        next: (apiResult: ApiResult<User>) => {
          if (apiResult.data) {
            this.logIn(apiResult.data);
          } else {
            // Si data est null, déconnecter l'utilisateur
            this.logOut();
          }
        },
        error: () => {
          // En cas d'erreur (token invalide), déconnecter l'utilisateur
          this.logOut();
        }
      });
    }
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
    localStorage.removeItem('token');
    this.user = undefined;
    this.isLogged = false;
    this.next();
  }

  private next(): void {
    this.isLoggedSubject.next(this.isLogged);
  }
}
