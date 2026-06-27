import { Product } from "./product";
import { Status } from "./status";
import { Technology } from "./technology";
import { User } from "./user";

export class Project {
    id!: number;
    name!: string;
    description!: string;
    reference1!: string;
    reference2!: string;
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

    getDuration():number{
      if(this.datePro!=null){
        return Math.ceil((new Date(this.datePro).getTime() - new Date(this.dateDev).getTime()) / (1000 * 60 * 60 * 24));
      }  
      return Math.ceil((new Date().getTime() - new Date(this.dateDev).getTime()) / (1000 * 60 * 60 * 24));
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