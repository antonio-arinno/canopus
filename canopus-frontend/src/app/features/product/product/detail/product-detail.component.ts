import { Component, inject, signal, WritableSignal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { combineLatest, distinctUntilChanged, map, Observable, of, shareReplay, startWith, switchMap } from 'rxjs';

import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatTableModule } from '@angular/material/table';

import { ProductService } from '@features/product/data/product.service';
import { UserService } from '@features/user/data/user.service';
import { ProjectService } from '@features/project/data/project.service';
import { Product, ProductRequest } from '@core/model/product';
import { User } from '@core/model/user';
import { Technology } from '@core/model/technology';
import { Project } from '@core/model/project';
import { ModelMapperService } from '@core/model/model-mapper.service';
import { FormStateService } from '@core/ui/form-state.service';
import { FormValidationService } from '@core/ui/form-validation.service';
import { RequestStateService } from '@core/ui/request-state.service';

@Component({
  selector: 'app-product-detail',
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
  templateUrl: './product-detail.component.html',
  styleUrl: './product-detail.component.scss'
})
export class ProductDetailComponent implements OnInit {

  // Inyección funcional encapsulada con modificadores de acceso óptimos
  private readonly productService = inject(ProductService);
  private readonly projectService = inject(ProjectService);
  private readonly userService = inject(UserService);
  private readonly modelMapperService = inject(ModelMapperService);
  private readonly formStateService = inject(FormStateService);
  private readonly formValidationService = inject(FormValidationService);
  private readonly requestStateService = inject(RequestStateService);
  private readonly router = inject(Router);
  private readonly activateRoute = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);

  projects: WritableSignal<Project[]> = signal([]);
  form!: FormGroup;
  product!: Product;

  filteredResponsibleUsers: Observable<User[]> | undefined;
  filteredBackupUsers: Observable<User[]> | undefined;
  filteredTechnologies: Observable<Technology[]> | undefined;
  
  private userTechnologies$!: Observable<Technology[]>;
  private usersByTechnology$!: Observable<User[]>;

  // Atributos reactivos compartidos con el HTML mediante Signals
  readonly isSubmitting = this.formStateService.isSubmitting;
  readonly successMessage = this.formStateService.successMessage;
  readonly isLoading = this.requestStateService.isLoading;
  readonly errorMessage = this.requestStateService.errorMessage;

  
  displayedColumns: string[] = ['projectName', 'status', 'countContributors', 'time'];

  ngOnInit(): void {
    this.buildForm();
    this.formStateService.clear();
    this.requestStateService.start();

    this.activateRoute.params.subscribe(params => {
      const id = params['id'];
      if (id) {
        this.productService.get(id).subscribe({
          next: (res: Product) => {
            const productsTmp = this.modelMapperService.mapProduct(res);
            
            // Rellenado atómico del formulario
            this.form.patchValue({
              id: productsTmp.id,
              name: productsTmp.name,
              description: productsTmp.description,
              technology: productsTmp.technology,
              responsible: productsTmp.responsible,
              backup: productsTmp.backup,
              projects: productsTmp.countProjects,
              contribuitors: productsTmp.countContributors,
              time: productsTmp.time,
              avgTime: productsTmp.avgTime,
              avgDuration: productsTmp.avgDuration
            });

            this.projectService.getByProduct(res.id).subscribe({
              next: (resProjects: Project[]) => {
                const mappedProjects = this.modelMapperService.mapProjectList(resProjects as unknown[]);
                this.projects.set(mappedProjects);
                this.requestStateService.finish();
              }
            });
          }
        });
      } else {
        this.product = new Product();
        this.requestStateService.finish();
      }
    });

    this.userTechnologies$ = this.userService.getMe().pipe(
      map(user => user.technologies ?? []),
      shareReplay({ bufferSize: 1, refCount: true })
    );

    // Reactividad en cascada al cambiar la tecnología
    this.usersByTechnology$ = this.form.get('technology')!.valueChanges.pipe(
      startWith(''),
      map(value => (value && typeof value === 'object' && (value as Technology).id) ? (value as Technology).id : null),
      distinctUntilChanged(),
      switchMap(technologyId => technologyId ? this.userService.getByTechnology(technologyId) : of([])),
      shareReplay({ bufferSize: 1, refCount: true })
    );

    this.filteredResponsibleUsers = combineLatest([
      this.usersByTechnology$,
      this.form.get('responsible')!.valueChanges.pipe(startWith(''))
    ]).pipe(
      map(([users, value]) => this._filterUsers(users, value))
    );

    this.filteredBackupUsers = combineLatest([
      this.usersByTechnology$,
      this.form.get('backup')!.valueChanges.pipe(startWith(''))
    ]).pipe(
      map(([users, value]) => this._filterUsers(users, value))
    );

    this.form.get('technology')!.valueChanges.pipe(
      map(value => (value && typeof value === 'object' && (value as Technology).id) ? (value as Technology).id : null),
      distinctUntilChanged()
    ).subscribe(() => {
      this.form.get('responsible')?.setValue(null, { emitEvent: false });
      this.form.get('backup')?.setValue(null, { emitEvent: false });
    });

    this.filteredTechnologies = this.form.get('technology')?.valueChanges.pipe(
      startWith(''),
      map(value => typeof value === 'string' ? value : value?.name ?? ''),
      switchMap(value => this.userTechnologies$.pipe(
        map(technologies => this._filterTechnologies(technologies, value))
      ))
    );   
  }

  private buildForm(): void {
    this.form = this.fb.group({
      id: [''],
      name: ['', [Validators.required]],
      description: ['', [Validators.required]],
      technology: ['', [Validators.required]],
      responsible: ['', [Validators.required]],
      backup: ['', [Validators.required]],
      projects: [{ value: '', disabled: true }, Validators.required],
      contribuitors: [{ value: '', disabled: true }, Validators.required],
      time: [{ value: '', disabled: true }, Validators.required],
      avgTime: [{ value: '', disabled: true }, Validators.required],
      avgDuration: [{ value: '', disabled: true }, Validators.required]
    });  
  }  

  create(event: Event): void {
    event.preventDefault();
    if (this.form.valid) {
      this.formStateService.startSubmit();
      const payload = this.buildProductPayload();
      if (!payload) {
        this.formStateService.setError('Debes seleccionar Tecnologia, Responsable y Backup desde la lista.');
        return;
      }
      this.productService.create(payload).subscribe({
        next: () => {
          this.formStateService.setSuccess('Producto creado con éxito.');
          this.router.navigateByUrl('/pvt/product');
        },
         error: () => this.formStateService.reset()
      });
    }    
  } 

  update(event: Event): void {
    event.preventDefault();
    if (this.form.valid) {
      this.formStateService.startSubmit();
      const payload = this.buildProductPayload();
      if (!payload) {
        this.formStateService.setError('Debes seleccionar Tecnologia, Responsable y Backup desde la lista.');
        return;
      }
      this.productService.update(payload).subscribe({
        next: () => {
          this.formStateService.setSuccess('Producto actualizado con éxito.');
          this.router.navigateByUrl('/pvt/product');
        },
        error: () => this.formStateService.reset()
      });
    }
  } 

  delete(event: Event): void {
    event.preventDefault();
    if (this.form.valid) {
      this.formStateService.startSubmit();
      const payload = this.form.getRawValue();
      this.product = Product.fromObject(payload);
      this.productService.delete(this.product.id).subscribe({
        next: () => {
          this.router.navigateByUrl('/pvt/product');
        },
        error: () => this.formStateService.reset()
      });
    }
  }

  private buildProductPayload(): ProductRequest | null {
    const raw = this.form.getRawValue();
    const technologyId = this.extractEntityId(raw.technology);
    const responsibleId = this.extractEntityId(raw.responsible);
    const backupId = this.extractEntityId(raw.backup);

    if (!technologyId || !responsibleId || !backupId) {
      return null;
    }

    return {
      ...(raw.id ? { id: raw.id } : {}),
      name: raw.name,
      description: raw.description,
      technologyId,
      responsibleId,
      backupId,
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

  private _filterUsers(users: User[], value: string | User): User[] {
    const term = typeof value === 'string' ? value : value?.name ?? '';
    const filterValue = term.toLowerCase();
    return filterValue? users.filter(user => user.name?.toLowerCase().includes(filterValue)): users;
  }
  
  displayFn(user: User): string {
    return user && user.name ? user.name : '';
  }
  
  private _filterTechnologies(technologies: Technology[], value: string): Technology[] {
    const filterValue = value.toLowerCase();
    return filterValue? technologies.filter(technology => technology.name?.toLowerCase().includes(filterValue)): technologies;
  }
  
  displayTechnology(technology: Technology): string {
    return technology && technology.name ? technology.name : '';
  }
  
  getValidationMessage(controlName: string): string | null {
    return this.formValidationService.getErrorMessage(this.form.get(controlName));
  }
}