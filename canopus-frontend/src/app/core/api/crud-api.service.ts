import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '@environments/environment';

@Injectable({
  providedIn: 'root'
})
export abstract class CrudApiService<T> {
  protected readonly http = inject(HttpClient);
  protected readonly apiUrl = environment.apiUrl;

  protected abstract readonly resourcePath: string;

  protected buildUrl(resource: string): string {
    const normalizedResource = resource.startsWith('/') ? resource.slice(1) : resource;
    return `${this.apiUrl}/${normalizedResource}`;
  }

  protected buildDetailUrl(id: number | string): string {
    const normalizedPath = this.resourcePath.startsWith('/') ? this.resourcePath : `/${this.resourcePath}`;
    return `${this.apiUrl}${normalizedPath}/${id}`;
  }

  protected httpGet<R>(resource: string) {
    return this.http.get<R>(this.buildUrl(resource));
  }

  protected httpPost<R>(resource: string, body: unknown) {
    return this.http.post<R>(this.buildUrl(resource), body);
  }

  protected httpPut<R>(resource: string, body: unknown) {
    return this.http.put<R>(this.buildUrl(resource), body);
  }

  protected httpDelete<R>(resource: string) {
    return this.http.delete<R>(this.buildUrl(resource));
  }

  protected httpGetById(id: number | string) {
    return this.http.get<T>(this.buildDetailUrl(id));
  }

  protected httpUpdate(id: number | string, body: unknown) {
    return this.http.put<T>(this.buildDetailUrl(id), body);
  }

  protected httpCreate(body: unknown) {
    return this.http.post<T>(this.buildUrl(this.resourcePath), body);
  }

  protected httpDeleteById(id: number | string) {
    return this.http.delete<void>(this.buildDetailUrl(id));
  }
}
