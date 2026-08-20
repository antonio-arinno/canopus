import { Injectable } from '@angular/core';
import { Product } from '@core/model/product';
import { Technology } from '@core/model/technology';
import { User } from '@core/model/user';

@Injectable({
  providedIn: 'root'
})
export class ProductListViewService {

  buildTechnologyTree(products: Product[]): Technology[] {
    const technologies: Technology[] = [];

    for (const product of products) {
      const technology = product.technology;
      const existingTechnology = technologies.find((item) => item.id === technology.id);

      if (existingTechnology) {
        existingTechnology.products.push(product);
      } else {
        technologies.push(
          Technology.fromSummary({
            id: technology.id,
            name: technology.name,
            description: '',
            responsible: technology.responsible,
            countProducts: 0,
            countProjects: 0,
            countContributors: 0,
            time: 0,
            products: []
          })
        );
        technologies[technologies.length - 1].products.push(product);
      }
    }

    return technologies;
  }
}
