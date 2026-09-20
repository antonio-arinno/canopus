import { Component, inject, WritableSignal, signal, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Observable, combineLatest, of, concat, map, mergeMap, startWith } from 'rxjs';

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

import { ProjectRequest, ProjectService } from '@features/project/data/project.service';
import { UserService } from '@features/user/data/user.service';
import { ProductService } from '@features/product/data/product.service';
import { ImputationService } from '@features/imputation/data/imputation.service';
import { Project } from '@core/model/project';
import { Product } from '@core/model/product';
import { User } from '@core/model/user';
import { Status } from '@core/model/status';
import { ImputationSummary } from '@core/model/imputation-summary';
import { ModelMapperService } from '@core/model/model-mapper.service';
import { FormStateService } from '@core/ui/form-state.service';
import { FormValidationService } from '@core/ui/form-validation.service';
import { RequestStateService } from '@core/ui/request-state.service';

@Component({
  selector: 'app-project-detail',
  imports: [
    CommonModule, 
    MatCardModule, 
    MatInputModule, 
    MatButtonModule, 
    ReactiveFormsModule, 
    MatAutocompleteModule, 
    MatSelectModule, 
    MatDatepickerModule, 
    CdkDropList, 
    CdkDrag, 
    MatGridListModule, 
    MatTableModule
  ],
  templateUrl: './project-detail.component.html',
  providers: [provideNativeDateAdapter(), DatePipe],
  styleUrl: './project-detail.component.scss'
})
export class ProjectDetailComponent implements OnInit {

