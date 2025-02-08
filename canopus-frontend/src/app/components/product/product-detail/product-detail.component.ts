import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';

import { FormBuilder, FormGroup, Validators, ReactiveFormsModule} from '@angular/forms';

import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { ProductService } from '@core/services/product.service';
import { UserService } from '@core/services/user.service';
import { ActivatedRoute, Router } from '@angular/router';
import { Product } from '@core/model/product';
import { map, mergeMap, Observable, startWith } from 'rxjs';
import { User } from '@core/model/user';
import {MatGridListModule} from '@angular/material/grid-list';
import { Technology } from '@core/model/technology';
import { TechnologyService } from '@core/services/technology.service';


@Component({
  selector: 'app-product-detail',
  imports: [CommonModule, MatCardModule, ReactiveFormsModule, MatInputModule, MatAutocompleteModule, MatButtonModule, MatGridListModule],
  templateUrl: './product-detail.component.html',
  styleUrl: './product-detail.component.scss'
})
export class ProductDetailComponent {

  productService = inject(ProductService);
  userService = inject(UserService);
  technologyService = inject(TechnologyService);
  router = inject(Router);
  activateRoute = inject(ActivatedRoute);
  fb = inject(FormBuilder);

  form!: FormGroup;

  product!: Product;

  filteredUsers: Observable<User[]> | undefined;  
  filteredTechnologies: Observable<Technology[]> | undefined;

  error!: string;
  message!: string;
  message2!: string;

  ngOnInit(): void {
    this.activateRoute.params.subscribe(params => {
      this.buildForm();
      let id = params['id']
      if(id){
        this.productService.get(id).subscribe({
          next:(res: Product)=> {          
            let productsTmp = Product.fromObject(res);
            this.form.get('id')?.setValue(productsTmp.id);
            this.form.get('name')?.setValue(productsTmp.name);
            this.form.get('description')?.setValue(productsTmp.description);   
            this.form.get('technology')?.setValue(productsTmp.technology);
            this.form.get('responsible')?.setValue(productsTmp.responsible);
            this.form.get('projects')?.setValue(productsTmp.getCountProjects());
            this.form.get('time')?.setValue(productsTmp.getTime());
            this.form.get('contribuitors')?.setValue(productsTmp.getCountContributors());

          },
          error: (err: any) => console.log(err)
        });
      }else{
        this.product = new Product();
      }
    });

    this.filteredUsers = this.form.get('responsible')?.valueChanges
    .pipe(
      startWith(''),
      map(value => typeof value === 'string' ? value : value.description),
      mergeMap(value => value ? this._filter(value) : this._getAll())
    );    
    
    this.filteredTechnologies = this.form.get('technology')?.valueChanges
    .pipe(
      startWith(''),
      map(value => typeof value === 'string' ? value : value.description),
      mergeMap(value => value ? this._filterTechnology(value) : this._getAllTechnologies())
    );   

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

  private _getAllTechnologies(): Observable<Technology[]> {    
    return this.technologyService.getAll();
  }  

  private _filterTechnology(value: string): Observable<Technology[]> {
    const filterValue = value.toLowerCase();
    return this.technologyService.getSelection(filterValue);
  }    

  displayTechnology(technology: Technology): string {
    return technology && technology.name ? technology.name : '';
  }


  private buildForm(){
    this.form = this.fb.group({
      id:             [''],
      name:           ['', [Validators.required]],
      description:    ['', [Validators.required]],
      technology:     ['', [Validators.required]],
      responsible:    ['', [Validators.required]],
      projects:       [{value: '', disabled: true}, Validators.required],
      contribuitors:  [{value: '', disabled: true}, Validators.required],
      time:           [{value: '', disabled: true}, Validators.required]
    });  
  }  

  update(event: Event): void {
    event.preventDefault();
    if(this.form.valid){
      console.log(this.form);
      this.product = this.form.value;
      console.log(this.product);
      this.productService.update(this.product).subscribe({
        next: (res: any) => {
          this.router.navigateByUrl('/pvt/product');
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
      this.product = this.form.value;
      this.productService.create(this.product).subscribe({
        next: (res: any) => {
          this.router.navigateByUrl('/pvt/product');
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
      this.product = this.form.value;
      this.productService.delete(this.product.id).subscribe({
        next: (res: any) => {
          this.router.navigateByUrl('/pvt/product');
        },
        error: (err: any) => {
          console.log('error');
          console.log(err)
          this.error = 'err.error';
          this.message2 = err.message;
          console.log(this.error);
          console.log(this.message2);
        },
      });
    }
  }


}
