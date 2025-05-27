import {bootstrapApplication} from '@angular/platform-browser';
import {AppComponent} from './app/app.component';
import {provideAnimations} from '@angular/platform-browser/animations';
import {provideRouter} from '@angular/router';
import {routes} from './app/app.routes';
import {provideHttpClient, withInterceptors} from '@angular/common/http';
import {jwtInterceptor} from './app/shared/interceptors/jwt.interceptor';
import {credentialsInterceptor} from './app/shared/interceptors/credentials.interceptor';
import {errorInterceptor} from './app/shared/interceptors/error.interceptor';

bootstrapApplication(AppComponent, {
  providers: [
    provideAnimations(), // Important pour les animations Material
    provideRouter(routes),
    provideHttpClient(
      withInterceptors([
        jwtInterceptor,
        errorInterceptor,
        credentialsInterceptor
      ]),
    )
  ]
}).catch(err => console.error(err));
