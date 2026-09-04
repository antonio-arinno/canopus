import { Component, inject, OnInit, signal, WritableSignal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { Router } from '@angular/router';
import { User } from '@core/model/user';
import { ModelMapperService } from '@core/model/model-mapper.service';
import { UserService } from '@features/user/data/user.service';
import { RequestStateService } from '@core/ui/request-state.service';

@Component({
  selector: 'app-user',
  imports: [MatTableModule, MatButtonModule, MatCardModule, MatPaginatorModule],
  templateUrl: './user.component.html',
  styleUrl: './user.component.scss'
})
export class UserComponent implements OnInit{

  userService = inject(UserService);
  requestStateService = inject(RequestStateService);
  router = inject(Router);

  users: WritableSignal<User[]> = signal([]);
  displayedColumns: string[] = ['name', 'lastname', 'countProducts', 'time'];
  pageIndex = 0;
  pageSize = 10;
  totalUsers = 0;
  readonly isLoading = this.requestStateService.isLoading;
  readonly errorMessage = this.requestStateService.errorMessage;


  modelMapperService = inject(ModelMapperService);

  ngOnInit(): void {
    this.loadPage();
  }

  loadPage(): void {
    this.requestStateService.start();
    this.userService.getPage(this.pageIndex, this.pageSize).subscribe({
      next: (response) => {
        const usersTmp = this.modelMapperService.mapUserList(response.content as unknown[]);
        this.users.set(usersTmp);
        this.totalUsers = response.totalElements;
        this.pageIndex = response.number;
        this.pageSize = response.size;
        this.requestStateService.finish();
      },
      error: (err: unknown) => this.requestStateService.setError(err),
    });
  }

  changePage(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadPage();
  }

  edit(id: number):void {
    this.router.navigate(['/pvt/user/detail', id]);
  }

  create(){
    this.router.navigate(['/pvt/user/detail']);  
  }
}
