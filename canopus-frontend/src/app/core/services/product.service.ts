import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Product } from '@core/model/product';
import { URL_BACKEND } from '@shared/config';

@Injectable({
  providedIn: 'root'
})

export class ProductService {

  private http = inject(HttpClient);

  getAll(){
    return this.http.get<Product[]>(URL_BACKEND + '/product');
  }
/*
  getByContributor(id: number){
    return this.http.get<Product[]>(URL_BACKEND + `/product/contributor/${id}`);
  }
*/
  getByContributor(){
    return this.http.get<Product[]>(URL_BACKEND + `/product/contributor`);
  }

  get(id: number){
    return this.http.get<Product>(URL_BACKEND + `/product/${id}`);
  }

  update(product: Product){
    return this.http.put(URL_BACKEND + `/product/${product.id}`, product);
  }

  create(product: Product){
    return this.http.post(URL_BACKEND + `/product`, product);
  }

  delete(id: number){
    return this.http.delete(URL_BACKEND + `/product/${id}`);
  }

  getSelection(term: string){
    return this.http.get<Product[]>(URL_BACKEND + `/product/select/${term}`);
  }

}