import { Routes } from '@angular/router';
import {HomeComponent} from './home/home.component';
import {RegisterComponent} from './features/auth/pages/register/register.component';

export const routes: Routes = [
  { path: '', component: HomeComponent, pathMatch: 'full' },
  { path: 'auth/register', component: RegisterComponent },
  { path: '**', redirectTo: '' } // Redirection par défaut en cas d'URL non valide

];
