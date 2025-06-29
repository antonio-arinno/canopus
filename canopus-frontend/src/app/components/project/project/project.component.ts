import { Component, inject, WritableSignal, signal, viewChild} from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatAccordion, MatExpansionModule } from '@angular/material/expansion';
import { MatCardModule } from '@angular/material/card';

import {MatButtonToggleModule} from '@angular/material/button-toggle';
import {FormControl, ReactiveFormsModule} from '@angular/forms';

import { Product } from '@core/model/product';
import { Router } from '@angular/router';
import { Project } from '@core/model/project';
import { TechnologyService } from '@core/services/technology.service';
import { Technology } from '@core/model/technology';
import { ProjectService } from '@core/services/project.service';
import { User } from '@core/model/user';
import { Status } from '@core/model/status';

@Component({
  selector: 'app-project',
  imports: [MatCardModule, MatExpansionModule, MatTableModule, MatButtonModule, MatFormFieldModule, MatButtonToggleModule, ReactiveFormsModule],
  templateUrl: './project.component.html',
  styleUrl: './project.component.scss'
})
export class ProjectComponent {

  responsibleControl = new FormControl('personal');
  stateControl = new FormControl('opened');
  modoControl = new FormControl('collapsed');
  accordion = viewChild.required(MatAccordion);

  technologyService = inject(TechnologyService);
  projectService = inject(ProjectService);
  router = inject(Router);

  technologies: WritableSignal<Technology[]> = signal([]);
  displayedColumns: string[] = ['name', 'description', 'status', 'contributors', 'time'];
//  dataSource = this.technologies;

  projects: Project[] = [];
  technologiesTmp: Technology[] = [];
  productsTmp: Product[] = [];
  projectsTmp: Project[] = [];

  error!: string;
  message!: string;
  message2!: string;  

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
*/
  ngOnInit(): void {
    this.projectList();

    this.responsibleControl.valueChanges.subscribe(() => this.projectList());

    this.stateControl.valueChanges.subscribe(() => this.projectList());

    this.modoControl.valueChanges.subscribe(() => {
      if (this.modoControl.value == 'expanded'){
        this.accordion().openAll();
      } else {
        this.accordion().closeAll();
      }
    })
  }
  
  projectList(){
    this.technologies.set([]);
    this.projects = [];
    if(this.responsibleControl.value == 'personal' && this.stateControl.value == 'opened'){
      this.projectService.getPersonalOpened().subscribe({
        next: (res: Project[]) => {
          for (var project of res) this.projects.push(project);
          this.updateList();
          this.technologies.set(this.technologiesTmp); 
        },
        error: (err: any) => {
          this.error = err.error.error;
          this.message = err.error.message;
          this.message2 = err.message;
        },
      });
    } else {
      if(this.responsibleControl.value == 'personal' && this.stateControl.value == 'all'){
        this.projectService.getPersonalAll().subscribe({
          next: (res: Project[]) => {
            for (var project of res) this.projects.push(project);
            this.updateList();
            this.technologies.set(this.technologiesTmp); 
          },
          error: (err: any) => {
            this.error = err.error.error;
            this.message = err.error.message;
            this.message2 = err.message;
          },
        });

      } else {
        if(this.responsibleControl.value == 'global' && this.stateControl.value == 'opened'){
          this.projectService.getGlobalOpened().subscribe({
            next: (res: Project[]) => {
              for (var project of res) this.projects.push(project);
              this.updateList();
              this.technologies.set(this.technologiesTmp); 
            },
            error: (err: any) => {
              this.error = err.error.error;
              this.message = err.error.message;
              this.message2 = err.message;
            },
          });
        } else {
          if(this.responsibleControl.value == 'global' && this.stateControl.value == 'all'){
            this.projectService.getGlobalAll().subscribe({
              next: (res: Project[]) => {
                for (var project of res) this.projects.push(project);
                this.updateList();
                this.technologies.set(this.technologiesTmp); 
              },
              error: (err: any) => {
                this.error = err.error.error;
                this.message = err.error.message;
                this.message2 = err.message;
              },
            });
          
          }  
        }
      } 
    }
    this.modoControl.setValue('collapsed');
  }

