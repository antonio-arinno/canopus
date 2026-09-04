import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Imputation } from '@core/model/imputation';
import { ImputationSummary } from '@core/model/imputation-summary';
import { ApiBaseService } from '@core/api/api-base.service';

export interface ImputationRequest {
  date: string;
  items: Array<{
    projectId: number;
    time: number;
  }>;
}

@Injectable({
  providedIn: 'root'
})
export class ImputationService extends ApiBaseService<Imputation> {
  protected override readonly resourcePath = '/imputation';

  getAll(): Observable<Imputation[]> {
    return this.httpGet<Imputation[]>('/imputation');
  }

  get(id: number): Observable<Imputation> {
    return this.httpGetById(id);
  }

  getByDate(date: string): Observable<Imputation> {
    return this.httpGet<Imputation>(`/imputation/date/${date}`);
  }

  getByProject(id: number): Observable<ImputationSummary[]> {
    return this.httpGet<ImputationSummary[]>(`/imputation/project/${id}`);
  }

  update(id: number, imputation: ImputationRequest): Observable<Imputation> {
    return this.httpUpdate(id, imputation);
  }

  create(imputation: ImputationRequest): Observable<Imputation> {
    return this.httpCreate(imputation);
  }

  delete(id: number): Observable<void> {
    return this.httpDeleteById(id);
  }
}
