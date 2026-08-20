import { Component, inject, OnInit, signal, WritableSignal, viewChild} from '@angular/core';
import { Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatAccordion, MatExpansionModule } from '@angular/material/expansion';
import { MatCardModule } from '@angular/material/card';

import {MatButtonToggleModule} from '@angular/material/button-toggle';
import {FormControl, ReactiveFormsModule} from '@angular/forms';

import { Product } from '@core/model/product';
import { ProductService } from '@features/product/data/product.service';
import { ProductListViewService } from '@features/product/data/product-list-view.service';
import { TechnologyService } from '@features/technology/data/technology.service';
import { Technology } from '@core/model/technology';
import { ModelMapperService } from '@core/model/model-mapper.service';
import { RequestStateService } from '@core/ui/request-state.service';

@Component({
  selector: 'app-product',
  imports: [MatCardModule, MatTableModule, MatButtonModule, MatExpansionModule, MatFormFieldModule, MatButtonToggleModule, ReactiveFormsModule],
  templateUrl: './product.component.html',
  styleUrl: './product.component.scss'
})
export class ProductComponent implements OnInit {

  modoControl = new FormControl('collapsed');
  responsibleControl = new FormControl('personal');

  accordion = viewChild.required(MatAccordion);

  technologyService = inject(TechnologyService);
  productService = inject(ProductService);
  productListViewService = inject(ProductListViewService);
  modelMapperService = inject(ModelMapperService);
  requestStateService = inject(RequestStateService);
  router = inject(Router);

  technologies: WritableSignal<Technology[]> = signal([]);
  products: Product[] = [];
  readonly isLoading = this.requestStateService.isLoading;
  readonly errorMessage = this.requestStateService.errorMessage;
  readonly emptyMessage = 'No products found.';

  displayedColumns: string[] = ['name', 'description', 'responsible', 'backup', 'time'];
//  dataSource = this.technologies;
  booksToDisplay: any;
/*
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
*/

  ngOnInit(): void {

    this.productList();
    this.responsibleControl.valueChanges.subscribe(() => this.productList());
    this.modoControl.valueChanges.subscribe(() => {
      if (this.modoControl.value == 'expanded'){
        this.accordion().openAll();
      } else {
        this.accordion().closeAll();
      }
    })
  }

  productList(){
    this.requestStateService.start();
    this.technologies.set([]);
    this.products = [];

    const responsible = this.responsibleControl.value ?? 'personal';

    this.productService.getByScope(responsible).subscribe({
      next: (res: Product[]) => {
        const payload = this.modelMapperService.mapProductList(res as unknown[]);
        this.products = [...payload];
        this.updateList();
        this.requestStateService.finish();
      },
      error: (err: any) => this.requestStateService.setError(err),
    });

    this.modoControl.setValue('collapsed');
  }

  updateList(){
    const technologies = this.productListViewService.buildTechnologyTree(this.products);
    this.technologies.set(technologies);
  }

  hasItems(): boolean {
    return this.technologies().length > 0;
  }

  edit(id: number):void {
    this.router.navigate(['/pvt/product/detail', id]);
  }

  create(){
    this.router.navigate(['/pvt/product/detail']);  
  }

}
