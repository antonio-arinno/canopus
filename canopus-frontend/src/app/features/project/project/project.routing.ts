import { Routes } from "@angular/router"
import { ProjectComponent } from "./list/project.component"
import { ProjectDetailComponent } from "./detail/project-detail.component"

export const PROJECT_ROUTES: Routes = [
    { path: '', component: ProjectComponent},
    { path: 'detail/:id', component: ProjectDetailComponent},
    { path: 'detail', component: ProjectDetailComponent }    
]