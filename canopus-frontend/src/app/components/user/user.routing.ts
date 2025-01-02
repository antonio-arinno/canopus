import { Routes } from "@angular/router"
import { UserComponent } from "./user/user.component"
import { UserDetailComponent } from "./user-detail/user-detail.component"

export const USER_ROUTES: Routes = [
    { path: '', component: UserComponent},
    { path: 'detail/:id', component: UserDetailComponent},
    { path: 'detail', component: UserDetailComponent }    
]