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

import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-project-detail',
  imports: [CommonModule, MatCardModule, MatInputModule, MatButtonModule, ReactiveFormsModule, MatAutocompleteModule, MatSelectModule, MatDatepickerModule, CdkDropList, CdkDrag, MatGridListModule, MatTableModule],
  templateUrl: './project-detail.component.html',
  providers: [provideNativeDateAdapter(), DatePipe],
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

  project!: Project;
  user!: User;
  error!: string;
  message!: string;
  message2!: string;
  dateOk = true;

  datePipe = inject(DatePipe); // Ahora esto ya no dará error

  users: WritableSignal<User[]> = signal([]);
  contribuitors: WritableSignal<User[]> = signal([]);
  dtos:         WritableSignal<Dto1[]> = signal([]);

  displayedColumns: string[] = ['name', 'time'];

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
          next:(res2: Project)=> {
            let res= Project.fromObject(res2);
            this.form.get('id')?.setValue(res.id);
            this.form.get('name')?.setValue(res.name);
            this.form.get('description')?.setValue(res.description);
            this.form.get('reference1')?.setValue(res.reference1);
            this.form.get('reference2')?.setValue(res.reference2);
            this.form.get('product')?.setValue(res.product);
            this.form.get('status')?.setValue(res.getStatus());
            if(res.dateDev!=null){this.form.get('dateDev')?.setValue(new Date(res.dateDev.replaceAll('-', '/')))}
            if(res.datePre!=null){this.form.get('datePre')?.setValue(new Date(res.datePre.replaceAll('-', '/')))}
            if(res.datePro!=null){this.form.get('datePro')?.setValue(new Date(res.datePro.replaceAll('-', '/')))}
            this.form.get('responsible')?.setValue(res.responsible);
            this.form.get('countContributors')?.setValue(res.countContributors);
            this.form.get('time')?.setValue(res.time);
            this.form.get('duration')?.setValue(res.getDuration());
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
      this.statusChange();
    })

    this.form.get('datePro')?.valueChanges.subscribe(() => {
      if(this.form.get('datePre')?.value > this.form.get('datePro')?.value && this.form.get('datePro')?.value){
        this.dateOk = false
      }else{
        this.dateOk = true
      }
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
    this.form.get('duration')?.setValue(project.getDuration()); 
  }    

  private _getAll(): Observable<Product[]> {
//    return this.productService.getAll();
    return this.productService.getByResponsibleMe();
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
      reference1:   [''],
      reference2:   [''],
      product:      ['', [Validators.required]],
      status:       [{value:'', disabled:true}], 
      dateDev:      [Date, [Validators.required]],
      datePre:      [{value:'', disabled:true}], 
      datePro:      [{value:'', disabled:true}], 
      responsible:  ['', [Validators.required]],
      countContributors: [{value: '', disabled: true}, Validators.required],
      time:         [{value: '', disabled: true}, Validators.required],
      duration:     [{value: '', disabled: true}, Validators.required]
    });  
  }  

  update(event: Event): void {
    event.preventDefault();
    if(this.form.valid){
      // 1. Obtenemos los valores actuales del formulario
      // Usamos getRawValue() para incluir los campos 'disabled' (datePre, datePro)
      const rawForm = this.form.getRawValue();
      // 2. Transformamos las fechas a formato texto "yyyy-MM-dd"
      const formattedProject = {
        ...rawForm,
        dateDev: this.datePipe.transform(rawForm.dateDev, 'yyyy-MM-dd'),
        datePre: this.datePipe.transform(rawForm.datePre, 'yyyy-MM-dd'),
        datePro: this.datePipe.transform(rawForm.datePro, 'yyyy-MM-dd'),
        contributors: this.contribuitors() 
      };
      this.projectService.update(formattedProject).subscribe({
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
/*
      this.project = this.form.value;
      this.project.contributors = this.contribuitors();
      this.projectService.create(this.project).subscribe({
*/
      const rawForm = this.form.getRawValue();
      const formattedProject = {
        ...rawForm,
        dateDev: this.datePipe.transform(rawForm.dateDev, 'yyyy-MM-dd'),
        datePre: this.datePipe.transform(rawForm.datePre, 'yyyy-MM-dd'),
        datePro: this.datePipe.transform(rawForm.datePro, 'yyyy-MM-dd'),
        contributors: this.contribuitors() 
      };
      this.projectService.create(formattedProject).subscribe({
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
          this.error = err.error;
          this.message = err.message;
        },
      });
    }
  }     


}

function value(value: User[]): User[] {
  throw new Error('Function not implemented.');
}

