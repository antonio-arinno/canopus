import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '@core/auth/auth.service';

export const tokenInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.token;

  // 1. Lista de endpoints públicos que NO requieren inyección de Token JWT
  // Según tu documento de arquitectura, el registro de empresas y el login entran aquí
  const publicUrls = ['/register', '/login'];
  const isPublicUrl = publicUrls.some(url => req.url.includes(url));

  // 2. Comprobación estricta: si la URL es pública, dejamos pasar la petición limpia de inmediato
  if (isPublicUrl) {
    return next(req);
  }

  // 3. Verificación de seguridad robusta del token (evitamos strings vacíos, null o undefined)
  if (token && token.trim() !== '') {
    const authReq = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`)
    });
    return next(authReq);
  }

  // Si no hay token y no es ruta pública, dejamos pasar (ej: redirecciones internas o assets locales)
  return next(req);
};

