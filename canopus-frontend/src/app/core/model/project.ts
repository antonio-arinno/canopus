import { Product } from "./product";
import { Status } from "./status";
import { User } from "./user";

export class Project {
    id!: number;
    name!: string;
    description!: string;
    product!: Product;
    status!: Status;
    responsible!: User;
    contributors: Array<User> = [];
    time!: number;

    getCountContributos():number {      
      return this.contributors.length;
    }

    public static fromObject(obj: any):Project { 
      let projectRef: Project = new Project();
      Object.assign(projectRef, obj);
      return projectRef;
    }    
  }