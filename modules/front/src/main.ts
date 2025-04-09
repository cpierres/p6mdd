import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';
import { routes } from './app/app.routes';
import {importProvidersFrom} from '@angular/core';
import {HttpClientModule} from '@angular/common/http';

bootstrapApplication(AppComponent, {
  providers: [
    provideAnimations(), // Important pour les animations Material
    provideRouter(routes),
    importProvidersFrom(HttpClientModule)
  ]
}).catch(err => console.error(err));
