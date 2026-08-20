import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { User } from '@core/model/user';
import { ApiBaseService } from '@core/api/api-base.service';

@Injectable({
  providedIn: 'root'
})
export class UserService extends ApiBaseService<User> {
  protected override readonly resourcePath = '/user';

  getAll(): Observable<User[]> {
    return this.httpGet<User[]>('/user');
  }

  getAllIdName(): Observable<User[]> {
    return this.httpGet<User[]>('/user/idname');
  }

  getByTechnology(id: number): Observable<User[]> {
    return this.httpGet<User[]>(`/user/technology/${id}`);
  }

  get(id: number): Observable<User> {
    return this.httpGetById(id);
  }

  getMe(): Observable<User> {
    return this.httpGet<User>('/user/me');
  }

  update(user: User): Observable<User> {
    return this.httpUpdate(user.id!, user);
  }

  create(user: User): Observable<User> {
    return this.httpCreate(user);
  }

  delete(id: number): Observable<void> {
    return this.httpDeleteById(id);
  }

  getSelection(term: string): Observable<User[]> {
    return this.httpGet<User[]>(`/user/select/${term}`);
  }
}
