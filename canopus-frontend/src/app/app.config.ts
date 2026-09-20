import { ApplicationConfig, LOCALE_ID, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { tokenInterceptor } from '@core/auth/interceptors/token.interceptor';
// 1. Importamos el nuevo interceptor de errores
import { errorInterceptor } from '@core/auth/interceptors/error.interceptor'; 

import { registerLocaleData } from '@angular/common';
import localeEs from '@angular/common/locales/es';

registerLocaleData(localeEs);

export const appConfig: ApplicationConfig = {
  providers: [provideZoneChangeDetection({ eventCoalescing: true }), 
              provideRouter(routes),
              provideHttpClient(withInterceptors([tokenInterceptor])),
              // 2. Añadimos el errorInterceptor a la cadena de interceptores HTTP
              provideHttpClient(withInterceptors([tokenInterceptor, errorInterceptor])),             
              provideAnimationsAsync(),
              { provide: LOCALE_ID, useValue: 'es-ES' }]
};
