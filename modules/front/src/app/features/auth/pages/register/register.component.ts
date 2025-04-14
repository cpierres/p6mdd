import {Component, OnInit} from '@angular/core';
import {UserFormComponent} from '../../components/user-form/user-form.component';
import {Router} from '@angular/router';
import {AuthService} from '../../services/auth.service';
import {RegisterRequest} from '../../interfaces/registerRequest.interface';
import {AuthSuccess} from '../../interfaces/authSuccess.interface';
import {ErrorHandlingService} from '../../../../shared/services/error-handling-service.service';

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
  onError = false;

  backendFieldErrors: { [key: string]: string } = {};

  constructor(private router: Router,
              private authService: AuthService,
              private errorHandlingService: ErrorHandlingService) {
  }

  ngOnInit(): void {
    this.headTitle = "Inscription";
  }

  /**
   * Méthode pour gérer les données du formulaire envoyées depuis UserFormComponent
   * @param registerRequest
   */
  handleFormSubmit(registerRequest: RegisterRequest): void {
    this.authService.register(registerRequest).subscribe({
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
