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

  technologyService = inject(TechnologyService);
  requestStateService = inject(RequestStateService);
  router = inject(Router);

  technologies: WritableSignal<Technology[]> = signal([]);
  readonly isLoading = this.requestStateService.isLoading;
  readonly errorMessage = this.requestStateService.errorMessage;
  readonly emptyMessage = 'No technologies found.';
  displayedColumns: string[] = ['name', 'description', 'countProducts', 'countProjects', 'countContributors', 'time'];
  dataSource = this.technologies;

  modelMapperService = inject(ModelMapperService);

  ngOnInit(): void {
    this.requestStateService.start();
    this.technologyService.getAll().subscribe({
      next: (res: Technology[]) => {
        const technologyTmp = this.modelMapperService.mapTechnologyList(res as unknown[]);
        this.technologies.set(technologyTmp);
        this.requestStateService.finish();
      },
      error: (err: any) => this.requestStateService.setError(err),
    });
  }

  hasItems(): boolean {
    return this.technologies().length > 0;
  }

  edit(id: number):void {
    this.router.navigate(['/pvt/technology/detail', id]);
  }

  create(){
    this.router.navigate(['/pvt/technology/detail']);  
  }

}