  // Inyección funcional encapsulada con modificadores óptimos de acceso
  private readonly projectService = inject(ProjectService);
  private readonly productService = inject(ProductService);
  private readonly imputationService = inject(ImputationService);
  private readonly userService = inject(UserService);
  private readonly modelMapperService = inject(ModelMapperService);
  private readonly formStateService = inject(FormStateService);
  private readonly formValidationService = inject(FormValidationService);
  private readonly requestStateService = inject(RequestStateService);
  private readonly router = inject(Router);
  private readonly activateRoute = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);
  private readonly datePipe = inject(DatePipe);

  form!: FormGroup;
  project!: Project;
  user!: User;
  dateOk = true;

  // Atributos reactivos públicos compartidos con la vista (Signals)
  readonly isSubmitting = this.formStateService.isSubmitting;
  readonly successMessage = this.formStateService.successMessage;
  readonly errorMessage = this.requestStateService.errorMessage;
  readonly isLoading = this.requestStateService.isLoading;

  users: WritableSignal<User[]> = signal([]);
  contribuitors: WritableSignal<User[]> = signal([]);
  imputationSummaries: WritableSignal<ImputationSummary[]> = signal([]);

  displayedColumns: string[] = ['name', 'time'];
  filteredProducts: Observable<Product[]> | undefined;    
  filteredUsers: Observable<User[]> | undefined;  

  public keys = Object.keys;
  public userRoles = Status;

  public getkeys(elementor: typeof Status) {
    return this.keys(elementor).map(key => key as keyof typeof elementor);
  }  

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

  ngOnInit(): void {
    this.formStateService.clear();
    this.requestStateService.start();
    this.buildForm();
    this.activateRoute.params.subscribe(params => {
      const id = params['id'];
      if (id) {
        this.projectService.get(id).subscribe({
          next: (res2: Project) => {
            const res = this.modelMapperService.mapProject(res2);
            
            // Consolidamos la carga de datos estructurada con patchValue
            this.form.patchValue({
              id: res.id,
              name: res.name,
              description: res.description,
              reference1: res.reference1,
              reference2: res.reference2,
              product: res.product,
              status: res.getStatus(),
              dateDev: res.dateDev ? new Date(res.dateDev.replaceAll('-', '/')) : null,
              datePre: res.datePre ? new Date(res.datePre.replaceAll('-', '/')) : null,
              datePro: res.datePro ? new Date(res.datePro.replaceAll('-', '/')) : null,
              responsible: res.responsible,
              countContributors: res.countContributors,
              time: res.time,
              duration: res.getDuration()
            });

            this.statusChange();
            this.contribuitors.set(res.contributors);
            
            this.userService.getByTechnology(res.product.technology.id).subscribe(users => {
              this.users.set(users);
              this.users.update((currentUsers) => currentUsers.filter(
                objeto => !res.contributors.some(objeto2 => objeto2.id === objeto.id)
              ));
            });

            this.imputationService.getByProject(res.id).subscribe({
              next: (payload: ImputationSummary[]) => {
                this.imputationSummaries.set(this.modelMapperService.mapImputationSummaryList(payload as unknown[]));
                this.requestStateService.finish();
              }
            });
          }
        });
      } else {       
        this.project = new Project();
        this.requestStateService.finish();
      }
    });

    this.filteredProducts = this.form.get('product')?.valueChanges.pipe(
      startWith(''),
      map(value => typeof value === 'string' ? value : value.description),
      mergeMap(value => value ? this._filter(value) : this._getAll())
    );
        
    this.filteredUsers = this.form.get('responsible')?.valueChanges.pipe(
      startWith(''),
      map(value => typeof value === 'string' ? value : value.name),
      mergeMap(value => value ? this._userFilter(value) : this._getUserAll())
    );

    // Escuchas reactivas para la validación lógica de flujos de fechas cruzadas
    this.form.get('dateDev')?.valueChanges.subscribe(() => {
      this.form.get('datePre')?.enable();
      this.statusChange();
    });

    this.form.get('datePre')?.valueChanges.subscribe(() => {
      const datePreValue = this.form.get('datePre')?.value;
      if (datePreValue) {
        if (this.form.get('dateDev')?.value > datePreValue) {
          this.dateOk = false;
        } else {
          this.dateOk = true;
          this.form.get('datePro')?.enable(); 
        }
      } else {
        this.form.get('datePro')?.setValue(null);
        this.form.get('datePro')?.disable(); 
      }
      this.statusChange();
    });

    this.form.get('datePro')?.valueChanges.subscribe(() => {
      const datePreValue = this.form.get('datePre')?.value;
      const dateProValue = this.form.get('datePro')?.value;
      if (datePreValue > dateProValue && dateProValue) {
        this.dateOk = false;
      } else {
        this.dateOk = true;
      }
      this.statusChange();
    });

    this.form.get('product')?.valueChanges.subscribe(() => {
      const techId = this.form.get('product')?.value?.technology?.id;
      if (techId) {
        this.userService.getByTechnology(techId).subscribe(users => {
          this.users.set(users);
          this.users.update((currentUsers) => currentUsers.filter(
            objeto => !this.contribuitors().some(objeto2 => objeto2.id === objeto.id)
          ));
        });
      }
    });
  }  

  private statusChange(): void {
    const datePro = { datepro: this.form.get('datePro')?.value };
    this.form.patchValue(datePro);
    const project = Project.fromObject(this.form.value);
    this.form.get('status')?.setValue(project.getStatus());
    this.form.get('duration')?.setValue(project.getDuration()); 
  }    

  private _getAll(): Observable<Product[]> {
    return this.productService.getByResponsibleOrBackupMe();
  }  

  private _filter(value: string): Observable<Product[]> {
    const filterValue = value.toLowerCase();
    return this._getAll().pipe(
      map(products => products.filter(product => product.name.toLowerCase().includes(filterValue)))
    );
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

  getValidationMessage(controlName: string): string | null {
    return this.formValidationService.getErrorMessage(this.form.get(controlName));
  }

  private buildForm(): void {
    this.form = this.fb.group({
      id: [''],
      name: ['', [Validators.required]],
      description: ['', [Validators.required]],
      reference1: [''],
      reference2: [''],
      product: ['', [Validators.required]],
      status: [{ value: '', disabled: true }],
      dateDev: [null, [Validators.required]], // Corregido Date por null inicial
      datePre: [{ value: null, disabled: true }],
      datePro: [{ value: null, disabled: true }],
      responsible: ['', [Validators.required]],
      countContributors: [{ value: '', disabled: true }, Validators.required],
      time: [{ value: '', disabled: true }, Validators.required],
      duration: [{ value: '', disabled: true }, Validators.required]
    });
  }

  update(event: Event): void {
    event.preventDefault();
    if (this.form.valid) {
      this.formStateService.startSubmit();
      const payload = this.buildProjectPayload();
      if (!payload) {
        this.formStateService.setError('Debes seleccionar Producto y Responsable desde la lista.');
        return;
      }
      const id = this.form.getRawValue().id;
      if (typeof id !== 'number') {
        this.formStateService.setError('No se pudo identificar la tecnología a actualizar.');
        return;
      }
      this.projectService.update(id, payload).subscribe({
        next: () => {
          this.formStateService.setSuccess('Proyecto actualizado con éxito.');
          this.router.navigateByUrl('/pvt/project');
        }
      }
    );
    }
  }

  create(event: Event): void {
    event.preventDefault();
    if (this.form.valid) {
      this.formStateService.startSubmit();
      const payload = this.buildProjectPayload();
      if (!payload) {
        this.formStateService.setError('Debes seleccionar Producto y Responsable desde la lista.');
        return;
      }
      this.projectService.create(payload).subscribe({
        next: () => {
          this.formStateService.setSuccess('Proyecto creado con éxito.');
          this.router.navigateByUrl('/pvt/project');
        }
      });
    }
  }

  delete(event: Event): void {
    event.preventDefault();
    if (this.form.valid) {
      this.project = this.form.value;
      this.projectService.delete(this.project.id).subscribe({
        next: () => {
          this.router.navigateByUrl('/pvt/project');
        }
      });
    }
  }

  private buildProjectPayload(): ProjectRequest | null {
    const raw = this.form.getRawValue();
    const productId = this.extractEntityId(raw.product);
    const responsibleId = this.extractEntityId(raw.responsible);
    const dateDev = this.datePipe.transform(raw.dateDev, 'yyyy-MM-dd') ?? undefined;
    const datePre = this.datePipe.transform(raw.datePre, 'yyyy-MM-dd') ?? undefined;
    const datePro = this.datePipe.transform(raw.datePro, 'yyyy-MM-dd') ?? undefined;
    if (!productId || !responsibleId || !dateDev) {
      return null;
    }
    const contributorIds = this.contribuitors()
      .map((user) => this.extractEntityId(user))
      .filter((id): id is number => id !== null);
    return {
      name: raw.name,
      description: raw.description,
      reference1: raw.reference1,
      reference2: raw.reference2,
      dateDev,
      datePre,
      datePro,
      productId,
      responsibleId,
      contributorIds,
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

