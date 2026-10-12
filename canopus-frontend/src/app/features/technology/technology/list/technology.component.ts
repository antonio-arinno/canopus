import { Component, inject, OnInit, signal, WritableSignal } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { Router } from '@angular/router';

import { Technology } from '@core/model/technology';
import { ModelMapperService } from '@core/model/model-mapper.service';
import { TechnologyService } from '@features/technology/data/technology.service';
import { RequestStateService } from '@core/ui/request-state.service';

@Component({
  selector: 'app-technology',
  imports: [MatTableModule, MatButtonModule, MatCardModule, MatPaginatorModule],
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
  pageIndex = 0;
  pageSize = 10;
  totalTechnologies = 0;
  
  // Enlazamos directamente los estados reactivos globales de UI
  readonly isLoading = this.requestStateService.isLoading;
  readonly errorMessage = this.requestStateService.errorMessage;
  readonly emptyMessage = 'No technologies found.';
  
  displayedColumns: string[] = ['name', 'description', 'countProducts', 'countProjects', 'countContributors', 'time'];
  dataSource = this.technologies;

  ngOnInit(): void {
    this.loadPage();
  }

  hasItems(): boolean {
    return this.technologies().length > 0;
  }

  changePage(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadPage();
  }

  private loadPage(): void {
    this.requestStateService.start();
    this.technologyService.getPage(this.pageIndex, this.pageSize).subscribe({
      next: (response) => {
        const technologies = this.modelMapperService.mapTechnologyList(response.content as unknown[]);
        this.technologies.set(technologies);
        this.totalTechnologies = response.totalElements;
        this.pageIndex = response.number;
        this.pageSize = response.size;
        this.requestStateService.finish();
      },
      error: () => this.requestStateService.finish()
    });
  }

  edit(id: number): void {
    this.router.navigate(['/pvt/technology/detail', id]);
  }

  create(): void {
    this.router.navigate(['/pvt/technology/detail']);  
  }
}
