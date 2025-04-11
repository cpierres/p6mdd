import {Routes} from '@angular/router';
import {HomeComponent} from './home/home.component';
import {RegisterComponent} from './features/auth/pages/register/register.component';
import {PostListComponent} from './features/post/pages/post-list/post-list.component';
import {ProfilComponent} from './features/auth/pages/profil/profil.component';

export const routes: Routes = [
  {path: '', redirectTo: 'home', pathMatch: 'full'}, // Route par défaut vers homes
  {path: 'home', component: HomeComponent},
  {path: 'auth/register', component: RegisterComponent},
  {path: 'post/list', component: PostListComponent},
  {path: 'auth/profil', component: ProfilComponent},
  {path: '**', redirectTo: ''} // Redirection par défaut en cas d'URL non valide

];
