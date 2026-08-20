import { Injectable } from "@angular/core";
import { Observable } from 'rxjs';
import { Technology } from "@core/model/technology";
import { ApiBaseService } from '@core/api/api-base.service';

@Injectable({
  providedIn: 'root'
})
export class TechnologyService extends ApiBaseService<Technology> {
  protected override readonly resourcePath = '/technology';

    getAll(): Observable<Technology[]> {
        return this.httpGet<Technology[]>('/technology');
    }

    create(technology: Technology): Observable<Technology> {
        return this.httpCreate(technology);
    }

    get(id: number): Observable<Technology> {
        return this.httpGetById(id);
    }

    delete(id: number): Observable<void> {
        return this.httpDeleteById(id);
    }

    update(technology: Technology): Observable<Technology> {
        return this.httpUpdate(technology.id!, technology);
    }

    getSelection(term: string): Observable<Technology[]> {
        return this.httpGet<Technology[]>(`/technology/select/${term}`);
    }
}