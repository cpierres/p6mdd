import {
  Component, computed, effect,
  EventEmitter,
  Input, OnDestroy,
  OnInit,
  Output, signal,
  Signal
} from '@angular/core';
import {BackComponent} from '../../../../shared/components/back/back.component';
import {AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatError, MatInput, MatLabel} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {NgClass, NgIf} from '@angular/common';
import {User} from '../../../user/interfaces/user.interface';
import {MatFormFieldModule} from '@angular/material/form-field';
import {Subject, takeUntil} from 'rxjs';
import {FieldErrors} from '../../../../shared/models/FieldErrors';
import {
  FieldErrorBackendComponent
} from '../../../../shared/components/field-error-backend/field-error-backend.component';
import {FieldErrorDetail} from '../../../../shared/interfaces/FieldErrorDetail';
import {ErrorHandlingService} from '../../../../shared/services/error-handling-service.service';

@Component({
  selector: 'app-user-form',
  //encapsulation: ViewEncapsulation.None,//pour pouvoir agir sur bug affichage de .mat-mdc-form-field-error-wrapper
  imports: [
    BackComponent,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInput,
    MatButtonModule,
    MatError,
    MatLabel,
    NgIf,
    FieldErrorBackendComponent,
    NgClass
  ],
  templateUrl: './user-form.component.html',
  styleUrl: './user-form.component.scss'
})
export class UserFormComponent<T = any> implements OnInit, OnDestroy {
  @Input() headTitle: string | undefined;
  @Input() labelSubmit: string | undefined;
  form!: FormGroup;
  //pour SOLID (srp) rendre le type retourné générique
  @Output() submit: EventEmitter<T> = new EventEmitter<T>();

  // @Input() initialEditMode: boolean = true; // définir le mode initial (false = view, true = edit)
  // // Propriété pour gérer le mode (par défaut : view)
  // isEditMode: boolean = true;
  //
  // @Input() currentUser$: BehaviorSubject<User | null> | null = null;
  // // Ajoutez une propriété pour stocker l'utilisateur actuel dans le composant
  // currentUser: User | null = null;
  //
  // private subscription!: Subscription; // Penser à nettoyer l'abonnement
  @Input({required: true}) isEditMode!: Signal<boolean>;
  @Input({required: true}) currentUser!: Signal<User | null>;

  readonly userEmail = computed(() => this.currentUser()?.email || "");

  @Output() editModeChange = new EventEmitter<boolean>(); // Communique au parent lorsqu'on change le mode.

  /** Signal local mutable pour gérer le mode en cas de modif interne */
  private editMode = signal<boolean>(false);

  /** Signal calculé : combine le mode initial global (parent) et local si bouton "Modifier" */
  readonly isEditModeValue = computed(() => this.editMode() || this.isEditMode());

  @Input() backendFieldErrors!: Signal<FieldErrors>;// erreurs provenant du backend

  @Input() context: 'register' | 'profil' = 'register';

  readonly emailValue = signal<string>(''); // Signal pour le champ email
  private destroy$ = new Subject<void>();

