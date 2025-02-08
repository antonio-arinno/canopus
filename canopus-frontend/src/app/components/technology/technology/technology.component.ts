import { Component, inject, signal, WritableSignal } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { Router } from '@angular/router';

import { Technology } from '@core/model/technology';
import { TechnologyService } from '@core/services/technology.service';

@Component({
  selector: 'app-technology',
  imports: [MatTableModule, MatButtonModule, MatCardModule],
  templateUrl: './technology.component.html',
  styleUrl: './technology.component.scss'
})
export class TechnologyComponent {

  technologyService = inject(TechnologyService);
  router = inject(Router);

  technologies: WritableSignal<Technology[]> = signal([]);
  displayedColumns: string[] = ['name', 'description', 'countProducts', 'countProjects', 'countContributors', 'time'];
  dataSource = this.technologies;

  ngOnInit(): void {
    this.technologyService.getAll().subscribe({
      next: (res: Technology[]) => {
        console.log(res)
        let technologyTmp = res.map(function (technology){
          return Technology.fromObject(technology);
        });
        this.technologies.set(technologyTmp);
      },
      error: (err: any) => console.log(err),
    });
  }

  edit(id: number):void {
    this.router.navigate(['/pvt/technology/detail', id]);
  }

  create(){
    this.router.navigate(['/pvt/technology/detail']);  
  }

}
