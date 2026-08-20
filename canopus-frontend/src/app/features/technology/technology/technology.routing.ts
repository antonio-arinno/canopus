import { Routes } from "@angular/router"
import { TechnologyComponent } from "./list/technology.component"
import { TechnologyDetailComponent } from "./detail/technology-detail.component"


export const TECHNOLOGY_ROUTES: Routes = [
    { path: '', component: TechnologyComponent},
    { path: 'detail/:id', component: TechnologyDetailComponent},
    { path: 'detail', component: TechnologyDetailComponent }    
]