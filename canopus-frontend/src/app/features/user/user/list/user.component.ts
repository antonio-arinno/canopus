import { Component, inject, OnInit, signal, WritableSignal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { Router } from '@angular/router';
import { User } from '@core/model/user';
import { ModelMapperService } from '@core/model/model-mapper.service';
import { UserService } from '@features/user/data/user.service';

@Component({
  selector: 'app-user',
  imports: [MatTableModule, MatButtonModule, MatCardModule],
  templateUrl: './user.component.html',
  styleUrl: './user.component.scss'
})
export class UserComponent implements OnInit{

  userService = inject(UserService);
  router = inject(Router);

  users: WritableSignal<User[]> = signal([]);
  displayedColumns: string[] = ['name', 'lastname', 'countProducts', 'time'];
//  dataSource = this.users;


  modelMapperService = inject(ModelMapperService);

  ngOnInit(): void {
    this.userService.getAll().subscribe({
      next: (res: User[]) => {
        const usersTmp = this.modelMapperService.mapUserList(res as unknown[]);
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
