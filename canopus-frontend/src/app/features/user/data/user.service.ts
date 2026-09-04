import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { User } from '@core/model/user';
import { ApiBaseService } from '@core/api/api-base.service';
import { PageResponse } from '@core/api/page-response';

@Injectable({
  providedIn: 'root'
})
export class UserService extends ApiBaseService<User> {
  protected override readonly resourcePath = '/user';

  getAll(): Observable<User[]> {
    return this.httpGet<User[]>('/user');
  }

  getPage(page: number, size: number): Observable<PageResponse<User>> {
    return this.httpGet<PageResponse<User>>(`/user/page?page=${page}&size=${size}`);
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

  updateMe(user: Partial<User>): Observable<User> {
    return this.httpPut<User>('/user/me', user);
  }

  changePassword(currentPassword: string, newPassword: string): Observable<void> {
    return this.httpPut<void>('/user/me/password', { currentPassword, newPassword });
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
