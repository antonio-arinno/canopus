import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';

import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormControl } from '@angular/forms';

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

@Component({
  selector: 'app-product-detail',
  imports: [CommonModule, MatCardModule, ReactiveFormsModule, MatInputModule, MatAutocompleteModule, MatButtonModule],
  templateUrl: './product-detail.component.html',
  styleUrl: './product-detail.component.scss'
})
export class ProductDetailComponent {

  productService = inject(ProductService);
  userService = inject(UserService);
  router = inject(Router);
  activateRoute = inject(ActivatedRoute);
  fb = inject(FormBuilder);

  form!: FormGroup;

  product!: Product;

  filteredUsers: Observable<User[]> | undefined;  

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
            this.form.get('id')?.setValue(res.id);
            this.form.get('name')?.setValue(res.name);
            this.form.get('description')?.setValue(res.description);    
            this.form.get('responsible')?.setValue(res.responsible);
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

  private buildForm(){
    this.form = this.fb.group({
      id: [''],
      name: ['', [Validators.required]],
      description: ['', [Validators.required]],
      responsible: ['', [Validators.required]]
    });  
  }  

  update(event: Event): void {
    event.preventDefault();
    if(this.form.valid){
      this.product = this.form.value;
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
          this.error = err.error.error;
          this.message = err.error.message;
          this.message2 = err.message;
        },
      });
    }
  }


}
