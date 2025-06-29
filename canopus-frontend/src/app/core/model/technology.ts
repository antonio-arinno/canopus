import { User } from "./user";
import { Product } from "./product"
import { Project } from "./project";

export class Technology {
  id!: number;
  name!: string;
  description!: string;
  responsible!: User;
  countProducts!: number;
  countProjects!: number;
  countContributors!: number;
  time!: number;
  products: Array<Product> = [];
  
  public static fromObject(obj: any):Technology { 
    let technologyRef: Technology = new Technology();
    Object.assign(technologyRef, obj);
    return technologyRef;
  }
}  



/*
  getCountProducts():number {      
    return this.products.length;
  }

  getCountProjects():number {      
    let projects = 0;
    this.products.forEach((product: Product) => {
      projects += product.projects.length;
    });
    return projects;
  }

  getCountContributors(): number {
    let contributors: Array<number> = [];
    this.products.forEach((product: Product) => {
      product.projects.forEach((project: Project) => {
        project.contributors.forEach((user: User) => {
          if (!contributors.includes(user.id)) {
            contributors.push(user.id);
          }
        })
      });
    });  
    return contributors.length;
  }  

  getTime():number {      
     let time = 0;
     this.products.forEach((product: Product) => {
        product.projects.forEach((project: Project) => {
          time += project.time;
        });
     });
     return time;
   }
*/