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
import { AuthService } from '@core/services/auth.service';
import { ImputationService } from '@core/services/imputation.service';
import { ProductService } from '@core/services/product.service';
import { MatSelectModule } from '@angular/material/select';

@Component({
  selector: 'app-imputation-detail',
  imports: [FormsModule, CommonModule, MatTableModule, MatCardModule, ReactiveFormsModule, MatInputModule, MatAutocompleteModule, MatDatepickerModule, MatNativeDateModule, MatButtonModule, MatSelectModule],
  templateUrl: './imputation-detail.component.html',
  styleUrl: './imputation-detail.component.scss'
})

export class ImputationDetailComponent {

  imputationService = inject(ImputationService);
  productService = inject(ProductService);
  authService = inject(AuthService);
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

  dataSourceItems!: MatTableDataSource<any>;

  totalTime: number = 0;

  error!: string;
  message!: string;
  message2!: string;  


  ngOnInit(): void {
    this.getProducts();
    this.buildForm();
    this.activatedRoute.params.subscribe(params => {
      let id = params['id'];
      const cmpDate = /\d{4}-\d{2}-\d{2}/;
      if(!id.match(cmpDate)){
        this.imputationService.get(id).subscribe({
          next:(res: Imputation)=> {
            this.getImputationForm(res);
          },
          error: (err: any) => console.log(err)
        });
      }else{
        this.imputationForm.get('date')?.setValue(id);
        this.addItemForm();
      }
    });     
  }  

  getProducts(){
//    this.productService.getByContributor(this.authService.user.id).subscribe({
    this.productService.getByContributor().subscribe({
      next: (res: Product[]) => {
        this.products.set(res);
      },
      error: (err: any) => console.log(err),
    });    
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
      this.imputation = new Imputation();
      this.imputation.date = this.imputationForm.get('date')?.value;
      for(let item of this.imputationForm.get('imputationItemForm')?.value){
        if(!this.updateItem(item)){
          this.loadItem(item);
        }
      }
      this.imputationService.create(this.imputation).subscribe({
        next: (res: any) => {
          this.router.navigateByUrl('/pvt/imputation');
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
    if(this.imputationForm.valid){
      this.imputation = new Imputation();
      this.imputation.id = this.imputationForm.get('id')?.value;
      this.imputation.date = this.imputationForm.get('date')?.value;
      for(let item of this.imputationForm.get('imputationItemForm')?.value){
        if(!this.updateItem(item)){
          this.loadItem(item);
        }
      }
      this.imputationService.update(this.imputation).subscribe({
        next: (res: any) => {
          this.router.navigateByUrl('/pvt/imputation');
        },
        error: (err: any) => {
          this.error = err.error.error;
          this.message = err.error.message;
          this.message2 = err.message;
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
        next: (res: any) => {
          this.router.navigateByUrl('/pvt/imputation');
        },
        error: (err: any) => {
          this.error = err.error.error;
          this.message = err.error.message;
          this.message2 = err.message;
        },
      });
    }
  }     

  dataChange(){
    const date = `${this.imputationForm.get('date')?.value.getFullYear()}-${(this.imputationForm.get('date')?.value.getMonth()+1).toString().padStart(2, '0')}-${this.imputationForm.get('date')?.value.getDate().toString().padStart(2, '0')}`;
    this.imputationService.getByDate(date).subscribe({
      next:(res: Imputation)=> {
        this.totalTime = 0;
        this.buildForm();
        if(res==null){
          this.imputationForm.get('date')?.setValue(date);
          this.addItemForm();
        }else{
          this.getImputationForm(res);
        }
      },
      error: (err: any) => console.log(err)
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


