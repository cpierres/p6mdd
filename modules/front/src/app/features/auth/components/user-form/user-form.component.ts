import {Component, EventEmitter, Input, OnDestroy, OnInit, Output, ViewEncapsulation} from '@angular/core';
import {BackComponent} from '../../../../shared/components/back/back.component';
import {AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatError, MatFormField, MatInput, MatLabel} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {NgIf} from '@angular/common';
import {RegisterRequest} from '../../interfaces/registerRequest.interface';
import {BehaviorSubject, Subscription} from 'rxjs';
import {User} from '../../../user/interfaces/user.interface';

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
export class UserFormComponent implements OnInit, OnDestroy {
  @Input() headTitle: string | undefined;
  @Input() labelSubmit: string | undefined;
  form!: FormGroup;
  @Output() submit: EventEmitter<RegisterRequest> = new EventEmitter<RegisterRequest>();

  @Input() initialEditMode: boolean = true; // définir le mode initial (false = view, true = edit)
  // Propriété pour gérer le mode (par défaut : view)
  isEditMode: boolean = true;

  @Input() currentUser$: BehaviorSubject<User | null> | null = null;
  private subscription!: Subscription; // Penser à nettoyer l'abonnement

  constructor(private fb: FormBuilder) {
  }

  ngOnInit(): void {
    // Initialisation de isEditMode avec choix @Input
    this.isEditMode = this.initialEditMode;

    this.form = this.fb.group({
      email: [{value: '', disabled: !this.isEditMode}, [Validators.required, Validators.email]],
      username: [{value: '', disabled: !this.isEditMode}, [Validators.required]],
      password: [{value: '', disabled: !this.isEditMode}, [Validators.required, this.passwordValidator]],
    });

    // Souscription à l'Observable optionnel 'currentUser$'
    if (this.currentUser$) {
      this.subscription = this.currentUser$.subscribe((user) => {
        if (user) {
          this.form.patchValue({
            email: user.email,
            username: user.username,
            password: '', // Ne pas pré-remplir un mot de passe
          });
        }
      });
    }
  }

  ngOnDestroy(): void {
    // Nettoyez l'abonnement pour éviter des fuites mémoire
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  onSubmit($event: Event) {
    $event.preventDefault(); // Empêche la soumission HTML par défaut
    $event.stopPropagation(); //sinon double soumission intempestive (avec event pour 2eme)

    if (this.isEditMode) {
      // Mode édition : On envoie les données au composant parent quand on enregistre
      this.submit.emit(this.form.value);
    } else {
      // Mode vue : On passe en mode édition
      this.toggleEditMode();
    }
  }

  toggleEditMode() {
    this.isEditMode = !this.isEditMode;

    // Activer ou désactiver les champs en fonction du mode
    Object.keys(this.form.controls).forEach((key) => {
      const control = this.form.get(key)!;
      this.isEditMode ? control.enable() : control.disable();
    });

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
