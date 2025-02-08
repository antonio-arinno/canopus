import { Component, inject, WritableSignal, signal, viewChild} from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatAccordion, MatExpansionModule } from '@angular/material/expansion';
import { Product } from '@core/model/product';
import { Router } from '@angular/router';
import { Project } from '@core/model/project';
import { TechnologyService } from '@core/services/technology.service';
import { Technology } from '@core/model/technology';

@Component({
  selector: 'app-backup',
  imports: [MatExpansionModule, MatTableModule, MatButtonModule, MatFormFieldModule],
  templateUrl: './backup.component.html',
  styleUrl: './backup.component.scss'
})
export class BackupComponent {

  accordion = viewChild.required(MatAccordion);

  technologyService = inject(TechnologyService);
  router = inject(Router);

  technologies: WritableSignal<Technology[]> = signal([]);
  displayedColumns: string[] = ['name', 'description', 'contributors', 'time', 'edit'];
  dataSource = this.technologies;

  error!: string;
  message!: string;
  message2!: string;  

  ngOnInit(): void {
    this.technologyService.getAll().subscribe({
      next: (res: Technology[]) => {
        let technologiesTmp = res.map(function (technology){
          let technologyTmp = { ...technology }
          technologyTmp.products = [];
          technology.products.map(function (product){
            let productTmp = { ...product }
            productTmp.projects = []
            technologyTmp.products.push(Product.fromObject(productTmp))
            product.projects.map(function (project){
              productTmp.projects.push(Project.fromObject(project))
            })
          }) 
          return Technology.fromObject(technologyTmp);
        });
        this.technologies.set(technologiesTmp);

/*
        let technologiesTmp = res.map(function (technology){
          let technologyTmp = { ...technology } 
          technologyTmp.products = [];
          technology.products.map(function (product){
            let productTmp = { ...product }
            productTmp.projects = [];
            product.projects.map(function (project){
              productTmp.projects.push(Project.fromObject(project))
            })
            technology.products.push(Product.fromObject(product))
          })
          return Technology.fromObject(technology)
        });
        this.technologies.set(technologiesTmp);
*/
        
        

                
/*
        let productsTmp = res.map(function (product){
          let productTmp = { ...product }
          productTmp.projects = [];
          product.projects.map(function (project){
              productTmp.projects.push(Project.fromObject(project));
          });
          return Product.fromObject(productTmp);
        });       
        this.products.set(productsTmp);
*/        
      },
      error: (err: any) => {
        this.error = err.error.error;
        this.message = err.error.message;
        this.message2 = err.message;
      },
    });
  }  

  edit(id: number):void {
    this.router.navigate(['/pvt/product/detail', id]);
  }

  create(){
    this.router.navigate(['/pvt/product/detail']);  
  }  

}
