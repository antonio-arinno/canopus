import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Product, ProductRequest } from '@core/model/product';
import { ApiBaseService } from '@core/api/api-base.service';

@Injectable({
  providedIn: 'root'
})
export class ProductService extends ApiBaseService<Product> {
  protected override readonly resourcePath = '/product';

  getByScope(responsible: string): Observable<Product[]> {
    const normalizedResponsible = responsible ?? 'personal';

    if (normalizedResponsible === 'personal') {
      return this.getByResponsibleMe();
    }

    return this.getAll();
  }

  getAll(): Observable<Product[]> {
    return this.httpGet<Product[]>('/product');
  }

  getByResponsibleMe(): Observable<Product[]> {
    return this.httpGet<Product[]>('/product/responsible');
  }

  getByResponsibleOrBackupMe(): Observable<Product[]> {
    return this.httpGet<Product[]>('/product/responsible-or-backup');
  }

  getByContributor(id: number): Observable<Product[]> {
    return this.httpGet<Product[]>(`/product/contributor/${id}`);
  }

  getByTechnology(id: number): Observable<Product[]> {
    return this.httpGet<Product[]>(`/product/technology/${id}`);
  }

  get(id: number): Observable<Product> {
    return this.httpGetById(id);
  }

  update(product: ProductRequest): Observable<Product> {
    return this.httpUpdate(product.id!, product);
  }

  create(product: ProductRequest): Observable<Product> {
    return this.httpCreate(product);
  }

  delete(id: number): Observable<void> {
    return this.httpDeleteById(id);
  }

  getSelection(term: string): Observable<Product[]> {
    return this.httpGet<Product[]>(`/product/select/${term}`);
  }
}