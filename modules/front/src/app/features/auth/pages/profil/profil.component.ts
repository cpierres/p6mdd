import {Component, OnInit} from '@angular/core';
import {UserFormComponent} from '../../components/user-form/user-form.component';
import {RegisterRequest} from '../../interfaces/registerRequest.interface';
import {BehaviorSubject} from 'rxjs';
import {User} from '../../../user/interfaces/user.interface';
import {AuthService} from '../../services/auth.service';

@Component({
  selector: 'app-profil',
  imports: [
    UserFormComponent
  ],
  templateUrl: './profil.component.html',
  styleUrl: './profil.component.css'
})
export class ProfilComponent implements OnInit {
  labelSubmit: string = "Sauvegarder";
  headTitle: string = "Profil utilisateur";
  initialEditMode: boolean = false;

  currentUser$: BehaviorSubject<User | null> = new BehaviorSubject<User | null>(null);

  constructor(private authService: AuthService) {
  }

  ngOnInit(): void {
    // Charger les données utilisateur
    this.authService.me().subscribe((user: User) => {
      this.currentUser$.next(user);
    });
  }


  handleFormSubmit(registerRequest: RegisterRequest): void {

  }

}
