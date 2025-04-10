import {Component, EventEmitter, Input, OnInit, Output, ViewEncapsulation} from '@angular/core';
import {BackComponent} from '../../../../shared/components/back/back.component';
import {AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatError, MatFormField, MatInput, MatLabel} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {NgIf} from '@angular/common';
import {RegisterRequest} from '../../interfaces/registerRequest.interface';

@Component({
  selector: 'app-user-form',
  //encapsulation: ViewEncapsulation.None,//pour pouvoir agir sur bug affichage de .mat-mdc-form-field-error-wrapper
  imports: [
    BackComponent,
    ReactiveFormsModule,
    MatFormField,
    MatInput,
    MatButtonModule,
    MatError,
    MatLabel,
    NgIf
  ],
  templateUrl: './user-form.component.html',
  styleUrl: './user-form.component.scss'
})
export class UserFormComponent implements OnInit {
  @Input() headTitle: string | undefined;
  @Input() labelSubmit: string | undefined;
  form!: FormGroup;
  @Output() submit: EventEmitter<RegisterRequest> = new EventEmitter<RegisterRequest>();

  constructor(private fb: FormBuilder) {
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      username: ['', [Validators.required]],
      password: ['', [Validators.required, this.passwordValidator]],
    });

  }

  onSubmit($event: Event) {
    $event.preventDefault(); // Empêche la soumission HTML par défaut
    $event.stopPropagation(); //sinon double soumission intempestive (avec event pour 2eme)
    // Émettre l'événement vers composant parent
    this.submit.emit(this.form.value); // Inclure les données du formulaire
  }

  /**
   * L'affichage des erreurs est gérée dans le template grâce au retour de cette méthode
   */
  private passwordValidator(control: AbstractControl): { [key: string]: boolean } | null {
    const password = control.value;

    if (!password) {
      return null; // Pas d'erreur si le champ est vide, (géré par Validators.required)
    }

    const errors: { [key: string]: boolean } = {};

    if (password.length <= 8) {
      errors['minLength'] = true;
    }

    if (!/[A-Z]/.test(password)) {
      errors['uppercase'] = true;
    }

    if (!/[a-z]/.test(password)) {
      errors['lowercase'] = true;
    }

    if (!/[0-9]/.test(password)) {
      errors['number'] = true;
    }

    if (!/[!@#$%^&*(),.?":{}|<>]/.test(password)) {
      errors['specialCharacter'] = true;
    }

    // Retourner null si aucune erreur, sinon retourne l'objet contenant les erreurs détectées
    return Object.keys(errors).length ? errors : null;
  }

}
