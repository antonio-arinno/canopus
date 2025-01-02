import { Routes } from "@angular/router";
import { PrivateComponent } from "./private.component";

export const PRIVATE_ROUTES: Routes = [
    { path: '', component: PrivateComponent, children: [
        {
            path: 'imputation',
            loadChildren: () => import('@components/imputation/imputation.routing').then(m => m.IMPUTATION_ROUTES)
        },
        {
            path: 'project',
            loadChildren: () => import('@components/project/project.routing').then(m => m.PROJECT_ROUTES)
        },
        {
            path: 'product',
            loadChildren: () => import('@components/product/product.routing').then(m => m.PRODUCT_ROUTES)
        },
        {
            path: 'user',
            loadChildren: () => import('@components/user/user.routing').then(m => m.USER_ROUTES)
        }
    ]}
];