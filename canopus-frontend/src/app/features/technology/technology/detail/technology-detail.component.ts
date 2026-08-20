import { Component, inject, WritableSignal, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

import { FormBuilder, FormGroup, Validators, ReactiveFormsModule} from '@angular/forms';

import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { UserService } from '@features/user/data/user.service';
import { ActivatedRoute, Router } from '@angular/router';
import { Product } from '@core/model/product';
import { map, mergeMap, Observable, startWith } from 'rxjs';
import { User } from '@core/model/user';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatTableModule } from '@angular/material/table';
import { TechnologyService } from '@features/technology/data/technology.service';
import { ProductService } from '@features/product/data/product.service';
import { Technology } from '@core/model/technology';
import { ModelMapperService } from '@core/model/model-mapper.service';
import { FormStateService } from '@core/ui/form-state.service';
import { FormValidationService } from '@core/ui/form-validation.service';
import { RequestStateService } from '@core/ui/request-state.service';

@Component({
  selector: 'app-technology-detail',
  imports: [CommonModule, MatCardModule, ReactiveFormsModule, MatInputModule, MatAutocompleteModule, MatButtonModule, MatGridListModule, MatTableModule],
  templateUrl: './technology-detail.component.html',
  styleUrl: './technology-detail.component.scss'
})
export class TechnologyDetailComponent {

  technologyService = inject(TechnologyService);
  productService = inject(ProductService);
  userService = inject(UserService);
  modelMapperService = inject(ModelMapperService);
  formStateService = inject(FormStateService);
  formValidationService = inject(FormValidationService);
  requestStateService = inject(RequestStateService);
  router = inject(Router);
  activateRoute = inject(ActivatedRoute);
  fb = inject(FormBuilder);

  products:         WritableSignal<Product[]> = signal([]);
  displayedColumns: string[] = ['product', 'responsible', 'backup', 'time'];
  form!: FormGroup;

  technology!: Technology;
  user!: User;

  filteredUsers: Observable<User[]> | undefined;  

  readonly isSubmitting = this.formStateService.isSubmitting;
  readonly successMessage = this.formStateService.successMessage;
  readonly errorMessage = this.formStateService.errorMessage;
  readonly isLoading = this.requestStateService.isLoading;

  ngOnInit(): void {
    this.requestStateService.start();
    this.activateRoute.params.subscribe(params => {
      this.buildForm();
      let id = params['id']
      if(id){
        this.technologyService.get(id).subscribe({
          next:(res: Technology)=> {
            const technologiesTmp = this.modelMapperService.mapTechnology(res);
            this.form.get('id')?.setValue(technologiesTmp.id);
            this.form.get('name')?.setValue(technologiesTmp.name);
            this.form.get('description')?.setValue(technologiesTmp.description);
            this.form.get('responsible')?.setValue(technologiesTmp.responsible);
            this.form.get('products')?.setValue(technologiesTmp.countProducts);
            this.form.get('projects')?.setValue(technologiesTmp.countProjects);
            this.form.get('contribuitors')?.setValue(technologiesTmp.countContributors);
            this.form.get('time')?.setValue(technologiesTmp.time);

            this.productService.getByTechnology(res.id).subscribe({
              next: (res: Product[]) => {
                this.products.set(this.modelMapperService.mapProductList(res as unknown[]));
                this.requestStateService.finish();
              },
              error: (err: any) => this.requestStateService.setError(err),
            });

          },
          error: (err: any) => this.requestStateService.setError(err)
        });
      }else{
        this.technology = new Technology();
        this.requestStateService.finish();
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

  getValidationMessage(controlName: string): string | null {
    return this.formValidationService.getErrorMessage(this.form.get(controlName));
  }
 
  create(event: Event): void {
    event.preventDefault();
    if(this.form.valid){
      this.formStateService.startSubmit();
      const payload = this.buildTechnologyPayload();
      if (!payload) {
        this.formStateService.setError('Debes seleccionar Responsable desde la lista.');
        return;
      }
      this.technology = Technology.fromObject(payload);
      this.technologyService.create(this.technology).subscribe({
        next: (res: any) => {
          this.formStateService.setSuccess('Tecnología creada con éxito.');
          this.router.navigateByUrl('/pvt/technology');
        },
        error: (err: any) => {
          const message = this.extractErrorMessage(err);
          this.formStateService.setError(message);
        },
      });
    }    
  } 

  delete(event: Event): void {
    event.preventDefault();
    if(this.form.valid){
      const payload = this.form.getRawValue();
      this.technology = Technology.fromObject(payload);
      this.technologyService.delete(this.technology.id).subscribe({
        next: (res: any) => {
          this.router.navigateByUrl('/pvt/technology');
        },
        error: (err: any) => {
          const message = this.extractErrorMessage(err);
          this.formStateService.setError(message);
        },
      });
    }
  }

  update(event: Event): void {
    event.preventDefault();
    if(this.form.valid){
      this.formStateService.startSubmit();
      const payload = this.buildTechnologyPayload();
      if (!payload) {
        this.formStateService.setError('Debes seleccionar Responsable desde la lista.');
        return;
      }
      this.technology = Technology.fromObject(payload);
      this.technologyService.update(this.technology).subscribe({
        next: (res: any) => {
          this.formStateService.setSuccess('Tecnología actualizada con éxito.');
          this.router.navigateByUrl('/pvt/technology');
        },
        error: (err: any) => {
          const message = this.extractErrorMessage(err);
          this.formStateService.setError(message);
        },
      });
    }
  } 

  private buildTechnologyPayload(): Partial<Technology> | null {
    const raw = this.form.getRawValue();
    const responsibleId = this.extractEntityId(raw.responsible);

    if (!responsibleId) {
      return null;
    }

    return {
      id: raw.id,
      name: raw.name,
      description: raw.description,
      responsible: { id: responsibleId } as User,
    };
  }

  private extractEntityId(value: unknown): number | null {
    if (typeof value === 'object' && value !== null && 'id' in value) {
      const id = (value as { id?: unknown }).id;
      if (typeof id === 'number' && !Number.isNaN(id)) {
        return id;
      }
    }

    return null;
  }

  private extractErrorMessage(err: any): string {
    const backendError = err?.error;

    if (typeof backendError === 'string' && backendError.trim().length > 0) {
      return backendError;
    }

    if (backendError?.message) {
      return backendError.message;
    }

    if (backendError?.detail) {
      return backendError.detail;
    }

    if (backendError?.title) {
      return backendError.title;
    }

    if (Array.isArray(backendError?.errors) && backendError.errors.length > 0) {
      const firstError = backendError.errors[0];
      if (typeof firstError === 'string') {
        return firstError;
      }
      if (firstError?.message) {
        return firstError.message;
      }
    }

    if (backendError?.errors && typeof backendError.errors === 'object') {
      const firstErrorList = Object.values(backendError.errors).find(
        (entry) => Array.isArray(entry) && entry.length > 0
      ) as string[] | undefined;

      if (firstErrorList?.[0]) {
        return firstErrorList[0];
      }
    }

    return err?.message ?? 'No se pudo completar la solicitud.';
  }
 
}
