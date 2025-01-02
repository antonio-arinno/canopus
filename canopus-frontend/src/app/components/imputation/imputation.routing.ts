import { Routes } from "@angular/router"
import { ImputationComponent } from "./imputation/imputation.component"
import { ImputationDetailComponent } from "./imputation-detail/imputation-detail.component"


export const IMPUTATION_ROUTES: Routes = [
    { path: '', component: ImputationComponent},
    { path: 'detail/:id', component: ImputationDetailComponent},
    { path: 'detail', component: ImputationDetailComponent }    
]