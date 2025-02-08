import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';

import { FormBuilder, FormGroup, Validators, ReactiveFormsModule} from '@angular/forms';

import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { UserService } from '@core/services/user.service';
import { ActivatedRoute, Router } from '@angular/router';
import { Product } from '@core/model/product';
import { map, mergeMap, Observable, startWith } from 'rxjs';
import { User } from '@core/model/user';
import {MatGridListModule} from '@angular/material/grid-list';
import { TechnologyService } from '@core/services/technology.service';
import { Technology } from '@core/model/technology';

@Component({
  selector: 'app-technology-detail',
  imports: [CommonModule, MatCardModule, ReactiveFormsModule, MatInputModule, MatAutocompleteModule, MatButtonModule, MatGridListModule],
  templateUrl: './technology-detail.component.html',
  styleUrl: './technology-detail.component.scss'
})
export class TechnologyDetailComponent {

  technologyService = inject(TechnologyService);
  userService = inject(UserService);
  router = inject(Router);
  activateRoute = inject(ActivatedRoute);
  fb = inject(FormBuilder);

  form!: FormGroup;

  technology!: Technology;

  filteredUsers: Observable<User[]> | undefined;  

  error!: string;
  message!: string;
  message2!: string;  

  ngOnInit(): void {
    this.activateRoute.params.subscribe(params => {
      this.buildForm();
      let id = params['id']
      if(id){
        this.technologyService.get(id).subscribe({
          next:(res: Technology)=> {          
            let technologiesTmp = Technology.fromObject(res);
            this.form.get('id')?.setValue(technologiesTmp.id);
            this.form.get('name')?.setValue(technologiesTmp.name);
            this.form.get('description')?.setValue(technologiesTmp.description);    
            this.form.get('responsible')?.setValue(technologiesTmp.responsible);
            this.form.get('products')?.setValue(technologiesTmp.getCountProducts());
            this.form.get('projects')?.setValue(technologiesTmp.getCountProjects());
            this.form.get('time')?.setValue(technologiesTmp.getTime());
            this.form.get('contribuitors')?.setValue(technologiesTmp.getCountContributors());

          },
          error: (err: any) => console.log(err)
        });
      }else{
        this.technology = new Technology();
      }
    });

    this.filteredUsers = this.form.get('responsible')?.valueChanges
    .pipe(
      startWith(''),
      map(value => typeof value === 'string' ? value : value.description),
      mergeMap(value => value ? this._filter(value) : this._getAll())
    );     
  }

  private buildForm(){
    this.form = this.fb.group({
      id: [''],
      name: ['', [Validators.required]],
      description: ['', [Validators.required]],
      responsible: ['', [Validators.required]],
      products: [{value: '', disabled: true}, Validators.required],
      projects: [{value: '', disabled: true}, Validators.required],
      contribuitors: [{value: '', disabled: true}, Validators.required],
      time: [{value: '', disabled: true}, Validators.required]
    });  
  }  

  private _getAll(): Observable<User[]> {    
    return this.userService.getAll();
  }  

  private _filter(value: string): Observable<User[]> {
    const filterValue = value.toLowerCase();
    return this.userService.getSelection(filterValue);
  }    

  displayFn(user: User): string {
    return user && user.name ? user.name : '';
  }  
 
  create(event: Event): void {
    event.preventDefault();
    if(this.form.valid){
      this.technology = this.form.value;
      this.technologyService.create(this.technology).subscribe({
        next: (res: any) => {
          this.router.navigateByUrl('/pvt/technology');
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
      this.technology = this.form.value;
      this.technologyService.delete(this.technology.id).subscribe({
        next: (res: any) => {
          this.router.navigateByUrl('/pvt/technology');
        },
        error: (err: any) => {
          this.error = err.error.error;
          this.message = err.error.message;
          this.message2 = err.message;
        },
      });
    }
  }

  update(event: Event): void {
    event.preventDefault();
    if(this.form.valid){
      this.technology = this.form.value;
      this.technologyService.update(this.technology).subscribe({
        next: (res: any) => {
          this.router.navigateByUrl('/pvt/technology');
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
