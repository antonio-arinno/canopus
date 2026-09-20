import { Component, inject, signal, WritableSignal, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';

import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatNativeDateModule } from '@angular/material/core';
import { MatInputModule } from '@angular/material/input';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatSelectModule } from '@angular/material/select';

import { Imputation } from '@core/model/imputation';
import { Product } from '@core/model/product';
import { Project } from '@core/model/project';
import { AuthService } from '@core/auth/auth.service';
import { ImputationRequest, ImputationService } from '@features/imputation/data/imputation.service';
import { ProductService } from '@features/product/data/product.service';
import { ProjectService } from '@features/project/data/project.service';
import { ModelMapperService } from '@core/model/model-mapper.service';
import { FormStateService } from '@core/ui/form-state.service';
import { FormValidationService } from '@core/ui/form-validation.service';
import { RequestStateService } from '@core/ui/request-state.service';

@Component({
  selector: 'app-imputation-detail',
  imports: [
    FormsModule, 
    CommonModule, 
    MatTableModule, 
    MatCardModule, 
    ReactiveFormsModule, 
    MatInputModule, 
    MatAutocompleteModule, 
    MatDatepickerModule, 
    MatNativeDateModule, 
    MatButtonModule, 
    MatSelectModule
  ],
  templateUrl: './imputation-detail.component.html',
  styleUrl: './imputation-detail.component.scss'
})
export class ImputationDetailComponent implements OnInit {

