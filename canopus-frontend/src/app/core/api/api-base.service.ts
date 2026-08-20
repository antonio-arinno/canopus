import { Injectable } from '@angular/core';
import { CrudApiService } from './crud-api.service';

@Injectable({
  providedIn: 'root'
})
export class ApiBaseService<T = unknown> extends CrudApiService<T> {
  protected override readonly resourcePath: string = '';
}
