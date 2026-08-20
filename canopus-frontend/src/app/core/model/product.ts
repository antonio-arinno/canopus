import { Project } from "./project";
import { Technology } from "./technology";
import { User } from "./user";

export class Product {
  id!: number;
  name!: string;
  description!: string;
  technology!: Technology;
  responsible!: User;
  backup!: User;
  countProjects!: number;
  countContributors!: number;
  time!: number;
  avgTime!: number;
  avgDuration!: number;
  projects: Array<Project> = [];

  public static fromObject(obj: any):Product { 
    const productRef: Product = new Product();
    Object.assign(productRef, obj);
    productRef.technology = obj?.technology ? Technology.fromSummary(obj.technology) : new Technology();
    productRef.responsible = obj?.responsible ? User.fromObject(obj.responsible) : new User();
    productRef.backup = obj?.backup ? User.fromObject(obj.backup) : new User();
    productRef.projects = [];
    return productRef;
  }
}
/* 
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
*/