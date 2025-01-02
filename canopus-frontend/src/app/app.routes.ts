import { Routes } from '@angular/router';
import { authGuard } from '@core/guards/auth.guard';


export const routes: Routes = [
    {
        path: '',
        loadChildren: () => import('./shared/layouts/public/public.routing').then(m => m.PUBLIC_ROUTES)
    },
    {   
        path: 'pvt',
        canActivate: [authGuard],
        loadChildren: () => import('./shared/layouts/private/private.routing').then(m => m.PRIVATE_ROUTES)

    }
];
