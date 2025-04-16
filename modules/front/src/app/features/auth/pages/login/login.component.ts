import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '../../services/auth.service';
import {LoginRequest} from '../../interfaces/loginRequest.interface';
import {MatInputModule} from '@angular/material/input';
import {CommonModule} from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {BackComponent} from '../../../../shared/components/back/back.component';
import {Router} from '@angular/router';
import {AuthSuccess} from '../../interfaces/authSuccess.interface';
import {MessagesService} from '../../../../shared/services/messages.service';

@Component({
  selector: 'app-login',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    BackComponent
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginPageComponent implements OnInit {
  form!: FormGroup;

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router,
              private messagesService: MessagesService) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      identifier: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.form.valid) {
      const loginRequest: LoginRequest = this.form.value;
      this.messagesService.clear();
      this.authService.login(loginRequest).subscribe({
        next: (response: AuthSuccess) => {
          // Redirige l'utilisateur vers une page sécurisée après connexion
          this.router.navigate(['/post/list']);
        },
        error: () => {
          // Gestion des erreurs déjà effectuée dans le service
        }
      });
    }
  }
}
