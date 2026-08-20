import { Routes } from "@angular/router"
import { UserComponent } from "./list/user.component"
import { UserDetailComponent } from "./detail/user-detail.component"

export const USER_ROUTES: Routes = [
    { path: '', component: UserComponent},
    { path: 'detail/:id', component: UserDetailComponent},
    { path: 'detail', component: UserDetailComponent }    
]