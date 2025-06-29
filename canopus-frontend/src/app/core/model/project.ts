import { Product } from "./product";
import { Status } from "./status";
import { Technology } from "./technology";
import { User } from "./user";

export class Project {
    id!: number;
    name!: string;
    description!: string;
    product!: Product;
    technology!: Technology;
    dateDev!: string;
    datePre!: string;
    datePro!: string;
    responsible!: User;
    time!: number;
    countContributors!: number;
    contributors: Array<User> = [];

    getStatus():Status {
      if(this.datePro!=null){
        return Status.PRODUCTION
      }else{
        if(this.datePre!=null){
          return Status.PRE_PRODUCTION
        }else{
          return Status.DEVELOPMENT
        }
      }
    }

    public static fromObject(obj: any):Project { 
      let projectRef: Project = new Project();
      Object.assign(projectRef, obj);
      return projectRef;
    }    
  }
  /*
      getCountContributors():number {      
        return this.contributors.length;
      }
  */