import {Component, OnInit, signal} from '@angular/core';
import {UserFormComponent} from '../../components/user-form/user-form.component';
import {Router} from '@angular/router';
import {AuthService} from '../../services/auth.service';
import {RegisterRequest} from '../../interfaces/registerRequest.interface';
import {AuthSuccess} from '../../interfaces/authSuccess.interface';
import {ErrorHandlingService} from '../../../../shared/services/error-handling-service.service';
import {User} from '../../../user/interfaces/user.interface';
import {ValidationErrorResponse} from '../../../../shared/interfaces/ValidationErrorResponse';

@Component({
  selector: 'app-register',
  imports: [
    UserFormComponent
  ],
  providers: [AuthService],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent implements OnInit {
  labelSubmit: string = "S'inscrire";
  headTitle: string = "Inscription";

  // Signals pour le mode édition et les données utilisateur
  isEditMode = signal<boolean>(true);
  currentUser = signal<User | null>(null);
  // backendFieldErrors inutile désormais puisque traité par error-interceptor
  // backendFieldErrors = signal<FieldErrors>({});

  constructor(private router: Router,
              private authService: AuthService,
              public errorHandlingService: ErrorHandlingService) {
  }

  ngOnInit(): void {
    this.headTitle = "Inscription";
    this.isEditMode.set(true);
    this.currentUser.set(null);
  }

  /**
   * Méthode pour gérer les données du formulaire envoyées depuis UserFormComponent
   * @param registerRequest
   */
  handleFormSubmit(registerRequest: RegisterRequest): void {
    this.authService.register(registerRequest).subscribe({
      next: (response: AuthSuccess) => {
        this.router.navigate(['/posts']);
      },
      error: (errorResponse:ValidationErrorResponse) => {
        // console.log('Validation Error response:', errorResponse);
        // L'intercepteur a déjà traité les erreurs et mis à jour les Signals
        // par conséquent le signal local backendFieldErrors devient inutile
        //this.backendFieldErrors.set(this.errorHandlingService.getFieldErrors());
        this.isEditMode.set(true);
      }
    });
  }
}
