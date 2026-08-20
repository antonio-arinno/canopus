import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Project } from '@core/model/project';
import { ApiBaseService } from '@core/api/api-base.service';

@Injectable({
  providedIn: 'root'
})
export class ProjectService extends ApiBaseService<Project> {
  protected override readonly resourcePath = '/project';

  getByScope(responsible: string, state: string): Observable<Project[]> {
    const normalizedResponsible = responsible ?? 'personal';
    const normalizedState = state ?? 'opened';

    switch (normalizedResponsible) {
      case 'personal':
        return normalizedState === 'all' ? this.getPersonalAll() : this.getPersonalOpened();
      case 'global':
        return normalizedState === 'all' ? this.getGlobalAll() : this.getGlobalOpened();
      default:
        return this.getPersonalOpened();
    }
  }

  getPersonalOpened(): Observable<Project[]> {
    return this.httpGet<Project[]>('/project');
  }

  getPersonalAll(): Observable<Project[]> {
    return this.httpGet<Project[]>('/project/all');
  }

  getGlobalOpened(): Observable<Project[]> {
    return this.httpGet<Project[]>('/project/global');
  }

  getGlobalAll(): Observable<Project[]> {
    return this.httpGet<Project[]>('/project/globalall');
  }

  get(id: number): Observable<Project> {
    return this.httpGetById(id);
  }

  getByProduct(id: number): Observable<Project[]> {
    return this.httpGet<Project[]>(`/project/product/${id}`);
  }

  update(project: Project): Observable<Project> {
    return this.httpUpdate(project.id!, project);
  }

  create(project: Project): Observable<Project> {
    return this.httpCreate(project);
  }

  delete(id: number): Observable<void> {
    return this.httpDeleteById(id);
  }

  getSelection(term: string): Observable<Project[]> {
    return this.httpGet<Project[]>(`/project/select/${term}`);
  }

  getByContributor(): Observable<Project[]> {
    return this.httpGet<Project[]>('/project/contributor');
  }

  getByOpenAndDate(date: string): Observable<Project[]> {
    return this.httpGet<Project[]>(`/project/contributor/${date}`);
  }
}
