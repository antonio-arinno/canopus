import { ApplicationConfig, LOCALE_ID, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { tokenInterceptor } from '@core/auth/interceptors/token.interceptor';

// 1. Importaciones para el idioma (Español)
import { registerLocaleData } from '@angular/common';
import localeEs from '@angular/common/locales/es';

// 2. Registramos los datos de cultura de España
registerLocaleData(localeEs);

export const appConfig: ApplicationConfig = {
  providers: [provideZoneChangeDetection({ eventCoalescing: true }), 
              provideRouter(routes),
              provideHttpClient(withInterceptors([tokenInterceptor])),
              provideAnimationsAsync(),
              { provide: LOCALE_ID, useValue: 'es-ES' }]
};
