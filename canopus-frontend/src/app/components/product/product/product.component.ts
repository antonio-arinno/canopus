import { Component, inject, OnInit, signal, WritableSignal, viewChild} from '@angular/core';
import { Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatAccordion, MatExpansionModule } from '@angular/material/expansion';
import { MatCardModule } from '@angular/material/card';

import { Product } from '@core/model/product';
import { ProductService } from '@core/services/product.service';
import { TechnologyService } from '@core/services/technology.service';
import { Technology } from '@core/model/technology';

@Component({
  selector: 'app-product',
  imports: [MatCardModule, MatTableModule, MatButtonModule, MatExpansionModule, MatFormFieldModule],
  templateUrl: './product.component.html',
  styleUrl: './product.component.scss'
})
export class ProductComponent implements OnInit {

  accordion = viewChild.required(MatAccordion);

  technologyService = inject(TechnologyService);
  productService = inject(ProductService);
  router = inject(Router);

  technologies: WritableSignal<Technology[]> = signal([]);
  displayedColumns: string[] = ['name', 'description', 'countProjects', 'countContributors', 'time'];
  dataSource = this.technologies;

  ngOnInit(): void {
    this.technologyService.getAll().subscribe({
      next: (res: Technology[]) => {
        let technologiesTmp = res.map(function (technology){
          let technologyTmp = { ...technology }
          technologyTmp.products = [];
          technology.products.map(function (product){
            let productTmp = { ...product }
            productTmp.projects = []
            technologyTmp.products.push(Product.fromObject(product))
          }) 
          return Technology.fromObject(technologyTmp);
        });
        this.technologies.set(technologiesTmp);  
        console.log(this.technologies());     
      },
      error: (err: any) => console.log(err),
    });
  }  




  /*
  ngOnInit(): void {
    this.productService.getAll().subscribe({
      next: (res: Product[]) => {
        let productsTmp = res.map(function (product){
          return Product.fromObject(product);
        });
        console.log(res);
        this.products.set(productsTmp);
      },
      error: (err: any) => console.log(err),
    });
  }
*/




  edit(id: number):void {
    this.router.navigate(['/pvt/product/detail', id]);
  }

  create(){
    this.router.navigate(['/pvt/product/detail']);  
  }

}
