import {Component, OnInit} from '@angular/core';
import {UserFormComponent} from '../../components/user-form/user-form.component';
import {RegisterRequest} from '../../interfaces/registerRequest.interface';
import {BehaviorSubject} from 'rxjs';
import {User} from '../../../user/interfaces/user.interface';
import {AuthService} from '../../services/auth.service';
import {AuthSuccess} from '../../interfaces/authSuccess.interface';
import {Router} from '@angular/router';
import {UserUpdate} from '../../../user/interfaces/user-update.interface';
import {MatButton} from '@angular/material/button';
import {SessionService} from '../../../../shared/services/session-service.service';

@Component({
  selector: 'app-profil',
  imports: [
    UserFormComponent,
    MatButton,
    MatButton
  ],
  templateUrl: './profil.component.html',
  styleUrl: './profil.component.css'
})
export class ProfilComponent implements OnInit {
  labelSubmit: string = "Sauvegarder";
  headTitle: string = "Profil utilisateur";
  initialEditMode: boolean = false;

  currentUser$: BehaviorSubject<User | null> = new BehaviorSubject<User | null>(null);
  public onError = false;

  constructor(private authService: AuthService, private router: Router) {
  }

  ngOnInit(): void {
    // Charger les données utilisateur
    this.authService.me().subscribe((user: User) => {
      this.currentUser$.next(user);
    });
  }

  handleFormSubmit(userUpdate: UserUpdate): void {
    this.authService.updateMe(userUpdate).subscribe({
      next: (response: AuthSuccess) => {
        this.router.navigate(['/post/list']);
      },
      error: () => {
        this.onError = true;
      }
    });
  }


}
