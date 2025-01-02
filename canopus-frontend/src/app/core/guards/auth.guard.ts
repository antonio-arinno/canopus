import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '@core/services/auth.service';

export const authGuard: CanActivateFn = (route, state) => {

  const authService = inject(AuthService);
  const routerService = inject(Router);

  if(authService.isAuthenticated()){
    if(authService.isTokenExpired()){
        authService.logout();
        routerService.navigate(['/auth/login']);
        return false;
    }
    return true;
  }
  routerService.navigate(['/auth/login']);
  return false;  
};
