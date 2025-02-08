import { Routes } from "@angular/router"
import { TechnologyComponent } from "./technology/technology.component"
import { TechnologyDetailComponent } from "./technology-detail/technology-detail.component"


export const TECHNOLOGY_ROUTES: Routes = [
    { path: '', component: TechnologyComponent},
    { path: 'detail/:id', component: TechnologyDetailComponent},
    { path: 'detail', component: TechnologyDetailComponent }    
]