import { Routes } from '@angular/router';
import { PrivateComponent } from './shared/layouts/private/private.component';

export const routes: Routes = [
    {
        path: '',
        loadChildren: () => import('./shared/layouts/public/public.routing').then(m => m.PUBLIC_ROUTES)
    },
    { path: 'pvt', component: PrivateComponent}
];
