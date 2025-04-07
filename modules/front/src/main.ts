import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';
import { routes } from './app/app.routes'; // Importez vos routes ici

bootstrapApplication(AppComponent, {
  providers: [
    provideAnimations(), // Important pour les animations Material
    provideRouter(routes)
  ]
}).catch(err => console.error(err));
