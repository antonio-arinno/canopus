import { Component, inject, OnInit, signal, WritableSignal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { Router } from '@angular/router';
import { User } from '@core/model/user';
import { UserService } from '@core/services/user.service';

@Component({
  selector: 'app-user',
  imports: [MatTableModule, MatButtonModule],
  templateUrl: './user.component.html',
  styleUrl: './user.component.scss'
})
export class UserComponent implements OnInit{

  userService = inject(UserService);
  router = inject(Router);

  users: WritableSignal<User[]> = signal([]);
//  displayedColumns: string[] = ['name', 'lastname', 'countProducts', 'countProjects', 'time'];
  displayedColumns: string[] = ['name', 'lastname'];
  dataSource = this.users;

  ngOnInit(): void {
    this.userService.getAll().subscribe({
      next: (res: User[]) => {
        console.log(res);
        let usersTmp = res.map(function (user){
          return User.fromObject(user);
        });
        this.users.set(usersTmp);
      },
      error: (err: any) => console.log(err),
    });
  }

  edit(id: number):void {
    this.router.navigate(['/pvt/user/detail', id]);
  }

  create(){
    this.router.navigate(['/pvt/user/detail']);  
  }
}
