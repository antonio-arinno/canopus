import { Routes } from "@angular/router";
import { PrivateComponent } from "./private.component";

export const PRIVATE_ROUTES: Routes = [
    { path: '', component: PrivateComponent, children: [
        {
            path: 'imputation',
            loadChildren: () => import('@features/imputation/imputation/imputation.routing').then(m => m.IMPUTATION_ROUTES)
        },
        {
            path: 'project',
            loadChildren: () => import('@features/project/project/project.routing').then(m => m.PROJECT_ROUTES)
        },
        {
            path: 'product',
            loadChildren: () => import('@features/product/product/product.routing').then(m => m.PRODUCT_ROUTES)
        },     
        {
            path: 'technology',
            loadChildren: () => import('@features/technology/technology/technology.routing').then(m => m.TECHNOLOGY_ROUTES)
        },           
        {
            path: 'user',
            loadChildren: () => import('@features/user/user/user.routing').then(m => m.USER_ROUTES)
        }
    ]}
];