import { Component, inject, WritableSignal, signal, viewChild} from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatAccordion, MatExpansionModule } from '@angular/material/expansion';
import { MatCardModule } from '@angular/material/card';
import { RequestStateService } from '@core/ui/request-state.service';

import {MatButtonToggleModule} from '@angular/material/button-toggle';
import {FormControl, ReactiveFormsModule} from '@angular/forms';

import { Router } from '@angular/router';
import { Project } from '@core/model/project';
import { Technology } from '@core/model/technology';
import { ProjectService } from '@features/project/data/project.service';
import { ProjectListViewService } from '@features/project/data/project-list-view.service';
import { ModelMapperService } from '@core/model/model-mapper.service';

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

  projectService = inject(ProjectService);
  projectListViewService = inject(ProjectListViewService);
  modelMapperService = inject(ModelMapperService);
  requestStateService = inject(RequestStateService);
  router = inject(Router);

  technologies: WritableSignal<Technology[]> = signal([]);
  displayedColumns: string[] = ['name', 'description', 'status', 'contributors', 'time'];

  projects: Project[] = [];

  readonly isLoading = this.requestStateService.isLoading;
  readonly errorMessage = this.requestStateService.errorMessage;
  readonly emptyMessage = 'No projects found.';

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
    this.requestStateService.start();
    this.technologies.set([]);
    this.projects = [];

    const responsible = this.responsibleControl.value ?? 'personal';
    const state = this.stateControl.value ?? 'opened';

    this.projectService.getByScope(responsible, state).subscribe({
      next: (res: Project[]) => {
        const payload = this.modelMapperService.mapProjectList(res as unknown[]);
        this.projects = [...payload];
        this.updateList();
        this.requestStateService.finish();
      },
      error: (err: any) => {
        this.requestStateService.setError(err);
      },
    });

    this.modoControl.setValue('collapsed');
  }

  updateList() {
    const technologies = this.projectListViewService.buildTechnologyTree(this.projects);
    this.technologies.set(technologies);
  }

  hasItems(): boolean {
    return this.technologies().length > 0;
  }

  edit(id: number): void {
    this.router.navigate(['/pvt/project/detail', id]);
  }

  create() {
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