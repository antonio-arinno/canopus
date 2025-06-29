import { Technology } from "./technology";

export class User {
    id!: number;
    username!: string;
    password!: string;
    name!: string;
    lastname!: string;
    email!: string;
    roles: string[]=[];
    countProducts!: number;
    countProjects!: number;
    time!: number;
    technologies: Array<Technology> = [];
    company!: string;

    public static fromObject(obj: any):User { 
        let userRef: User = new User();
        Object.assign(userRef, obj);
        return userRef;
      }
}