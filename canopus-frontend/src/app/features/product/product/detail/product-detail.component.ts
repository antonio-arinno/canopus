import { Component, inject, signal, WritableSignal } from '@angular/core';
import { CommonModule } from '@angular/common';

import { FormBuilder, FormGroup, Validators, ReactiveFormsModule} from '@angular/forms';

import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { ProductService } from '@features/product/data/product.service';
import { UserService } from '@features/user/data/user.service';
import { ActivatedRoute, Router } from '@angular/router';
import { Product } from '@core/model/product';
import { map, mergeMap, Observable, startWith } from 'rxjs';
import { User } from '@core/model/user';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatTableModule } from '@angular/material/table';
import { Technology } from '@core/model/technology';
import { TechnologyService } from '@features/technology/data/technology.service';
import { ImputationService } from '@features/imputation/data/imputation.service';
import { ProjectService } from '@features/project/data/project.service';
import { Project } from '@core/model/project';
import { ModelMapperService } from '@core/model/model-mapper.service';
import { FormStateService } from '@core/ui/form-state.service';
import { FormValidationService } from '@core/ui/form-validation.service';
import { RequestStateService } from '@core/ui/request-state.service';

@Component({
  selector: 'app-product-detail',
  imports: [CommonModule, MatCardModule, ReactiveFormsModule, MatInputModule, MatAutocompleteModule, MatButtonModule, MatGridListModule, MatTableModule],
  templateUrl: './product-detail.component.html',
  styleUrl: './product-detail.component.scss'
})
export class ProductDetailComponent {

  productService = inject(ProductService);
  projectService = inject(ProjectService);
//  imputationService = inject(ImputationService);
  userService = inject(UserService);
  technologyService = inject(TechnologyService);
  modelMapperService = inject(ModelMapperService);
  formStateService = inject(FormStateService);
  formValidationService = inject(FormValidationService);
  requestStateService = inject(RequestStateService);
  router = inject(Router);
  activateRoute = inject(ActivatedRoute);
  fb = inject(FormBuilder);
  projects2: Project[] = [];
  projects:         WritableSignal<Project[]> = signal([]);
  form!: FormGroup;

  product!: Product;

  filteredUsers: Observable<User[]> | undefined;  
  filteredTechnologies: Observable<Technology[]> | undefined;

  readonly isSubmitting = this.formStateService.isSubmitting;
  readonly successMessage = this.formStateService.successMessage;
  readonly errorMessage = this.formStateService.errorMessage;
  readonly isLoading = this.requestStateService.isLoading;
  displayedColumns: string[] = ['projectName', 'status', 'countContributors', 'time'];


  ngOnInit(): void {
    this.requestStateService.start();
    this.activateRoute.params.subscribe(params => {
      this.buildForm();
      let id = params['id']
      if(id){
        this.productService.get(id).subscribe({
          next:(res: Product)=> {
            const productsTmp = this.modelMapperService.mapProduct(res);
            this.form.get('id')?.setValue(productsTmp.id);
            this.form.get('name')?.setValue(productsTmp.name);
            this.form.get('description')?.setValue(productsTmp.description);
            this.form.get('technology')?.setValue(productsTmp.technology);
            this.form.get('responsible')?.setValue(productsTmp.responsible);
            this.form.get('backup')?.setValue(productsTmp.backup);
            this.form.get('projects')?.setValue(productsTmp.countProjects);
            this.form.get('contribuitors')?.setValue(productsTmp.countContributors);
            this.form.get('time')?.setValue(productsTmp.time);
            this.form.get('avgTime')?.setValue(productsTmp.avgTime);
            this.form.get('avgDuration')?.setValue(productsTmp.avgDuration);

            this.projectService.getByProduct(res.id).subscribe({
              next: (res: Project[]) => {
                const mappedProjects = this.modelMapperService.mapProjectList(res as unknown[]);
                this.projects2 = mappedProjects;
                this.projects.set(this.projects2);
                this.requestStateService.finish();
              },
              error: (err: any) => this.requestStateService.setError(err),
            });
          },
          error: (err: any) => this.requestStateService.setError(err)
        });
      }else{
        this.product = new Product();
        this.requestStateService.finish();
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

  getValidationMessage(controlName: string): string | null {
    return this.formValidationService.getErrorMessage(this.form.get(controlName));
  }


  private buildForm(){
    this.form = this.fb.group({
      id:             [''],
      name:           ['', [Validators.required]],
      description:    ['', [Validators.required]],
      technology:     ['', [Validators.required]],
      responsible:    ['', [Validators.required]],
      backup:         ['', [Validators.required]],
      projects:       [{value: '', disabled: true}, Validators.required],
      contribuitors:  [{value: '', disabled: true}, Validators.required],
      time:           [{value: '', disabled: true}, Validators.required],
      avgTime:        [{value: '', disabled: true}, Validators.required],
      avgDuration:    [{value: '', disabled: true}, Validators.required]
    });  
  }  

  update(event: Event): void {
    event.preventDefault();
    if(this.form.valid){
      this.formStateService.startSubmit();
      const payload = this.buildProductPayload();
      if (!payload) {
        this.formStateService.setError('Debes seleccionar Tecnologia, Responsable y Backup desde la lista.');
        return;
      }
      this.product = Product.fromObject(payload);
      this.productService.update(this.product).subscribe({
        next: () => {
          this.formStateService.setSuccess('Producto actualizado con éxito.');
          this.router.navigateByUrl('/pvt/product');
        },
        error: (err: any) => {
          const message = this.extractErrorMessage(err);
          this.formStateService.setError(message);
        },
      });
    }
  }     

  create(event: Event): void {
    event.preventDefault();
    if(this.form.valid){
      this.formStateService.startSubmit();
      const payload = this.buildProductPayload();
      if (!payload) {
        this.formStateService.setError('Debes seleccionar Tecnologia, Responsable y Backup desde la lista.');
        return;
      }
      this.product = Product.fromObject(payload);
      this.productService.create(this.product).subscribe({
        next: () => {
          this.formStateService.setSuccess('Producto creado con éxito.');
          this.router.navigateByUrl('/pvt/product');
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
      this.formStateService.startSubmit();
      const payload = this.form.getRawValue();
      this.product = Product.fromObject(payload);
      this.productService.delete(this.product.id).subscribe({
        next: (res: any) => {
          this.router.navigateByUrl('/pvt/product');
        },
        error: (err: any) => {
          const message = this.extractErrorMessage(err);
          this.formStateService.setError(message);
        },
      });
    }
  }

  private buildProductPayload(): Partial<Product> | null {
    const raw = this.form.getRawValue();
    const technologyId = this.extractEntityId(raw.technology);
    const responsibleId = this.extractEntityId(raw.responsible);
    const backupId = this.extractEntityId(raw.backup);

    if (!technologyId || !responsibleId || !backupId) {
      return null;
    }

    return {
      id: raw.id,
      name: raw.name,
      description: raw.description,
      technology: { id: technologyId } as Technology,
      responsible: { id: responsibleId } as User,
      backup: { id: backupId } as User,
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

    if (backendError?.error && typeof backendError.error === 'string') {
      return backendError.error;
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
