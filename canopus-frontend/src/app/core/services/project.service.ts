import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Project } from '@core/model/project';
import { URL_BACKEND } from '@shared/config';
import { firstValueFrom } from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class ProjectService {

  private http = inject(HttpClient);

/*
  getAll() {
    return this.http.get<Project[]>(URL_BACKEND + '/project');
  }
*/
  getPersonalOpened(){
    return this.http.get<Project[]>(URL_BACKEND + '/project');
  }

  getPersonalAll(){
    return this.http.get<Project[]>(URL_BACKEND + '/project/all');
  }

  getGlobalOpened(){
    return this.http.get<Project[]>(URL_BACKEND + '/project/global');
  }

  getGlobalAll(){
    return this.http.get<Project[]>(URL_BACKEND + '/project/globalall');
  }

  get(id: number){
    return this.http.get<Project>(URL_BACKEND + `/project/${id}`);
  }

  getByProduct(id: number){
    return this.http.get<Project[]>(URL_BACKEND + `/project/product/${id}`);
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

  getByContributor(){
    return this.http.get<Project[]>(URL_BACKEND + `/project/contributor`);
  }  

  getByOpenAndDate(date: string){
    return this.http.get<Project[]>(URL_BACKEND + `/project/contributor/${date}`);
  }

}
