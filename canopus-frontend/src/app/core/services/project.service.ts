import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Project } from '@core/model/project';
import { URL_BACKEND } from '@shared/config';


@Injectable({
  providedIn: 'root'
})
export class ProjectService {

  private http = inject(HttpClient);


  getAll() {
    return this.http.get<Project[]>(URL_BACKEND + '/project');
  }

  get(id: number){
    return this.http.get<Project>(URL_BACKEND + `/project/${id}`);
  }

  update(project: Project){
    return this.http.put(URL_BACKEND + `/project/${project.id}`, project);
  }

  create(project: Project){
    return this.http.post(URL_BACKEND + `/project`, project);
  }

  delete(id: number){
    return this.http.delete(URL_BACKEND + `/project/${id}`);
  }  

  getSelection(term: string){
    return this.http.get<Project[]>(URL_BACKEND + `/project/select/${term}`);
  }
}
