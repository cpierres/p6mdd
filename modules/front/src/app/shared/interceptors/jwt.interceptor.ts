import { HttpHandler, HttpInterceptorFn } from '@angular/common/http';

// Définit l'intercepteur comme une fonction compatible avec Angular 19
export const jwtInterceptor: HttpInterceptorFn = (request, next) => {
  const token = localStorage.getItem('token');
  if (token) {
    request = request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
  }
  return next(request);
};
