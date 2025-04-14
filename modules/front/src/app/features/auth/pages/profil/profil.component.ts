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

  currentUser$: BehaviorSubject<User | null> = new BehaviorSubject<User | null>(null);
  public onError = false;
  backendFieldErrors: { [key: string]: string } = {};

  constructor(private authService: AuthService, private router: Router,
              private errorHandlingService: ErrorHandlingService) {
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
      error: (error) => {
        this.onError = true;
        this.errorHandlingService.handleValidationErrors(error);
        this.backendFieldErrors = this.errorHandlingService.getFieldErrors();
      }
    });
  }


}
