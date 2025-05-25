import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '../../services/auth.service';
import {LoginRequest} from '../../interfaces/loginRequest.interface';
import {MatInputModule} from '@angular/material/input';
import {CommonModule} from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {BackComponent} from '../../../../shared/components/back/back.component';
import {Router} from '@angular/router';
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
              private messagesService: MessagesService) {
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      identifier: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  // onSubmit(): void {
  //   if (!this.form.valid) {
  //     this.messagesService.showMessage('Formulaire invalide', 'error');
  //     return;
  //   }
  //
  //   const loginRequest: LoginRequest = this.form.value;
  //
  //   this.messagesService.clear();
  //
  //   // Bug (non systématique) rencontré lors de mise en place du guard d'authentification :
  //   // De temps en temps, lors du login on restait sur le login alors que le menu s'affichait bien.
  //   // Explication :
  //   // - AuthService fait une requête de login et obtient un token
  //   // - appelle ensuite de façon asynchrone la méthode me() pour obtenir les informations utilisateur
  //   // - Pendant ce temps, le composant de login tentait immédiatement de naviguer vers /posts
  //   // - authGuard vérifie sessionService.isLogged, mais cette valeur est encore false car l'appel asynchrone
  //   // à me() n'était pas encore terminé
  //   // donc l'objectif est d'attendre que le statut soit bien à jour avant de naviguer vers /posts
  //   this.authService.login(loginRequest).pipe(
  //     switchMap(() => this.sessionService.$isLogged()), // Attendre le changement d'état de connexion
  //     take(1) // Prendre une seule valeur et désabonner automatiquement
  //   ).subscribe({
  //     next: (isLogged: boolean) => {
  //       if (isLogged) {
  //         // Navigation sécurisée après confirmation de l'état connecté
  //         this.router.navigate(['/posts']);
  //       }
  //     },
  //     error: (error) => {
  //       // Gestion globale des erreurs
  //       this.messagesService.showMessage('Erreur lors de la tentative de connexion.', 'error');
  //       console.error('Erreur onSubmit:', error);
  //     }
  //   });
  // }

  onSubmit(): void {
    if (!this.form.valid) {
      this.messagesService.showMessage('Formulaire invalide', 'error');
      return;
    }

    const loginRequest: LoginRequest = this.form.value;
    this.messagesService.clear();

    this.authService.login(loginRequest).subscribe({
      next: () => {
        // Navigation après login réussi
        this.router.navigate(['/posts']);
      },
      error: (error) => {
        // Gestion globale des erreurs
        console.error('Erreur onSubmit:', error);
      }
    });
  }


}
