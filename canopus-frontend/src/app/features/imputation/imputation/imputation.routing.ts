import { Routes } from "@angular/router"
import { ImputationComponent } from "./list/imputation.component"
import { ImputationDetailComponent } from "./detail/imputation-detail.component"


export const IMPUTATION_ROUTES: Routes = [
    { path: '', component: ImputationComponent},
    { path: 'detail/:id', component: ImputationDetailComponent},
    { path: 'detail', component: ImputationDetailComponent }    
]