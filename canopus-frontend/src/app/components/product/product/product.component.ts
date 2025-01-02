import { Component, inject, OnInit, signal, WritableSignal } from '@angular/core';
import { Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';

import { Product } from '@core/model/product';
import { ProductService } from '@core/services/product.service';

@Component({
  selector: 'app-product',
  imports: [MatTableModule, MatButtonModule],
  templateUrl: './product.component.html',
  styleUrl: './product.component.scss'
})
export class ProductComponent implements OnInit {

  productService = inject(ProductService);
  router = inject(Router);

  products: WritableSignal<Product[]> = signal([]);
  displayedColumns: string[] = ['name', 'description', 'countContributors', 'countProjects', 'time'];
  dataSource = this.products;

  ngOnInit(): void {
    this.productService.getAll().subscribe({
      next: (res: Product[]) => {
        console.log(res);
        let productsTmp = res.map(function (product){
          return Product.fromObject(product);
        });
        this.products.set(productsTmp);
      },
      error: (err: any) => console.log(err),
    });
  }

  edit(id: number):void {
    this.router.navigate(['/pvt/product/detail', id]);
  }

  create(){
    this.router.navigate(['/pvt/product/detail']);  
  }

}
