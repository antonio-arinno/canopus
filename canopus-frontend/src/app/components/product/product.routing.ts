import { Routes } from "@angular/router"
import { ProductComponent } from "./product/product.component"
import { ProductDetailComponent } from "./product-detail/product-detail.component"

export const PRODUCT_ROUTES: Routes = [
    { path: '', component: ProductComponent},
    { path: 'detail/:id', component: ProductDetailComponent},
    { path: 'detail', component: ProductDetailComponent }    
]