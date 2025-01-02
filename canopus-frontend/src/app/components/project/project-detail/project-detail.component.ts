import { Component, inject, WritableSignal, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormControl } from '@angular/forms';

import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatSelectModule } from '@angular/material/select';

import {
  CdkDragDrop,
  moveItemInArray,
  transferArrayItem,
  CdkDrag,
  CdkDropList,
} from '@angular/cdk/drag-drop';

import { Observable, concat, map, mergeMap, startWith } from 'rxjs';

import { ProjectService } from '@core/services/project.service';
import { UserService } from '@core/services/user.service';
import { Project } from '@core/model/project';
import { Product } from '@core/model/product';
import { User } from  '@core/model/user';
import { ProductService } from '@core/services/product.service';
import { Status } from '@core/model/status';

@Component({
  selector: 'app-project-detail',
  imports: [CommonModule, MatCardModule, MatInputModule, MatButtonModule, ReactiveFormsModule, MatAutocompleteModule, MatSelectModule, CdkDropList, CdkDrag],
  templateUrl: './project-detail.component.html',
  styleUrl: './project-detail.component.scss'
})
export class ProjectDetailComponent {

  
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
  projectService = inject(ProjectService);
  productService = inject(ProductService);
  userService = inject(UserService);
  router = inject(Router);
  activateRoute = inject(ActivatedRoute);
  fb = inject(FormBuilder);

  form!: FormGroup;
  project!: Project;
  user!: User;
  error!: string;
  message!: string;
  message2!: string;

  users: WritableSignal<User[]> = signal([]);
  contribuitors: WritableSignal<User[]> = signal([]);

  filteredProducts: Observable<Product[]> | undefined;    
  filteredUsers: Observable<User[]> | undefined;  

  public keys = Object.keys;
  public userRoles = Status;

  public getkeys(elementor: typeof Status){
    return this.keys(elementor).map(key => key as keyof typeof elementor);
  }  

  ngOnInit(): void {
    this.activateRoute.params.subscribe(params => {
      this.buildForm();
      let id = params['id']
      if(id){
        this.projectService.get(id).subscribe({
          next:(res: Project)=> {
            this.form.get('id')?.setValue(res.id);
            this.form.get('name')?.setValue(res.name);
            this.form.get('description')?.setValue(res.description);
            this.form.get('product')?.setValue(res.product);
            this.form.get('status')?.setValue(res.status);
            this.form.get('responsible')?.setValue(res.responsible);
            this.contribuitors.set(res.contributors);
            this.userService.getAll()
            .subscribe(users => {
              this.users.set(users);
              this.users.update((currentUsers) => currentUsers.filter(
                objeto => !res.contributors.some(objeto2 => objeto2.id == objeto.id)
              ))
            })
          },
          error: (err: any) => console.log(err)
        });
      }else{
        this.userService.getAll()
        .subscribe(users => {
          this.users.set(users);
        })
        this.project = new Project();
      }
    });

    this.filteredProducts = this.form.get('product')?.valueChanges
      .pipe(
        startWith(''),
        map(value => typeof value === 'string' ? value : value.description),
        mergeMap(value => value ? this._filter(value) : this._getAll())
      );

        
    this.filteredUsers = this.form.get('responsible')?.valueChanges
    .pipe(
      startWith(''),
      map(value => typeof value === 'string' ? value : value.name),
      mergeMap(value => value ? this._userFilter(value) : this._getUserAll())
    );            
  }  


  private _getAll(): Observable<Product[]> {
    return this.productService.getAll();
  }  

  private _filter(value: string): Observable<Product[]> {
    const filterValue = value.toLowerCase();
    return this.productService.getSelection(filterValue);
  }  

  displayFn(product: Product): string {
    return product && product.name ? product.name : '';
  }

  private _getUserAll(): Observable<User[]> {    
    return this.userService.getAll();
  }  

  private _userFilter(value: string): Observable<User[]> {
    const filterValue = value.toLowerCase();
    return this.userService.getSelection(filterValue);
  }    

  displayUserFn(user: User): string {
    return user && user.name ? user.name : '';
  }  

  private buildForm(){
    this.form = this.fb.group({
      id: [''],
      name: ['', [Validators.required]],
      description: ['', [Validators.required]],
      product: ['', [Validators.required]],
      status: ['',  [Validators.required]],
      responsible: ['', [Validators.required]]
    });  
  }  

  update(event: Event): void {
    event.preventDefault();
    if(this.form.valid){
      this.project = this.form.value;
      this.project.contributors = this.contribuitors();
      this.projectService.update(this.project).subscribe({
        next: (res: any) => {
          this.router.navigateByUrl('/pvt/project');
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
      this.project = this.form.value;
      this.project.contributors = this.contribuitors();
      this.projectService.create(this.project).subscribe({
        next: (res: any) => {
          this.router.navigateByUrl('/pvt/project');
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
      this.project = this.form.value;
      this.projectService.delete(this.project.id).subscribe({
        next: (res: any) => {
          this.router.navigateByUrl('/pvt/project');
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

function value(value: User[]): User[] {
  throw new Error('Function not implemented.');
}

