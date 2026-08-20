import { CommonModule } from '@angular/common';
import { Component, inject, WritableSignal, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';

import {
  CdkDragDrop,
  moveItemInArray,
  transferArrayItem,
  CdkDrag,
  CdkDropList,
} from '@angular/cdk/drag-drop';

import { User } from '@core/model/user';
import { UserService } from '@features/user/data/user.service';
import { TechnologyService } from '@features/technology/data/technology.service';
import { Technology } from '@core/model/technology';
import { ProductService } from '@features/product/data/product.service';
import { Product } from '@core/model/product';
import { ModelMapperService } from '@core/model/model-mapper.service';
import { forkJoin } from 'rxjs';
import { FormStateService } from '@core/ui/form-state.service';
import { RequestStateService } from '@core/ui/request-state.service';

@Component({
  selector: 'app-user-detail',
  imports: [CommonModule, MatCardModule, ReactiveFormsModule, MatInputModule, MatAutocompleteModule, MatButtonModule, CdkDropList, CdkDrag, MatGridListModule, MatTableModule],
  templateUrl: './user-detail.component.html',
  styleUrl: './user-detail.component.scss'
})
export class UserDetailComponent {

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

  userService = inject(UserService);
  technologyService = inject(TechnologyService);
  productService = inject(ProductService);
  modelMapperService = inject(ModelMapperService);
  formStateService = inject(FormStateService);
  requestStateService = inject(RequestStateService);
  router = inject(Router);
  activateRoute = inject(ActivatedRoute);
  fb = inject(FormBuilder);
  form!: FormGroup;
  user!: User;

  error!: string;
  message!: string;
  message2!: string;
  readonly isSubmitting = this.formStateService.isSubmitting;
  readonly successMessage = this.formStateService.successMessage;
  readonly errorMessage = this.formStateService.errorMessage;
  readonly isLoading = this.requestStateService.isLoading;
  readonly requestErrorMessage = this.requestStateService.errorMessage;

  all_technologies: WritableSignal<Technology[]> = signal([]);
  my_technologies:  WritableSignal<Technology[]> = signal([]);
  displayedColumns: string[] = ['product', 'technology', 'responsible', 'countProjects', 'countContributors', 'time'];
  products:         WritableSignal<Product[]> = signal([]);


  ngOnInit(): void {
    this.requestStateService.start();
    this.activateRoute.params.subscribe(params => {
      this.formStateService.reset();
      this.buildForm();
      let id = params['id'];
      if(id){
        this.userService.get(id).subscribe({
          next:(res: User)=> {
            const mappedUser = this.modelMapperService.mapUser(res);
            this.form.get('id')?.setValue(mappedUser.id);
            this.form.get('username')?.setValue(mappedUser.username);
            this.form.get('password')?.setValue(mappedUser.password);
            this.form.get('name')?.setValue(mappedUser.name);
            this.form.get('lastname')?.setValue(mappedUser.lastname);
            this.form.get('email')?.setValue(mappedUser.email);
            this.form.get('countProducts')?.setValue(mappedUser.countProducts);
            this.form.get('countProjects')?.setValue(mappedUser.countProjects);
            this.form.get('time')?.setValue(mappedUser.time);
            this.my_technologies.set(mappedUser.technologies);

            forkJoin({
              allTechnologies: this.technologyService.getAll(),
              contributorProducts: this.productService.getByContributor(mappedUser.id)
            }).subscribe({
              next: ({ allTechnologies, contributorProducts }) => {
                this.all_technologies.set(this.modelMapperService.mapTechnologyList(allTechnologies as unknown[]));
                this.all_technologies.update((currentTechs) => currentTechs.filter(
                  technology => !mappedUser.technologies.some(selectedTech => selectedTech.id === technology.id)
                ));
                this.products.set(this.modelMapperService.mapProductList(contributorProducts as unknown[]));
                this.requestStateService.finish();
              },
              error: (err: any) => this.requestStateService.setError(err),
            });
          },
          error: (err: any) => this.requestStateService.setError(err)
        });
      }else{
        this.user = new User();
        this.technologyService.getAll()
        .subscribe({
          next: (all_technologies) => {
            this.all_technologies.set(this.modelMapperService.mapTechnologyList(all_technologies as unknown[]));
            this.requestStateService.finish();
          },
          error: (err: any) => this.requestStateService.setError(err)
        });
      }
    });
  }

  private buildForm(){
    this.form = this.fb.group({
      id:             [''],
      username:       ['', [Validators.required, Validators.minLength(4), Validators.maxLength(12)]],
      password:       [''],
      name:           ['', [Validators.required]],
      lastname:       ['', [Validators.required]],
      email:          ['', [Validators.required, Validators.email]],
      countProducts:  [{value: '', disabled: true}, Validators.required],
      countProjects:  [{value: '', disabled: true}, Validators.required],
      time:           [{value: '', disabled: true}, Validators.required]
    });  
  }  

  update(event: Event): void {
    event.preventDefault();
    if(this.form.valid){
      this.formStateService.startSubmit();
      const payload = this.form.getRawValue();
      this.user = User.fromObject(payload);
      this.user.technologies = this.my_technologies();
      this.userService.update(this.user).subscribe({
        next: () => {
          this.formStateService.setSuccess('Usuario actualizado con éxito.');
          this.router.navigateByUrl('/pvt/user');
        },
        error: (err: any) => {
          const message = err.error?.message ?? 'No se pudo completar la solicitud.';
          this.formStateService.setError(message);
          this.error = err.error?.error ?? 'Error desconocido';
          this.message = message;
        },
      });
    }
  }     

  create(event: Event): void {
    event.preventDefault();
    if(this.form.valid){
      this.formStateService.startSubmit();
      const payload = this.form.getRawValue();
      this.user = User.fromObject(payload);
      this.user.technologies = this.my_technologies();
      this.userService.create(this.user).subscribe({
        next: () => {
          this.formStateService.setSuccess('Usuario creado con éxito.');
          this.router.navigateByUrl('/pvt/user');
        },
        error: (err: any) => {
          const message = err.error?.message ?? 'No se pudo completar la solicitud.';
          this.formStateService.setError(message);
          this.error = err.error?.error ?? 'Error desconocido';
          this.message = message;
        },
      });
    }    
  } 

  delete(event: Event): void {
    event.preventDefault();
    const id = this.form.get('id')?.value;
    if(id){
      this.formStateService.startSubmit();
      this.userService.delete(id).subscribe({
        next: () => {
          this.formStateService.setSuccess('Usuario eliminado con éxito.');
          this.router.navigateByUrl('/pvt/user');
        },
        error: (err: any) => {
          const message = err.error?.message ?? err.message ?? 'No se pudo completar la solicitud.';
          this.formStateService.setError(message);
          this.error = err.name ?? 'Error desconocido';
          this.message = message;
        },
      });
    }
  }

}