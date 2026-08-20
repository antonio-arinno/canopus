import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject, signal, WritableSignal } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatNativeDateModule } from '@angular/material/core';
import { MatInputModule } from '@angular/material/input';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { ActivatedRoute, Router } from '@angular/router';
import { Imputation } from '@core/model/imputation';
import { ImputationItem } from '@core/model/imputation-item';
import { Product } from '@core/model/product';
import { Project } from '@core/model/project';
import { AuthService } from '@core/auth/auth.service';
import { ImputationService } from '@features/imputation/data/imputation.service';
import { ProductService } from '@features/product/data/product.service';
import { MatSelectModule } from '@angular/material/select';
import { ProjectService } from '@features/project/data/project.service';
import { ModelMapperService } from '@core/model/model-mapper.service';
import { FormStateService } from '@core/ui/form-state.service';
import { FormValidationService } from '@core/ui/form-validation.service';
import { RequestStateService } from '@core/ui/request-state.service';

@Component({
  selector: 'app-imputation-detail',
  imports: [FormsModule, CommonModule, MatTableModule, MatCardModule, ReactiveFormsModule, MatInputModule, MatAutocompleteModule, MatDatepickerModule, MatNativeDateModule, MatButtonModule, MatSelectModule],
  templateUrl: './imputation-detail.component.html',
  styleUrl: './imputation-detail.component.scss'
})

export class ImputationDetailComponent {

  imputationService = inject(ImputationService);
  productService = inject(ProductService);
  projectService = inject(ProjectService);
  authService = inject(AuthService);
  modelMapperService = inject(ModelMapperService);
  formStateService = inject(FormStateService);
  formValidationService = inject(FormValidationService);
  requestStateService = inject(RequestStateService);
  fb = inject(FormBuilder);
  cd = inject(ChangeDetectorRef)
  router = inject(Router);

  activatedRoute = inject(ActivatedRoute);

  displayedColumns: string[] = ['project', 'time', 'delete'];
  
  imputationForm!: FormGroup;
  imputationItemForm!: FormGroup;
  imputation!: Imputation;
  imputationItem!: ImputationItem;
  product!: Product;
  project!: Project;

  products: WritableSignal<Product[]> = signal([]);

  productsTmp: Product[] = [];
  projects: Project[] = [];

  dataSourceItems!: MatTableDataSource<any>;

  totalTime: number = 0;

  readonly isSubmitting = this.formStateService.isSubmitting;
  readonly successMessage = this.formStateService.successMessage;
  readonly submitErrorMessage = this.formStateService.errorMessage;
  readonly isLoading = this.requestStateService.isLoading;
  readonly errorMessage = this.requestStateService.errorMessage;

  ngOnInit(){
    this.requestStateService.start();
    this.buildForm();
    this.activatedRoute.params.subscribe(params => {
      let id = params['id'];
      const cmpDate = /\d{4}-\d{2}-\d{2}/;
      if(!id.match(cmpDate)){
        this.imputationService.get(id).subscribe({
          next:(res: Imputation)=> {
            this.getProjects(res.date);
            this.getImputationForm(res);
            this.requestStateService.finish();
          },
          error: (err: any) => this.requestStateService.setError(err)
        });
      }else{
        this.imputationForm.get('date')?.setValue(id);
        this.addItemForm();
        this.getProjects(id);
      }

    });   
  }   

  getProjects(id: string){
    this.requestStateService.start();
    this.projectService.getByOpenAndDate(id).subscribe({
      next: (res: Project[]) => {
        const mappedProjects = this.modelMapperService.mapProjectList(res as unknown[]);
        this.projects = [...mappedProjects];
        this.updateList();
        this.products.set(this.productsTmp);
        this.requestStateService.finish();
      },
      error: (err: any) => this.requestStateService.setError(err),
    });  
    
  }


