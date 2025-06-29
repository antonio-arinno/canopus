import { CommonModule } from '@angular/common';
import { Component, inject, WritableSignal, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';

import {
  CdkDragDrop,
  moveItemInArray,
  transferArrayItem,
  CdkDrag,
  CdkDropList,
} from '@angular/cdk/drag-drop';

import { User } from '@core/model/user';
import { UserService } from '@core/services/user.service';
import { TechnologyService } from '@core/services/technology.service';
import { Technology } from '@core/model/technology';
import { ProductService } from '@core/services/product.service';
import { Product } from '@core/model/product';

@Component({
  selector: 'app-user-detail',
  imports: [CommonModule, MatCardModule, ReactiveFormsModule, MatInputModule, MatAutocompleteModule, MatButtonModule, CdkDropList, CdkDrag, MatGridListModule, MatTableModule],
  templateUrl: './user-detail.component.html',
  styleUrl: './user-detail.component.scss'
})
export class UserDetailComponent {

  drop(event: CdkDragDrop<any[]>) {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex,
      );
    }
  } 

  userService = inject(UserService);
  technologyService = inject(TechnologyService);
  productService = inject(ProductService);
  router = inject(Router);
  activateRoute = inject(ActivatedRoute);
  fb = inject(FormBuilder);
  form!: FormGroup;
  user!: User;

  error!: string;
  message!: string;
  message2!: string;

  all_technologies: WritableSignal<Technology[]> = signal([]);
  my_technologies:  WritableSignal<Technology[]> = signal([]);
  displayedColumns: string[] = ['product', 'technology', 'responsible', 'countProjects', 'countContributors', 'time'];
  products:         WritableSignal<Product[]> = signal([]);


  ngOnInit(): void {
    this.activateRoute.params.subscribe(params => {
      this.buildForm();
      let id = params['id']
      if(id){
        this.userService.get(id).subscribe({
          next:(res: User)=> {

//            let kk = JSON.parse(sessionStorage.getItem('user')!) as User;
            this.form.get('id')?.setValue(res.id);
            this.form.get('username')?.setValue(res.username);
            this.form.get('password')?.setValue(res.password);
            this.form.get('name')?.setValue(res.name);
            this.form.get('lastname')?.setValue(res.lastname);
            this.form.get('email')?.setValue(res.email);
            this.form.get('countProducts')?.setValue(res.countProducts);
//            this.form.get('countProjects')?.setValue(res.countProjects);
            this.form.get('time')?.setValue(res.time);
            this.my_technologies.set(res.technologies);
            this.technologyService.getAll()
            .subscribe(all_technologies => {
              this.all_technologies.set(all_technologies);
              this.all_technologies.update((currentUsers) => currentUsers.filter(
                objeto => !res.technologies.some(objeto2 => objeto2.id == objeto.id)
              ))
            })

            this.productService.getByContributor(res.id).subscribe({
              next: (res: Product[]) => {
                this.products.set(res);   
              },
              error: (err: any) => console.log(err),
            });




          },
          error: (err: any) => console.log(err)
        });
      }else{
        this.technologyService.getAll()
        .subscribe(all_technologies => {
          this.all_technologies.set(all_technologies);
        })
        this.user = new User();
      }
    });
  }

  private buildForm(){
    this.form = this.fb.group({
      id:             [''],
      username:       [''],
      password:       [''],
      name:           ['', [Validators.required]],
      lastname:       ['', [Validators.required]],
      email:          ['', [Validators.required]],
      countProducts:  [{value: '', disabled: true}, Validators.required],
      countProjects:  [{value: '', disabled: true}, Validators.required],
      time:           [{value: '', disabled: true}, Validators.required]
    });  
  }  

  update(event: Event): void {
    event.preventDefault();
    if(this.form.valid){
      this.user = this.form.value;
      this.user.technologies = this.my_technologies();
      this.userService.update(this.user).subscribe({
        next: (res: any) => {
          this.router.navigateByUrl('/pvt/user');
        },
        error: (err: any) => {
          this.error = err.error.error;
          this.message = err.error.message;
          this.message2 = err.message;
        },
      });
    }
  }     

  create(event: Event): void {
    event.preventDefault();
    if(this.form.valid){
      this.user = this.form.value;
      this.userService.create(this.user).subscribe({
        next: (res: any) => {
          this.router.navigateByUrl('/pvt/user');
        },
        error: (err: any) => {
          this.error = err.error.error;
          this.message = err.error.message;
          this.message2 = err.message;
        },
      });
    }    
  } 

  delete(event: Event): void {
    event.preventDefault();
    if(this.form.valid){
      this.user = this.form.value;
      this.userService.delete(this.user.id).subscribe({
        next: (res: any) => {
          this.router.navigateByUrl('/pvt/user');
        },
        error: (err: any) => {
          this.error = err.error.error;
          this.message = err.error.message;
          this.message2 = err.message;
        },
      });
    }
  }


} 