  // Inyección funcional encapsulada con modificadores limpios de solo lectura
  private readonly imputationService = inject(ImputationService);
  private readonly productService = inject(ProductService);
  private readonly projectService = inject(ProjectService);
  private readonly authService = inject(AuthService);
  private readonly modelMapperService = inject(ModelMapperService);
  private readonly formStateService = inject(FormStateService);
  private readonly formValidationService = inject(FormValidationService);
  private readonly requestStateService = inject(RequestStateService);
  private readonly fb = inject(FormBuilder);
  private readonly cd = inject(ChangeDetectorRef);
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);

  displayedColumns: string[] = ['project', 'time', 'delete'];
  
  imputationForm!: FormGroup;
  imputationItemForm!: FormGroup;
  imputation!: Imputation;
  product!: Product;

  products: WritableSignal<Product[]> = signal([]);
  productsTmp: Product[] = [];
  projects: Project[] = [];
  dataSourceItems!: MatTableDataSource<any>;

  totalTime = 0;

  // Atributos reactivos expuestos al HTML mediante Signals globales del Core
  readonly isSubmitting = this.formStateService.isSubmitting;
  readonly successMessage = this.formStateService.successMessage;
  readonly submitErrorMessage = this.formStateService.errorMessage;
  readonly isLoading = this.requestStateService.isLoading;
  readonly errorMessage = this.requestStateService.errorMessage;

  ngOnInit(): void {
    this.formStateService.clear();
    this.requestStateService.start();
    this.buildForm();
    
    this.activatedRoute.params.subscribe(params => {
      const id = params['id'];
      const cmpDate = /\d{4}-\d{2}-\d{2}/;
      
      if (!id.match(cmpDate)) {
        this.imputationService.get(id).subscribe({
          next: (res: Imputation) => {
            this.getProjects(res.date);
            this.getImputationForm(res);
            this.requestStateService.finish();
          }
        });
      } else {
        this.imputationForm.get('date')?.setValue(id);
        this.addItemForm();
        this.getProjects(id);
      }
    });   
  }   

  getProjects(id: string): void {
    this.requestStateService.start();
    this.projectService.getByOpenAndDate(id).subscribe({
      next: (res: Project[]) => {
        const mappedProjects = this.modelMapperService.mapProjectList(res as unknown[]);
        this.projects = [...mappedProjects];
        this.updateList();
        this.products.set(this.productsTmp);
        this.requestStateService.finish();
      }
    });  
  }

  updateList(): void {
    this.productsTmp = [];

    for (const project of this.projects) {
      const existingProduct = this.productsTmp.find((product) => product.id === project.product.id);

      if (existingProduct) {
        existingProduct.projects.push(Project.fromObject(project));
        continue;
      }

      const productView = Product.fromObject({
        id: project.product.id,
        name: project.product.name,
        description: '',
        technology: project.product.technology,
        responsible: project.product.responsible,
        backup: project.product.backup,
        countProjects: 0,
        countContributors: 0,
        time: 0,
        avgTime: 0,
        avgDuration: 0,
        projects: []
      });

      productView.projects.push(Project.fromObject(project));
      this.productsTmp.push(productView);
    }
  }

  getImputationForm(res: any): void {
    this.imputationForm.patchValue({
      id: res.id,
      date: res.date
    });
    
    for (const item of res.items) {
      this.addItem(item);
      this.totalTime += item.time;
    }
  }

  // Getter tipado para acceder cómodamente al FormArray desde la vista o métodos
  get item(): FormArray {
    return this.imputationForm.controls["imputationItemForm"] as FormArray;
  }

  formEmpty(): boolean {
    return this.item.length === 0;
  }

  addItem(value: any): void {
    const itemGroup = this.fb.group({
      id: [value.id || ''],
      projectId: [value.project?.id || '', Validators.required],
      time: [value.time || '', Validators.required]
    });
    
    this.item.push(itemGroup);
    this.dataSourceItems = new MatTableDataSource(this.item.controls);
  }

  addItemForm(): void {
    const itemGroup = this.fb.group({
      id: [''],
      projectId: ['', Validators.required],
      time: ['', Validators.required]
    });
    
    this.item.push(itemGroup);
    this.dataSourceItems = new MatTableDataSource(this.item.controls);
    this.cd.detectChanges();
  }

  private buildForm(): void {
    this.imputationForm = this.fb.group({
      id: [''],
      date: ['', Validators.required],   
      imputationItemForm: this.fb.array([])
    });
  }

  create(event: Event): void {
    event.preventDefault();
    if (this.imputationForm.valid) {
      this.formStateService.startSubmit();
      const payload = this.buildImputationPayload();
      if (!payload) {
        this.formStateService.setError('Debes completar la fecha y al menos una fila válida.');
        return;
      }
      this.imputationService.create(payload).subscribe({
        next: () => {
          this.formStateService.setSuccess('Imputación creada con éxito.');
          this.router.navigateByUrl('/pvt/imputation');
        }
      });      
    }    
  }

  update(event: Event): void {
    event.preventDefault();
    if (this.imputationForm.valid) {
      this.formStateService.startSubmit();
      const payload = this.buildImputationPayload();
      if (!payload) {
        this.formStateService.setError('Debes completar la fecha y al menos una fila válida.');
        return;
      }
      const id = this.imputationForm.getRawValue().id;
      if (typeof id !== 'number') {
        this.formStateService.setError('No se pudo identificar la imputación a actualizar.');
        return;
      }
      this.imputationService.update(id, payload).subscribe({
        next: () => {
          this.formStateService.setSuccess('Imputación actualizada con éxito.');
          this.router.navigateByUrl('/pvt/imputation');
        }
      });      
    }    
  }

  delete(event: Event): void {
    event.preventDefault();
    if (this.imputationForm.valid) {
      this.imputation = this.imputationForm.value;
      this.imputationService.delete(this.imputation.id).subscribe({
        next: () => {
          this.router.navigateByUrl('/pvt/imputation');
        }
      });
    }
  }     

  private buildImputationPayload(): ImputationRequest | null {
    const payload = this.imputationForm.getRawValue();
    const date = this.normalizeDate(payload.date);
    const items = payload.imputationItemForm as Array<{ id?: number; projectId?: number; time?: number }>;

    if (!date || !Array.isArray(items) || items.length === 0) {
      return null;
    }

    const mergedItems = new Map<number, number>();
    for (const item of items) {
      if (!item?.projectId || !item?.time) {
        continue;
      }
      mergedItems.set(item.projectId, (mergedItems.get(item.projectId) ?? 0) + item.time);
    }

    if (mergedItems.size === 0) {
      return null;
    }

    return {
      date,
      items: Array.from(mergedItems, ([projectId, time]) => ({ projectId, time })),
    };
  }

  private normalizeDate(value: unknown): string | null {
    if (value instanceof Date) {
      const year = value.getFullYear();
      const month = (value.getMonth() + 1).toString().padStart(2, '0');
      const day = value.getDate().toString().padStart(2, '0');
      return `${year}-${month}-${day}`;
    }

    if (typeof value === 'string' && value.trim().length > 0) {
      return value;
    }

    return null;
  }

  getValidationMessage(controlName: string): string | null {
    return this.formValidationService.getErrorMessage(this.imputationForm.get(controlName));
  }

  dataChange(): void {
    const dateValue = this.imputationForm.get('date')?.value;
    if (!dateValue) return;
    const date = `${dateValue.getFullYear()}-${(dateValue.getMonth() + 1).toString().padStart(2, '0')}-${dateValue.getDate().toString().padStart(2, '0')}`;
    this.requestStateService.start();
    this.getProjects(date);
    this.imputationService.getByDate(date).subscribe({
      next: (res: Imputation) => {
        this.totalTime = 0;
        this.buildForm();
        this.getImputationForm(res);
        this.requestStateService.finish();
      },
      error: (err: any) => {
        // Manejamos de forma específica el 404 de negocio (día sin imputar previo)
        if (err?.status === 404 || err?.error?.status === 404) {
          this.buildForm();
          this.imputationForm.get('date')?.setValue(dateValue);
          this.addItemForm();
          this.requestStateService.finish();
        }
      }
    });
  }

  deleteImputationItem(index: number): void {
    this.item.removeAt(index);
    this.dataSourceItems = new MatTableDataSource(this.item.controls);
    this.updateTime();
  }
  
  updateTime(): void {
    this.totalTime = 0;
    for (const item of this.item.value) {
      this.totalTime += (item.time || 0);
    }
  }
}
