import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../auth.service';
import { RequestStateService } from '../../ui/request-state.service'; 

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const requestState = inject(RequestStateService); 

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      
      // 1. Si el fallo ocurre en la pantalla de login/auth, NO ejecutamos la lógica global automática
      if (router.url.includes('/login') || router.url.includes('/auth') || req.url.includes('/login')) {
        return throwError(() => error);
      }

      // 2. Enviamos el error a tu servicio para que extraiga el mensaje de Spring Boot en pantallas internas
      requestState.setError(error);

      // 3. Control de flujos críticos globales en el área privada
      switch (error.status) {
        case 401: // Unauthorized (Token JWT expirado o inválido estando dentro de la app)
          console.warn('Sesión inválida o expirada. Redirigiendo al login...');
          authService.logout(); 
          // MEJORA: Apunta a la ruta raíz real de tu estructura auth.routing.ts (normalmente '/auth/login' o vacío)
          router.navigate(['/auth/login']); 
          break;
          
        case 403: // Forbidden
          console.error('No tienes permisos para realizar esta acción.');
          break;

        default:
          break;
      }

      return throwError(() => error);
    })
  );
};

