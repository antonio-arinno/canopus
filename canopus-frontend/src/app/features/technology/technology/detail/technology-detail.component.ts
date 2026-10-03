import { Component, inject, WritableSignal, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { map, mergeMap, Observable, startWith } from 'rxjs';

import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatTableModule } from '@angular/material/table';

import { UserService } from '@features/user/data/user.service';
import { ProductService } from '@features/product/data/product.service';
import { TechnologyRequest, TechnologyService } from '@features/technology/data/technology.service';
import { Product } from '@core/model/product';
import { User } from '@core/model/user';
import { Technology } from '@core/model/technology';
import { ModelMapperService } from '@core/model/model-mapper.service';
import { FormStateService } from '@core/ui/form-state.service';
import { FormValidationService } from '@core/ui/form-validation.service';
import { RequestStateService } from '@core/ui/request-state.service';

@Component({
  selector: 'app-technology-detail',
  imports: [
    CommonModule, 
    MatCardModule, 
    ReactiveFormsModule, 
    MatInputModule, 
    MatAutocompleteModule, 
    MatButtonModule, 
    MatGridListModule, 
    MatTableModule
  ],
  templateUrl: './technology-detail.component.html',
  styleUrl: './technology-detail.component.scss'
})
export class TechnologyDetailComponent implements OnInit {

  // Inyección funcional encapsulada con private readonly
  private readonly technologyService = inject(TechnologyService);
  private readonly productService = inject(ProductService);
  private readonly userService = inject(UserService);
  private readonly modelMapperService = inject(ModelMapperService);
  private readonly formStateService = inject(FormStateService);
  private readonly formValidationService = inject(FormValidationService);
  private readonly requestStateService = inject(RequestStateService);
  private readonly router = inject(Router);
  private readonly activateRoute = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);

  products: WritableSignal<Product[]> = signal([]);
  displayedColumns: string[] = ['product', 'responsible', 'backup', 'time'];
  form!: FormGroup;

  technology!: Technology;
  user!: User;
  filteredUsers: Observable<User[]> | undefined;  

  // Atributos reactivos compartidos con el HTML
  readonly isSubmitting = this.formStateService.isSubmitting;
  readonly isLoading = this.requestStateService.isLoading;

  ngOnInit(): void {
    this.formStateService.clear();
    this.requestStateService.start();
    this.buildForm();

    this.activateRoute.params.subscribe(params => {
      const id = params['id'];
      if (id) {
        this.technologyService.get(id).subscribe({
          next: (res: Technology) => {
            const technologiesTmp = this.modelMapperService.mapTechnology(res);
            this.form.patchValue({
              id: technologiesTmp.id,
              name: technologiesTmp.name,
              description: technologiesTmp.description,
              responsible: technologiesTmp.responsible,
              products: technologiesTmp.countProducts,
              projects: technologiesTmp.countProjects,
              contribuitors: technologiesTmp.countContributors,
              time: technologiesTmp.time
            });

            this.productService.getByTechnology(res.id).subscribe({
              next: (resProducts: Product[]) => {
                this.products.set(this.modelMapperService.mapProductList(resProducts as unknown[]));
                this.requestStateService.finish();
              }
              // El error local se delega automáticamente al interceptor global
            });
          }
        });
      } else {
        this.technology = new Technology();
        this.requestStateService.finish();
      }
    });

    this.filteredUsers = this.form.get('responsible')?.valueChanges.pipe(
      startWith(''),
      map(value => typeof value === 'string' ? value : (value?.description ?? '')),
      mergeMap(value => value ? this._filter(value) : this._getAll())
    );     
  }

  private buildForm(): void {
    this.form = this.fb.group({
      id: [''],
      name: ['', [Validators.required]],
      description: ['', [Validators.required]],
      responsible: ['', [Validators.required]],
      products: [{ value: '', disabled: true }, Validators.required],
      projects: [{ value: '', disabled: true }, Validators.required],
      contribuitors: [{ value: '', disabled: true }, Validators.required],
      time: [{ value: '', disabled: true }, Validators.required]
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
    if (this.form.valid) {
      this.formStateService.startSubmit();
      const payload = this.buildTechnologyPayload();
      if (!payload) {
        this.formStateService.setError('Debes seleccionar Responsable desde la lista.');
        return;
      }
      this.technologyService.create(payload).subscribe({
        next: () => {
          this.formStateService.setSuccess('Tecnología creada con éxito.');
          this.router.navigateByUrl('/pvt/technology');
        },
        error: (err) => this.handleFormError(err, 'No se pudo crear la tecnología debido a restricciones del sistema.')
      });
    }    
  } 

  delete(event: Event): void {
    event.preventDefault();
    if (this.form.valid) {
      const payload = this.form.getRawValue();
      this.technology = Technology.fromObject(payload);
      this.formStateService.startSubmit();

      this.technologyService.delete(this.technology.id).subscribe({
        next: () => {
          this.formStateService.setSuccess('Tecnología eliminada con éxito.');
          this.router.navigateByUrl('/pvt/technology');
        },
        error: (err) => this.handleFormError(err, 'No se pudo eliminar la tecnología debido a dependencias en el sistema.')
      });
    }
  }

  update(event: Event): void {
    event.preventDefault();
    if (this.form.valid) {
      this.formStateService.startSubmit();
      const payload = this.buildTechnologyPayload();
      if (!payload) {
        this.formStateService.setError('Debes seleccionar Responsable desde la lista.');
        return;
      }
      const id = this.form.getRawValue().id;
      if (typeof id !== 'number') {
        this.formStateService.setError('No se pudo identificar la tecnología a actualizar.');
        return;
      }
      this.technologyService.update(id, payload).subscribe({
        next: () => {
          this.formStateService.setSuccess('Tecnología actualizada con éxito.');
          this.router.navigateByUrl('/pvt/technology');
        },
        error: (err) => this.handleFormError(err, 'No se pudo actualizar la tecnología debido a restricciones del sistema.')
      });
    }
  } 

  private handleFormError(err: any, fallbackMessage: string): void {
    if (err?.status === 403) {
      this.formStateService.setError('No tienes permisos para realizar esta acción.');
    } else {
      const backendMessage = err?.error?.message || fallbackMessage;
      this.formStateService.setError(backendMessage);
    }
  }


  private buildTechnologyPayload(): TechnologyRequest | null {
    const raw = this.form.getRawValue();
    const responsibleId = this.extractEntityId(raw.responsible);

    if (!responsibleId) {
      return null;
    }

    return {
      name: raw.name,
      description: raw.description,
      responsibleId,
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
}
