import { Component, inject, OnInit, signal, WritableSignal } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { Router } from '@angular/router';

import { Technology } from '@core/model/technology';
import { ModelMapperService } from '@core/model/model-mapper.service';
import { TechnologyService } from '@features/technology/data/technology.service';
import { RequestStateService } from '@core/ui/request-state.service';

@Component({
  selector: 'app-technology',
  imports: [MatTableModule, MatButtonModule, MatCardModule],
  templateUrl: './technology.component.html',
  styleUrl: './technology.component.scss'
})
export class TechnologyComponent implements OnInit {

  // Uso de inyección de dependencias moderna y consistente
  private readonly technologyService = inject(TechnologyService);
  private readonly requestStateService = inject(RequestStateService);
  private readonly modelMapperService = inject(ModelMapperService);
  private readonly router = inject(Router);

  technologies: WritableSignal<Technology[]> = signal([]);
  
  // Enlazamos directamente los estados reactivos globales de UI
  readonly isLoading = this.requestStateService.isLoading;
  readonly errorMessage = this.requestStateService.errorMessage;
  readonly emptyMessage = 'No technologies found.';
  
  displayedColumns: string[] = ['name', 'description', 'countProducts', 'countProjects', 'countContributors', 'time'];
  dataSource = this.technologies;

  ngOnInit(): void {
    // 1. Iniciamos el spinner y reseteamos errores previos automáticos
    this.requestStateService.start();

    this.technologyService.getAll().subscribe({
      next: (res: Technology[]) => {
        const technologyTmp = this.modelMapperService.mapTechnologyList(res as unknown[]);
        this.technologies.set(technologyTmp);
        // 2. Apagamos el estado de carga al recibir los datos con éxito
        this.requestStateService.finish();
      }
      // NOTA: El bloque 'error' se elimina por completo. 
      // El 'errorInterceptor' interceptará cualquier fallo de Spring Boot (ej. 401, 500) 
      // y actualizará el state global provocando que la UI pinte el mat-error automáticamente.
    });
  }

  hasItems(): boolean {
    return this.technologies().length > 0;
  }

  edit(id: number): void {
    this.router.navigate(['/pvt/technology/detail', id]);
  }

  create(): void {
    this.router.navigate(['/pvt/technology/detail']);  
  }
}
