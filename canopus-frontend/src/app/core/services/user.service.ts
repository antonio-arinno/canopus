import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { User } from '@core/model/user';
import { URL_BACKEND } from '@shared/config';

@Injectable({
  providedIn: 'root'
})

export class UserService {

  private http = inject(HttpClient);

  getAll(){
    return this.http.get<User[]>(URL_BACKEND + '/user');
  }

  get(id: number){
    return this.http.get<User>(URL_BACKEND + `/user/${id}`);
  }

  update(user: User){
    return this.http.put(URL_BACKEND + `/user/${user.id}`, user);
  }

  create(user: User){
    return this.http.post(URL_BACKEND + `/user`, user);
  }

  delete(id: number){
    return this.http.delete(URL_BACKEND + `/user/${id}`);
  }

  getSelection(term: string){
    return this.http.get<User[]>(URL_BACKEND + `/user/select/${term}`);
  }

}
