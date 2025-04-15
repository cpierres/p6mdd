import {Component, OnInit} from '@angular/core';
import {UserFormComponent} from '../../components/user-form/user-form.component';
import {BehaviorSubject} from 'rxjs';
import {User} from '../../../user/interfaces/user.interface';
import {AuthService} from '../../services/auth.service';
import {AuthSuccess} from '../../interfaces/authSuccess.interface';
import {Router} from '@angular/router';
import {UserUpdate} from '../../../user/interfaces/user-update.interface';
import {ErrorHandlingService} from '../../../../shared/services/error-handling-service.service';

@Component({
  selector: 'app-profil',
  imports: [
    UserFormComponent,
  ],
  templateUrl: './profil.component.html',
  styleUrl: './profil.component.scss'
})
export class ProfilComponent implements OnInit {
  labelSubmit: string = "Sauvegarder";
  headTitle: string = "Profil utilisateur";
  initialEditMode: boolean = false;
  context: string = "profil";

  currentUser$: BehaviorSubject<User | null> = new BehaviorSubject<User | null>(null);
  public onError = false;
  backendFieldErrors: { [key: string]: string } = {};

  constructor(private authService: AuthService, private router: Router,
              private errorHandlingService: ErrorHandlingService) {
  }

  ngOnInit(): void {
    // Charger les données utilisateur pour alimenter la page
    this.authService.me().subscribe((user: User) => {
      this.currentUser$.next(user);
    });
  }

  handleFormSubmit(userUpdate: UserUpdate): void {
    // Comparer l'email dans userUpdate avec celui de currentUser$
    const currentEmail = this.currentUser$.value?.email;
    const isEmailModified = currentEmail !== userUpdate.email;

    this.authService.updateMe(userUpdate).subscribe({
      next: (response: AuthSuccess) => {
        // Si l'email a changé, rediriger vers la route de déconnexion
        if (isEmailModified) {
          this.router.navigate(['/logout']);
        } else {
          // Sinon, revenir en mode lecture
          this.initialEditMode = false; // Désactive la modification
        }
      },
      error: (error) => {
        this.onError = true;
        this.errorHandlingService.handleValidationErrors(error);
        this.backendFieldErrors = this.errorHandlingService.getFieldErrors();
      }
    });
  }


}