  constructor(private fb: FormBuilder, private errorHandlingService: ErrorHandlingService) {

    // Initialisation du formulaire avec tous les champs activés par défaut
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      username: ['', [Validators.required, Validators.minLength(2)]],
      password: ['', [Validators.required, Validators.minLength(8), this.passwordValidator]],
    });

    effect(() => {
      //console.log("effect UserFormComponent.backendFieldErrors() triggered... ", this.backendFieldErrors(), "context:", this.context,)
      // Mise à jour du formulaire avec les données de l'utilisateur (via Signal)
      const user = this.currentUser();
      if (user) {
        this.form.patchValue({
          email: user.email,
          username: user.username,
          password: '' // Pas de pré-remplissage pour des raisons de sécurité
        });
      }

      const errors = this.backendFieldErrors();
      if (errors && Object.keys(errors).length > 0) {
        // S'il y a des erreurs backend, on les applique et on garde le formulaire activé
        Object.keys(errors).forEach((field) => {
          const control = this.form.get(field);
          if (control) {
            const errorDetail = errors.find(e => e.field === field)?.message;
            if (errorDetail) {
              control.setErrors({backend: errorDetail});
            }
            // Marquer le contrôle comme touché
            control.markAsTouched();
            // console.log(`État du contrôle ${field}:`, {
            //   valid: this.form.get(field)?.valid,
            //   invalid: this.form.get(field)?.invalid,
            //   dirty: this.form.get(field)?.dirty,
            //   touched: this.form.get(field)?.touched,
            //   disabled: this.form.get(field)?.disabled
            // });
            // console.log(`Erreurs control pour ${field} : `, control.errors);
          }
        });

      } else {
        // S'il n'y a pas d'erreurs backend, on applique la logique normale d'activation/désactivation
        if (this.isEditModeValue()) {
          this.form.enable();
        } else {
          this.form.disable();
        }
      }

    });

  }

  ngOnInit(): void {
    //Écoute des changements dans le champ email et mise à jour du Signal
    this.form.get('email')?.valueChanges
      .pipe(takeUntil(this.destroy$)) // Arrête l'observable au moment du `destroy`
      .subscribe((email) => {
        if (email !== undefined) {
          this.emailValue.set(email); // Mise à jour du Signal
        }
      });

    //configure les champs pour qu'en cas de changement l'erreur backend s'efface automatiquement
    this.setupFieldErrorClearingOnChange();

  }

  onSubmit($event: Event) {
    $event.preventDefault(); // Empêche la soumission HTML par défaut
    $event.stopPropagation(); //sinon double soumission intempestive (avec event pour 2eme)

    if (this.isEditModeValue()) {
      // Mode édition : On envoie les données au composant parent sans quitter le mode édition
      // this.exitEditMode();
      this.submit.emit(this.form.value as T);// Emettre données typées dynamiquement
      this.editMode.set(false);
    } else {
      // Bascule le mode local en édition.
      this.editMode.set(true); // Active le mode édition en interne.
      this.editModeChange.emit(true); // Informe le parent (si nécessaire).
    }
  }

  /** Méthode appelée quand la sauvegarde réussit */
  exitEditMode(): void {
    this.editMode.set(false); // Revenir en lecture seule (consultation).
    this.editModeChange.emit(false); // Informer le parent pour synchronisation.
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

  /**
   * Méthode pour vérifier si l'email a été modifié dans le contexte de la mise à jour du profil
   */
  readonly isEmailModifiedForProfil = computed(() => {
    return this.context === 'profil' && this.currentUser()
      ? this.emailValue() !== this.currentUser()?.email // Compare "emailValue" (Signal) avec l'email utilisateur
      : false;
  });

  readonly submitButtonLabel = computed(() => {
    // console.log('submitButtonLabel Recalculation triggered...','context:', this.context,
    //   'isEditModeValue:', this.isEditModeValue(),
    //   'isEmailModifiedForProfil:', this.isEmailModifiedForProfil());

    if (this.context === 'profil' && this.isEditModeValue() && this.isEmailModifiedForProfil()) {
      return 'Enregistrer et déconnecter'; // Libellé spécifique si l'email a été modifié pour Profil
    }
    return this.isEditModeValue() ? (this.labelSubmit || 'Enregistrer') : 'Modifier';
  });

  // méthodes pour récupérer les erreurs de champ
  getFieldError(fieldName: string): FieldErrorDetail | undefined {
    return this.backendFieldErrors().find(error => error.field === fieldName);
  }

  hasFieldError(fieldName: string): boolean {
    return !!this.getFieldError(fieldName);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private setupFieldErrorClearingOnChange(): void {
    // Pour chaque contrôle dans le formulaire
    Object.keys(this.form.controls).forEach(controlName => {
      this.form.get(controlName)?.valueChanges
        .pipe(takeUntil(this.destroy$))
        .subscribe(() => {
          if (this.hasFieldError(controlName)) {
            this.errorHandlingService.clearFieldError(controlName);
          }
        });
    });
  }

}
