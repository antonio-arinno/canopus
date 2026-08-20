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

  public static fromObject(obj: any): Technology {
    const technologyRef = new Technology();
    Object.assign(technologyRef, obj);
    technologyRef.responsible = obj?.responsible ? User.fromObject(obj.responsible) : new User();
    technologyRef.products = Array.isArray(obj?.products)
      ? obj.products.map((product: Product) => Product.fromObject(product))
      : [];
    return technologyRef;
  }

  public static fromSummary(obj: any): Technology {
    const technologyRef = new Technology();
    technologyRef.id = obj?.id;
    technologyRef.name = obj?.name ?? '';
    technologyRef.description = obj?.description ?? '';
    technologyRef.responsible = obj?.responsible ? User.fromObject(obj.responsible) : new User();
    technologyRef.countProducts = obj?.countProducts ?? 0;
    technologyRef.countProjects = obj?.countProjects ?? 0;
    technologyRef.countContributors = obj?.countContributors ?? 0;
    technologyRef.time = obj?.time ?? 0;
    technologyRef.products = [];
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