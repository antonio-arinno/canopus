import { Injectable } from '@angular/core';
import { Imputation } from '@core/model/imputation';
import { ImputationSummary } from '@core/model/imputation-summary';
import { Product } from '@core/model/product';
import { Project } from '@core/model/project';
import { Technology } from '@core/model/technology';
import { User } from '@core/model/user';

@Injectable({
  providedIn: 'root'
})
export class ModelMapperService {

  mapTechnology(payload: unknown): Technology {
    return Technology.fromObject(payload);
  }

  mapTechnologyList(payload: unknown[]): Technology[] {
    return payload.map((item) => Technology.fromObject(item));
  }

  mapUser(payload: unknown): User {
    return User.fromObject(payload);
  }

  mapUserList(payload: unknown[]): User[] {
    return payload.map((item) => User.fromObject(item));
  }

  mapProduct(payload: unknown): Product {
    return Product.fromObject(payload);
  }

  mapProductList(payload: unknown[]): Product[] {
    return payload.map((item) => Product.fromObject(item));
  }

  mapProject(payload: unknown): Project {
    return Project.fromObject(payload);
  }

  mapProjectList(payload: unknown[]): Project[] {
    return payload.map((item) => Project.fromObject(item));
  }

  mapImputation(payload: unknown): Imputation {
    return Object.assign(new Imputation(), payload);
  }

  mapImputationList(payload: unknown[]): Imputation[] {
    return payload.map((item) => Object.assign(new Imputation(), item));
  }

  mapImputationSummary(payload: unknown): ImputationSummary {
    return ImputationSummary.fromObject(payload);
  }

  mapImputationSummaryList(payload: unknown[]): ImputationSummary[] {
    return payload.map((item) => ImputationSummary.fromObject(item));
  }
}