  updateList(){
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

  getImputationForm(res: any){
    this.imputationForm.get('id')?.setValue(res.id);
    this.imputationForm.get('date')?.setValue(res.date);
    for(let item of res.items){
      this.addItem(item);
      this.totalTime = this.totalTime + item.time;
    }
  }

  get item() {
    return this.imputationForm.controls["imputationItemForm"] as FormArray;
  };

  formEmpty(): Boolean {
    if((this.imputationForm.controls["imputationItemForm"] as FormArray).length == 0){
      return true;
    }
    return false;
  }

  addItem(value: any): void {
    this.buildFormItem();
    this.imputationItemForm.get('id')?.setValue(value.id);
    this.imputationItemForm.get('projectId')?.setValue(value.project.id);
    this.imputationItemForm.get('time')?.setValue(value.time);
    this.item.push(this.imputationItemForm);
    this.dataSourceItems = new MatTableDataSource(this.item.controls);
  };

  addItemForm(): void {
    this.buildFormItem();
    this.item.push(this.imputationItemForm);
    this.dataSourceItems = new MatTableDataSource(this.item.controls);
    this.cd.detectChanges();
  };

  private buildForm(){
    this.imputationForm = this.fb.group({
      id: [''],
      date: ['', Validators.required],   
      imputationItemForm: this.fb.array([])
    })
  }

  private buildFormItem(){
    this.imputationItemForm = this.fb.group({
      id: [''],
      projectId: ['', Validators.required],
      time: ['', Validators.required]  
    });
  }

  create(event: Event): void {
    event.preventDefault();
    if(this.imputationForm.valid){
      this.formStateService.startSubmit();
      const payload = this.buildImputationPayload();
      if (!payload) {
        this.formStateService.setError('Debes completar la fecha y al menos una fila valida.');
        return;
      }
      this.imputation = payload;
      this.imputationService.create(this.imputation).subscribe({
        next: () => {
          this.formStateService.setSuccess('Imputación creada con éxito.');
          this.router.navigateByUrl('/pvt/imputation');
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
    if(this.imputationForm.valid){
      this.formStateService.startSubmit();
      const payload = this.buildImputationPayload();
      if (!payload) {
        this.formStateService.setError('Debes completar la fecha y al menos una fila valida.');
        return;
      }
      this.imputation = payload;
      this.imputationService.update(this.imputation).subscribe({
        next: () => {
          this.formStateService.setSuccess('Imputación actualizada con éxito.');
          this.router.navigateByUrl('/pvt/imputation');
        },
        error: (err: any) => {
          const message = this.extractErrorMessage(err);
          this.formStateService.setError(message);
        },
      });      
    }    
  }

  loadItem(itemForm: any): void{
    this.imputationItem = new ImputationItem();
    this.imputationItem.id = itemForm.id;
    this.project = new Project();
    this.project.id = itemForm.projectId; //no es necesario para el create
    this.imputationItem.project = this.project;
    this.imputationItem.time = itemForm.time;
    this.imputation.items.push(this.imputationItem);    
  }

  updateItem(itemForm: any): Boolean{
    for(let item of this.imputation.items){
      if(item.project.id==itemForm.projectId){
        item.time = item.time + itemForm.time;  
        return true;
      }
    }
    return false;
  }

  delete(event: Event): void {
    event.preventDefault();
    if(this.imputationForm.valid){
      this.imputation = this.imputationForm.value;
      this.imputationService.delete(this.imputation.id).subscribe({
        next: () => {
          this.router.navigateByUrl('/pvt/imputation');
        },
        error: (err: any) => {
          const message = this.extractErrorMessage(err);
          this.formStateService.setError(message);
        },
      });
    }
  }     

  private buildImputationPayload(): Imputation | null {
    const payload = this.imputationForm.getRawValue();
    const date = this.normalizeDate(payload.date);
    const items = payload.imputationItemForm as Array<{ id?: number; projectId?: number; time?: number }>;

    if (!date || !Array.isArray(items) || items.length === 0) {
      return null;
    }

    const imputation = new Imputation();
    imputation.id = payload.id;
    imputation.date = date;
    this.imputation = imputation;

    for (const item of items) {
      if (!item?.projectId || !item?.time) {
        continue;
      }
      if (!this.updateItem(item)) {
        this.loadItem(item);
      }
    }

    if (imputation.items.length === 0) {
      return null;
    }

    return imputation;
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

  getValidationMessage(controlName: string): string | null {
    return this.formValidationService.getErrorMessage(this.imputationForm.get(controlName));
  }

  dataChange(){
    const date = `${this.imputationForm.get('date')?.value.getFullYear()}-${(this.imputationForm.get('date')?.value.getMonth()+1).toString().padStart(2, '0')}-${this.imputationForm.get('date')?.value.getDate().toString().padStart(2, '0')}`;
    this.requestStateService.start();
    this.getProjects(date);
    this.imputationService.getByDate(date).subscribe({
      next:(res: Imputation)=> {
        this.totalTime = 0;
        this.buildForm();
        this.getImputationForm(res);      
        this.requestStateService.finish();
      },
        error: (err: any) => {
          if (err.error.status == 404){
            this.buildForm();
            this.imputationForm.get('date')?.setValue(date);
            this.addItemForm();
          }else{
            const message = this.extractErrorMessage(err);
            this.formStateService.setError(message);
          }
        },
    });
  }

  deleteImputationItem(item: number): void {
    this.item.removeAt(item);
    this.dataSourceItems = new MatTableDataSource(this.item.controls);
    this.updateTime();
  }

  updateTime():void {
    this.totalTime = 0;
    for(let item of this.item.value){
      this.totalTime = this.totalTime + item.time;
    }
  }

}


