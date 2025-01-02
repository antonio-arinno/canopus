import { Project } from "./project";
import { User } from "./user";

export class Product {
  id!: number;
  name!: string;
  description!: string;
  responsible!: User;
  
  projects: Array<Project> = [];
 
  getTime():number {      
    let time = 0;
    this.projects.forEach((item: Project) => {
      time += item.time;
    });
    return time;
  }

  getCountProjects():number {      
    return this.projects.length;
  }

  getCountContributors(): number {
    let contributors: Array<number> = [];
    this.projects.forEach((project: Project) => {
      project.contributors.forEach((user: User) => {
        if (!contributors.includes(user.id)) {
          contributors.push(user.id);
        }
      })
    });
    return contributors.length;
  }

  public static fromObject(obj: any):Product { 
    let productRef: Product = new Product();
    Object.assign(productRef, obj);
    return productRef;
  }
}