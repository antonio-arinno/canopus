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
