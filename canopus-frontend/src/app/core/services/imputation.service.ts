import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Imputation } from '@core/model/imputation';
import { URL_BACKEND } from '@shared/config';


@Injectable({
  providedIn: 'root'
})
export class ImputationService {

  private http = inject(HttpClient);

  getAll() {
    return this.http.get<Imputation[]>(URL_BACKEND + '/imputation');
  }

  get(id: number){
    return this.http.get<Imputation>(URL_BACKEND + `/imputation/${id}`);
  }

  getByDate(date: string){
    return this.http.get<Imputation>(URL_BACKEND + `/imputation/date/${date}`);

  }

  update(imputation: Imputation){
    return this.http.put(URL_BACKEND + `/imputation/${imputation.id}`, imputation);
  }

  create(imputation: Imputation){
    return this.http.post(URL_BACKEND + `/imputation`, imputation);
  }

  delete(id: number){
    return this.http.delete(URL_BACKEND + `/imputation/${id}`);
  }  

}
