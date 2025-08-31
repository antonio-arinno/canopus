import { Component, inject, WritableSignal, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormControl } from '@angular/forms';

import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatGridListModule } from '@angular/material/grid-list';

import { provideNativeDateAdapter } from '@angular/material/core';
import { MatTableModule } from '@angular/material/table';

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
import { ImputationService } from '@core/services/imputation.service';
import { Dto1 } from '@core/model/dto/dto1';

@Component({
  selector: 'app-project-detail',
  imports: [CommonModule, MatCardModule, MatInputModule, MatButtonModule, ReactiveFormsModule, MatAutocompleteModule, MatSelectModule, MatDatepickerModule, CdkDropList, CdkDrag, MatGridListModule, MatTableModule],
  templateUrl: './project-detail.component.html',
  providers: [provideNativeDateAdapter()],
  styleUrl: './project-detail.component.scss'
})
export class ProjectDetailComponent implements OnInit {

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
  imputationService = inject(ImputationService);
  userService = inject(UserService);
  router = inject(Router);
  activateRoute = inject(ActivatedRoute);
  fb = inject(FormBuilder);

  form!: FormGroup;


/*
  form = new FormGroup({
    id: new FormControl(),
    name: new FormControl(''),
    description: new FormControl(''),
    product: new FormControl(),
    status: new FormControl(''),
    date1: new FormControl(''),
    date2: new FormControl({value:'', disabled:true}),
    date3: new FormControl(''), 
    responsible: new FormControl(),
  });  
*/

  prueba = new Date('2025-04-15');

  project!: Project;
  user!: User;
  error!: string;
  message!: string;
  message2!: string;
  dateOk = true;

  users: WritableSignal<User[]> = signal([]);
  contribuitors: WritableSignal<User[]> = signal([]);
  dtos:         WritableSignal<Dto1[]> = signal([]);

  displayedColumns: string[] = ['name', 'time'];

  filteredProducts: Observable<Product[]> | undefined;    
  filteredUsers: Observable<User[]> | undefined;  

//  status: WritableSignal<Status> = signal(Status.STATELESS)

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
          next:(res2: Project)=> {
            let res= Project.fromObject(res2);

           // return Technology.fromObject(technologyTmp);


//product.projects.push(Project.fromObject(project));


            this.form.get('id')?.setValue(res.id);
            this.form.get('name')?.setValue(res.name);
            this.form.get('description')?.setValue(res.description);
            this.form.get('product')?.setValue(res.product);
            this.form.get('status')?.setValue(res.getStatus());
            if(res.dateDev!=null){this.form.get('dateDev')?.setValue(new Date(res.dateDev.replaceAll('-', '/')))}
            if(res.datePre!=null){this.form.get('datePre')?.setValue(new Date(res.datePre.replaceAll('-', '/')))}
            if(res.datePro!=null){this.form.get('datePro')?.setValue(new Date(res.datePro.replaceAll('-', '/')))}
            this.form.get('responsible')?.setValue(res.responsible);
            this.form.get('countContributors')?.setValue(res.countContributors);
            this.form.get('time')?.setValue(res.time);
            console.log(4);
            this.statusChange();
            this.contribuitors.set(res.contributors);
            this.userService.getByTechnology(res.technology.id)
            .subscribe(users => {
              this.users.set(users);
              this.users.update((currentUsers) => currentUsers.filter(
                objeto => !res.contributors.some(objeto2 => objeto2.id == objeto.id)
              ))
            });
            this.imputationService.getByProject(res.id).subscribe({
              next: (res: Dto1[]) => {
                this.dtos.set(res);   
              },
              error: (err: any) => console.log(err),
            });

          },
          error: (err: any) => console.log(err)
        });
      }else{
/*        
        this.userService.getAll()
        .subscribe(users => {
          this.users.set(users);
        })
*/          
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

    this.form.get('dateDev')?.valueChanges.subscribe(()=>{
      this.form.get('datePre')?.enable()
      console.log(1)
      this.statusChange();
    }
    )

    this.form.get('datePre')?.valueChanges.subscribe(() => {
      if(this.form.get('datePre')?.value){
        if(this.form.get('dateDev')?.value > this.form.get('datePre')?.value ){
          this.dateOk = false
        }else{
          this.dateOk = true
          this.form.get('datePro')?.enable() 
        }
      }else{
        this.form.get('datePro')?.setValue(null)
        this.form.get('datePro')?.disable() 
      }
      console.log(2)
      this.statusChange();
    })

    this.form.get('datePro')?.valueChanges.subscribe(() => {
      if(this.form.get('datePre')?.value > this.form.get('datePro')?.value && this.form.get('datePro')?.value){
        this.dateOk = false
      }else{
        this.dateOk = true
      }
      console.log(3)
      this.statusChange();
    })

    this.form.get('product')?.valueChanges.subscribe(() => {
      if(this.form.get('product')?.value.technology){
        this.userService.getByTechnology(this.form.get('product')?.value.technology.id)
        .subscribe(users => {
          this.users.set(users);
          this.users.update((currentUsers) => currentUsers.filter(
            objeto => !this.contribuitors().some(objeto2 => objeto2.id == objeto.id)
          ))
        })
      }
    })

  }  

  private statusChange2(event: Event){
    event.preventDefault();
    let res= Project.fromObject(this.form.value);
    this.form.get('status')?.setValue(res.getStatus());
  }

  private statusChange(){
    const datePro = { datepro: this.form.get('datePro')?.value};
    this.form.patchValue( datePro );
    let project = Project.fromObject(this.form.value);
    this.form.get('status')?.setValue(project.getStatus());
/*
    if(this.form.get('datePro')?.value){
      this.status.set(Status.PRODUCTION)
    }else{
      if(this.form.get('datePre')?.value){
        this.status.set(Status.PRE_PRODUCTION)
      }else{
        if(this.form.get('dateDev')?.value){
          this.status.set(Status.DEVELOPMENT)
        }else{
          this.status.set(Status.STATELESS)
        }  
      }
    }
*/ 
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
      id:           [''],
      name:         ['', [Validators.required]],
      description:  ['', [Validators.required]],
      product:      ['', [Validators.required]],
      status:       [{value:'', disabled:true}], 
      dateDev:      [Date, [Validators.required]],
      datePre:      [{value:'', disabled:true}], 
      datePro:      [{value:'', disabled:true}], 
      responsible:  ['', [Validators.required]],
      countContributors: [{value: '', disabled: true}, Validators.required],
      time:         [{value: '', disabled: true}, Validators.required]
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

