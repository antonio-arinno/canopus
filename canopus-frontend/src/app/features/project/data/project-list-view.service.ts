import { Injectable } from '@angular/core';
import { Product } from '@core/model/product';
import { Project } from '@core/model/project';
import { Technology } from '@core/model/technology';

@Injectable({
  providedIn: 'root'
})
export class ProjectListViewService {

  buildTechnologyTree(projects: Project[]): Technology[] {
    const technologies: Technology[] = [];

    for (const project of projects) {
      const technology = this.findOrCreateTechnology(technologies, project.technology);
      const product = this.findOrCreateProduct(technology.products, project.product);

      const projectView = this.buildProjectView(project);
      product.projects.push(projectView);
    }

    return technologies;
  }

  private findOrCreateTechnology(technologies: Technology[], sourceTechnology: Technology): Technology {
    let technology = technologies.find(item => item.id === sourceTechnology.id);

    if (!technology) {
      technology = Technology.fromSummary({
        id: sourceTechnology.id,
        name: sourceTechnology.name,
        description: '',
        responsible: sourceTechnology.responsible,
        countProducts: 0,
        countProjects: 0,
        countContributors: 0,
        time: 0
      });

      technologies.push(technology);
    }

    return technology;
  }

  private findOrCreateProduct(products: Product[], sourceProduct: Product): Product {
    let product = products.find(item => item.id === sourceProduct.id);

    if (!product) {
      product = Product.fromObject({
        id: sourceProduct.id,
        name: sourceProduct.name,
        description: '',
        technology: sourceProduct.technology,
        responsible: sourceProduct.responsible,
        backup: sourceProduct.backup,
        countProjects: 0,
        countContributors: 0,
        time: 0,
        avgTime: 0,
        avgDuration: 0,
        projects: []
      });

      products.push(product);
    }

    return product;
  }

  private buildProjectView(project: Project): Project {
    const projectView = new Project();
    Object.assign(projectView, project);
    return Project.fromObject(projectView);
  }
}
