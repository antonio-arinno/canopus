import { Routes } from "@angular/router"
import { BackupComponent } from "./backup/backup.component"
import { ProductDetailComponent } from "./product-detail/product-detail.component"

export const BACKUP_ROUTES: Routes = [
    { path: '', component: BackupComponent},
    { path: 'detail/:id', component: ProductDetailComponent},
    { path: 'detail', component: ProductDetailComponent }    
]