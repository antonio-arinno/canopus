import { Project } from "./project";
import { Technology } from "./technology";
import { User } from "./user";

export interface ProductRequest {
  id?: number;
  name: string;
  description: string;
  technologyId: number;
  responsibleId: number;
  backupId: number;
}

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
