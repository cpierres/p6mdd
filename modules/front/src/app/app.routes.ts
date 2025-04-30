import {Routes} from '@angular/router';
import {HomeComponent} from './home/home.component';
import {RegisterComponent} from './features/auth/pages/register/register.component';
import {ProfilComponent} from './features/auth/pages/profil/profil.component';
import {LoginPageComponent} from './features/auth/pages/login/login.component';
import {TopicListAllComponent} from './features/topic/pages/topic-list-all/topic-list-all.component';
import {PostCreateComponent} from './features/post/pages/post-create/post-create.component';
import {PostListPageComponent} from './features/post/pages/post-list-page/post-list-page.component';
import {PostCommentComponent} from './features/post/pages/post-comment/post-comment.component';
import {authGuard} from './shared/guards/auth.guard';
import {unauthGuard} from './shared/guards/unauth.guard';

export const routes: Routes = [
  {path: '', redirectTo: 'home', pathMatch: 'full'}, // Route par défaut vers home
  {path: 'home', component: HomeComponent},
  {path: 'auth/login', component: LoginPageComponent, canActivate: [unauthGuard] },
  {path: 'auth/register', component: RegisterComponent, canActivate: [unauthGuard]},
  {path: 'auth/profil', component: ProfilComponent, canActivate: [authGuard]},
  {path: 'posts/new', component: PostCreateComponent, canActivate: [authGuard]},
  {path: 'posts', component: PostListPageComponent, canActivate: [authGuard]},
  {path: 'topics', component: TopicListAllComponent, canActivate: [authGuard]},
  {path: 'posts/:id', component: PostCommentComponent, canActivate: [authGuard]},
  {path: '**', redirectTo: 'home'} // Redirection par défaut en cas d'URL non valide
];
