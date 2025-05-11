import { HttpHandler, HttpInterceptorFn } from '@angular/common/http';

// Définit l'intercepteur comme une fonction compatible avec Angular 19
export const jwtInterceptor: HttpInterceptorFn = (request, next) => {
  const token = localStorage.getItem('token');

  // Exclure certaines routes de l'application de l'intercepteur
  //const publicRoutes = ['/auth/register','/auth/login'];
  const publicRoutes = ['/api/auth/register', '/api/auth/login'];

  // Vérifier si la requête correspond à l'une des routes publiques
  if (publicRoutes.some(route => request.url.includes(route))) {
    return next(request); // Skip l'intercepteur
  }

  if (token) {
    request = request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
  }
  return next(request);
};
