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
    // Ne plus vérifier le localStorage, mais plutôt faire une requête /me
    // avec withCredentials: true pour vérifier si l'utilisateur est connecté
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
      error: () => {
        this.logOut();
      }
    });
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
    // Ne plus supprimer le token du localStorage
    this.user = undefined;
    this.isLogged = false;
    this.next();
  }

  private next(): void {
    this.isLoggedSubject.next(this.isLogged);
  }
}
