import { Injectable } from "@angular/core";
import { Observable } from 'rxjs';
import { Technology } from "@core/model/technology";
import { ApiBaseService } from '@core/api/api-base.service';

export interface TechnologyRequest {
    name: string;
    description?: string;
    responsibleId: number;
}

@Injectable({
  providedIn: 'root'
})
export class TechnologyService extends ApiBaseService<Technology> {
  protected override readonly resourcePath = '/technology';

    getAll(): Observable<Technology[]> {
        return this.httpGet<Technology[]>('/technology');
    }

    create(technology: TechnologyRequest): Observable<Technology> {
        return this.httpCreate(technology);
    }

    get(id: number): Observable<Technology> {
        return this.httpGetById(id);
    }

    delete(id: number): Observable<void> {
        return this.httpDeleteById(id);
    }

    update(id: number, technology: TechnologyRequest): Observable<Technology> {
        return this.httpUpdate(id, technology);
    }

    getSelection(term: string): Observable<Technology[]> {
        return this.httpGet<Technology[]>(`/technology/select/${term}`);
    }
}