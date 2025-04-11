import {Component, OnInit} from '@angular/core';
import {UserFormComponent} from '../../components/user-form/user-form.component';
import {Router} from '@angular/router';
import {AuthService} from '../../services/auth.service';
import {RegisterRequest} from '../../interfaces/registerRequest.interface';
import {AuthSuccess} from '../../interfaces/authSuccess.interface';

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
  public onError = false;

  constructor(private router: Router, private authService: AuthService) {
    //console.log("RegisterComponent constructor")
  }

  ngOnInit(): void {this.headTitle = "Inscription";  }

  /**
   * Méthode pour gérer les données du formulaire envoyées depuis UserFormComponent
   * @param registerRequest
   */
  handleFormSubmit(registerRequest: RegisterRequest): void {
    //TODO Nettoyage
    //console.log('CPI RegisterComponent handleFormSubmit soumis avec les données :', registerRequest);

    // this.authService.register(registerRequest).subscribe({
    //   next: () => this.router.navigate(['/auth/login']),
    //   error: () => this.onError = true,
    // });

    // this.authService.register(registerRequest).subscribe(
    //   (response: AuthSuccess) => {
    //     localStorage.setItem('token', response.token);
    //      this.authService.me().subscribe((user: User) => {
    //        this.sessionService.logIn(user);
    //     this.router.navigate(['/']);
    //     });
    //   },
    //   error => this.onError = true
    // );

    // this.authService.register(registerRequest).subscribe(
    //   (response: AuthSuccess) => {
    //     localStorage.setItem('token', response.token);
    //     // this.authService.me().subscribe((user: User) => {
    //     //   this.sessionService.logIn(user);
    //       this.router.navigate(['/']);
    //     // });
    //   },
    //   error => this.onError = true
    // );

    //MENTOR2: syntaxe projet 3 frontend dépréciée en v19
    this.authService.register(registerRequest).subscribe({
      next: (response: AuthSuccess) => {
        this.router.navigate(['/post/list']);
      },
      error: () => {
        this.onError = true;
      }
    });

  }

}
