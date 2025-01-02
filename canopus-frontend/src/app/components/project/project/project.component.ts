import { Component, inject, WritableSignal, signal} from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { ProjectService } from '@core/services/project.service';
import { Project } from '@core/model/project';
import { Router } from '@angular/router';

@Component({
  selector: 'app-project',
  imports: [MatTableModule, MatButtonModule, MatFormFieldModule],
  templateUrl: './project.component.html',
  styleUrl: './project.component.scss'
})
export class ProjectComponent {

  projectService = inject(ProjectService);
  router = inject(Router);

  projects: WritableSignal<Project[]> = signal([]);
  displayedColumns: string[] = ['name', 'description', 'contributors', 'time', 'edit'];
  dataSource = this.projects;

  error!: string;
  message!: string;
  message2!: string;  

  ngOnInit(): void {
    this.projectService.getAll().subscribe({
      next: (res: Project[]) => {
//        this.projects.set(res);
        console.log(res);
        let projectsTmp = res.map(function (project){
          return Project.fromObject(project);
        });
        this.projects.set(projectsTmp);
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
