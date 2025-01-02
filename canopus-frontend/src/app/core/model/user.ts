export class User {
    id!: number;
    username!: string;
    password!: string;
    name!: string;
    lastname!: string;
    email!: string;
    roles: string[]=[];
    company!: string;

    public static fromObject(obj: any):User { 
        let userRef: User = new User();
        Object.assign(userRef, obj);
        return userRef;
      }
}