import {Component, OnInit, signal} from '@angular/core';
import {UserFormComponent} from '../../components/user-form/user-form.component';
import {User} from '../../../user/interfaces/user.interface';
import {AuthService} from '../../services/auth.service';
import {AuthSuccess} from '../../interfaces/authSuccess.interface';
import {Router} from '@angular/router';
import {UserUpdate} from '../../../user/interfaces/user-update.interface';
import {ErrorHandlingService} from '../../../../shared/services/error-handling-service.service';
import {SessionService} from '../../../../shared/services/session-service.service';
import {TopicSubscribedStatus} from '../../../topic/interfaces/TopicSubscribedStatus';
import {TopicService} from '../../../topic/services/topic.service';
import {TopicListComponent} from '../../../topic/components/topic-list/topic-list.component';

@Component({
  selector: 'app-profil',
  imports: [
    UserFormComponent,
    TopicListComponent,
  ],
  templateUrl: './profil.component.html',
  styleUrl: './profil.component.scss'
})
export class ProfilComponent {
  labelSubmit: string = "Sauvegarder";
  headTitle: string = "Profil utilisateur";
  // Signals pour le mode édition et les données utilisateur
  isEditMode = signal<boolean>(false);
  currentUser = signal<User | null>(null);
  context: string = "profil";

  public onError = false;
  backendFieldErrors = signal<{ [key: string]: string }>({});
  topics: TopicSubscribedStatus[] = [];

  constructor(private authService: AuthService, private router: Router,
              private errorHandlingService: ErrorHandlingService,
              private sessionService: SessionService,
              private topicService: TopicService) {
    // Charger les données utilisateur pour alimenter la page
    // (pour signal ne pas faire dans ngOnInit mais dans constructor)
    this.authService.me().subscribe((user: User) => {
      this.currentUser.set(user);
    });

    // Souscrire à l'Observable des Topics pour avoir les mises à jour
    this.topicService.topicsWithSubscriptionStatus$.subscribe(topics => {
      this.topics = topics;
    });

    // Chargement initial des des topics
    this.loadTopics();
  }

  loadTopics(): void {
    this.topicService.getTopicsWithSubscriptionStatus().subscribe();
  }

  handleFormSubmit(userUpdate: UserUpdate): void {
    // Comparer l'email dans userUpdate avec celui de currentUser$
    const currentEmail = this.currentUser()?.email;
    const isEmailModified = currentEmail !== userUpdate.email;

    this.authService.updateMe(userUpdate).subscribe({
      next: (response: AuthSuccess) => {
        // Si l'email a changé, rediriger vers la route de déconnexion
        if (isEmailModified) {
          this.sessionService.logOut();
          this.router.navigate(['/']);
        } else {
          // Sinon, revenir en mode lecture après la sauvegarde
          this.isEditMode.set(false);
        }
      },
      error: (error) => {
        this.onError = true;
        this.errorHandlingService.handleValidationErrors(error);
        this.backendFieldErrors.set(this.errorHandlingService.getFieldErrors());
      }
    });
  }

  // Méthode appelée quand le composant enfant veut changer le mode
  handleEditModeChange(isEdit: boolean): void {
    this.isEditMode.set(isEdit);
  }

}
