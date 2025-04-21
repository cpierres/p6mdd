import {Routes} from '@angular/router';
import {HomeComponent} from './home/home.component';
import {RegisterComponent} from './features/auth/pages/register/register.component';
import {ProfilComponent} from './features/auth/pages/profil/profil.component';
import {LoginPageComponent} from './features/auth/pages/login/login.component';
import {TopicListAllComponent} from './features/topic/pages/topic-list-all/topic-list-all.component';
import {PostCreateComponent} from './features/post/pages/post-create/post-create.component';
import {PostListPageComponent} from './features/post/pages/post-list-page/post-list-page.component';

export const routes: Routes = [
  {path: '', redirectTo: 'home', pathMatch: 'full'}, // Route par défaut vers homes
  {path: 'home', component: HomeComponent},
  {path: 'auth/login', component: LoginPageComponent},
  {path: 'auth/register', component: RegisterComponent},
  {path: 'auth/profil', component: ProfilComponent},
  {path: 'post/create', component: PostCreateComponent},
  {path: 'post/list', component: PostListPageComponent},
  {path: 'topics', component: TopicListAllComponent},
  {path: '**', redirectTo: ''} // Redirection par défaut en cas d'URL non valide

];
