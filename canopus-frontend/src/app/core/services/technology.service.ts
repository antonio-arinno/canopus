import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { URL_BACKEND } from "@shared/config";
import { Technology } from "@core/model/technology";

@Injectable({
  providedIn: 'root'
})

export class TechnologyService {

    private http = inject(HttpClient);

    getAll(){
        return this.http.get<Technology[]>(URL_BACKEND + '/technology');
    }

    create(technology: Technology){
        return this.http.post(URL_BACKEND + `/technology`, technology);
    }

    get(id: number){
        return this.http.get<Technology>(URL_BACKEND + `/technology/${id}`);
    }

    delete(id: number){
        return this.http.delete(URL_BACKEND + `/technology/${id}`);
    }

    update(technology: Technology){
        return this.http.put(URL_BACKEND + `/technology/${technology.id}`, technology);
    }    

    getSelection(term: string){
        return this.http.get<Technology[]>(URL_BACKEND + `/technology/select/${term}`);
    }

}