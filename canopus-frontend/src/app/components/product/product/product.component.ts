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
import { ProductService } from '@core/services/product.service';
import { TechnologyService } from '@core/services/technology.service';
import { Technology } from '@core/model/technology';
import { User } from '@core/model/user';

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
  router = inject(Router);

  technologies: WritableSignal<Technology[]> = signal([]);
  technologiesTmp: Technology[] = [];
  products: Product[] = [];

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
    this.technologies.set([]);
    this.products = [];

    if(this.responsibleControl.value == 'personal'){
      this.productService.getByResponsibleMe().subscribe({
        next: (res: Product[]) => {
          for (var product of res) this.products.push(product);
          this.updateList();
          this.technologies.set(this.technologiesTmp);    
        },
        error: (err: any) => console.log(err),
      });
    } else {
      this.productService.getAll().subscribe({
        next: (res: Product[]) => {
          for (var product of res) this.products.push(product);
          this.updateList();
          this.technologies.set(this.technologiesTmp);    
        },
        error: (err: any) => console.log(err),
      });
    } 
    this.modoControl.setValue('collapsed'); 
  }

  updateList(){
    this.technologiesTmp = [];
    let exist = false;
    for (let product of this.products) {
      exist = false;
      for (let technology of this.technologiesTmp) {
        if (product.technology.id === technology.id){
          technology.products.push(product)
          exist = true;
        }
      }
      if (!exist){
        this.technologiesTmp.push({
          id: product.technology.id,
          name: product.technology.name,
          description: '',
          responsible: new User,
          countProducts: NaN,
          countProjects: NaN,
          countContributors: NaN,
          time: NaN,
          products: [product]
        });
      }
    }
  }


  edit(id: number):void {
    this.router.navigate(['/pvt/product/detail', id]);
  }

  create(){
    this.router.navigate(['/pvt/product/detail']);  
  }

}
