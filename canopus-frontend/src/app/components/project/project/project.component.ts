import { Component, inject, WritableSignal, signal, viewChild} from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatAccordion, MatExpansionModule } from '@angular/material/expansion';
import { MatCardModule } from '@angular/material/card';
import { Product } from '@core/model/product';
import { Router } from '@angular/router';
import { Project } from '@core/model/project';
import { TechnologyService } from '@core/services/technology.service';
import { Technology } from '@core/model/technology';

@Component({
  selector: 'app-project',
  imports: [MatCardModule, MatExpansionModule, MatTableModule, MatButtonModule, MatFormFieldModule],
  templateUrl: './project.component.html',
  styleUrl: './project.component.scss'
})
export class ProjectComponent {

  accordion = viewChild.required(MatAccordion);

  technologyService = inject(TechnologyService);
  router = inject(Router);

  technologies: WritableSignal<Technology[]> = signal([]);
//  displayedColumns: string[] = ['name', 'description', 'contributors', 'time', 'edit'];
  displayedColumns: string[] = ['name', 'description', 'contributors', 'time'];
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
      },
      error: (err: any) => {
        this.error = err.error.error;
        this.message = err.error.message;
        this.message2 = err.message;
      },
    });
  }  

  edit(id: number):void {
    this.router.navigate(['/pvt/project/detail', id]);
  }

  create(){
    this.router.navigate(['/pvt/project/detail']);  
  }  

}