  updateList(){
    this.technologiesTmp = [];
    let existTech = false;
    let existProd = false;
    
    for (let project of this.projects) {
      existTech = false;
      existProd = false;
      for (let technology of this.technologiesTmp) {
        if (project.technology.id === technology.id){
          existTech = true;
          for(let product of technology.products){
            if(project.product.id === product.id){
              product.projects.push(Project.fromObject(project));//(project);
              existProd = true
            }
          }
        }
      }
      if (!existTech && !existProd){
        
        this.updateProject(project);
        this.productsTmp = [];
        this.productsTmp.push({
          id: project.product.id,
          name: project.product.name,
          description: '',
          technology: new Technology,
          responsible: new User,
          backup: new User,
          countProjects: NaN,
          countContributors: NaN,
          time:NaN,
          avgTime:NaN,
          avgDuration:NaN,
          projects: this.projectsTmp
        });
        
        this.technologiesTmp.push({
          id: project.technology.id,
          name: project.technology.name,
          description: '',
          responsible: new User,
          countProducts: NaN,
          countProjects: NaN,
          countContributors: NaN,
          time: NaN,
          products: this.productsTmp
        });
      } else 
        if (!existProd){
          for (let technology of this.technologiesTmp) {
            if (project.technology.id === technology.id){
              this.updateProject(project);
              technology.products.push({
                id: project.product.id,
                name: project.product.name,
                description: '',
                technology: new Technology,
                responsible: new User,
                backup: new User,
                countProjects: NaN,
                countContributors: NaN,
                time: NaN,
                avgTime: NaN,
                avgDuration: NaN,
                projects: this.projectsTmp});
            }
          }
        }
    }      
  }

  updateProject(project: Project){
    this.projectsTmp = [];
    let projecttmp = new Project;
    projecttmp.id = project.id;
    projecttmp.name = project.name;
    projecttmp.description = project.description;
    projecttmp.product = new Product;
    projecttmp.technology = new Technology;
    projecttmp.dateDev = project.dateDev;
    projecttmp.datePre = project.datePre;
    projecttmp.datePro = project.datePro;
    projecttmp.responsible = new User;
    projecttmp.countContributors = project.countContributors;
    projecttmp.contributors = [];
    projecttmp.time = project.time;
    this.projectsTmp.push(Project.fromObject(projecttmp))
  }  
    /*
    ({
      id: project.id,
      name: project.name,
      description: project.description,
      product: new Product,
      technology: new Technology,
      dateDev: project.dateDev,
      datePre: project.datePre,
      datePro: project.datePro,
      status: project.status,
      responsible: new User,
      countContributors: project.countContributors,
      contributors: [],
      time: project.time
    });
    */


  edit(id: number):void {
    this.router.navigate(['/pvt/project/detail', id]);
  }

  create(){
    this.router.navigate(['/pvt/project/detail']);  
  }  

}



/*
updateList(){
  this.technologiesTmp = [];
  let exist = false;
  
  for (let project of this.projects) {
    exist = false;
    for (let technology of this.technologiesTmp) {
      if (project.technology.id === technology.id){
//          technology.products.push(product)
        exist = true;
      }
    }
    if (!exist){
      this.projectsTmp = [];
      this.projectsTmp.push({
        id: project.id,
        name: project.name,
        description: project.description,
        product: new Product,
        technology: new Technology,
        status: Status.Development,
        responsible: new User,
        contributors: [],
        time: 0,
        getCountContributors: function (): number {
          throw new Error('Function not implemented.');
        }
      })

      this.productsTmp = [];
      this.productsTmp.push({
        id: project.product.id,
        name: project.product.name,
        description: '',
        technology: new Technology,
        responsible: new User,
        countProjects: NaN,
        projects: this.projectsTmp

      });
      
      this.technologiesTmp.push({
        id: project.technology.id,
        name: project.technology.name,
        description: '',
        responsible: new User,
        countProducts: NaN,
        products: this.productsTmp
      });
    }
  }
    */