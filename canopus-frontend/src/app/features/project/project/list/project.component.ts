import { Component, inject, OnInit, signal, WritableSignal, viewChild } from '@angular/core';
import { Router } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';

import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatAccordion, MatExpansionModule } from '@angular/material/expansion';
import { MatCardModule } from '@angular/material/card';
import { MatButtonToggleModule } from '@angular/material/button-toggle';

import { Project } from '@core/model/project';
import { Technology } from '@core/model/technology';
import { ProjectService } from '@features/project/data/project.service';
import { ProjectListViewService } from '@features/project/data/project-list-view.service';
import { ModelMapperService } from '@core/model/model-mapper.service';
import { RequestStateService } from '@core/ui/request-state.service';

@Component({
  selector: 'app-project',
  imports: [
    MatCardModule, 
    MatExpansionModule, 
    MatTableModule, 
    MatButtonModule, 
    MatFormFieldModule, 
    MatButtonToggleModule, 
    ReactiveFormsModule
  ],
  templateUrl: './project.component.html',
  styleUrl: './project.component.scss'
})
export class ProjectComponent implements OnInit {

  // Inyección funcional moderna con modificadores privados de solo lectura
  private readonly projectService = inject(ProjectService);
  private readonly projectListViewService = inject(ProjectListViewService);
  private readonly modelMapperService = inject(ModelMapperService);
  private readonly requestStateService = inject(RequestStateService);
  private readonly router = inject(Router);

  responsibleControl = new FormControl('personal');
  stateControl = new FormControl('opened');
  modoControl = new FormControl('collapsed');
  
  // Consulta de vista optimizada para Angular 19
  accordion = viewChild.required(MatAccordion);

  technologies: WritableSignal<Technology[]> = signal([]);
  projects: Project[] = [];

  // Enlaces directos a los estados reactivos globales de UI
  readonly isLoading = this.requestStateService.isLoading;
  readonly errorMessage = this.requestStateService.errorMessage;
  readonly emptyMessage = 'No projects found.';

  displayedColumns: string[] = ['name', 'description', 'status', 'contributors', 'time'];

  ngOnInit(): void {
    this.projectList();

    // Suscripción reactiva ante cambios en los filtros de usuario
    this.responsibleControl.valueChanges.subscribe(() => this.projectList());
    this.stateControl.valueChanges.subscribe(() => this.projectList());

    this.modoControl.valueChanges.subscribe(mode => {
      if (mode === 'expanded') {
        this.accordion().openAll();
      } else {
        this.accordion().closeAll();
      }
    });
  }
  
  projectList(): void {
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
      }
      // NOTA: Callback 'error:' eliminado. El 'errorInterceptor' captura
      // automáticamente cualquier fallo en el backend (401, 404, 500) y
      // actualiza el RequestStateService provocando que la UI pinte la alerta de forma nativa.
    });

    // Cambiamos el estado visual evitando que emita un evento secundario innecesario hacia el acordeón
    this.modoControl.setValue('collapsed', { emitEvent: false });
  }

  updateList(): void {
    const techTree = this.projectListViewService.buildTechnologyTree(this.projects);
    this.technologies.set(techTree);
  }

  hasItems(): boolean {
    return this.technologies().length > 0;
  }

  edit(id: number): void {
    this.router.navigate(['/pvt/project/detail', id]);
  }

  create(): void {
    this.router.navigate(['/pvt/project/detail']);
  }
}

