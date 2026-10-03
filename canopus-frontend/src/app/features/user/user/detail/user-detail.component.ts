import { Component, inject, OnInit, signal, WritableSignal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';

import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatTableModule } from '@angular/material/table';
import { CdkDragDrop, moveItemInArray, transferArrayItem, CdkDrag, CdkDropList } from '@angular/cdk/drag-drop';

import { User } from '@core/model/user';
import { Technology } from '@core/model/technology';
import { Product } from '@core/model/product';
import { UserService } from '@features/user/data/user.service';
import { TechnologyService } from '@features/technology/data/technology.service';
import { ProductService } from '@features/product/data/product.service';
import { ModelMapperService } from '@core/model/model-mapper.service';
import { FormStateService } from '@core/ui/form-state.service';
import { RequestStateService } from '@core/ui/request-state.service';
import { AuthService } from '@core/auth/auth.service';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-user-detail',
  imports: [
    CommonModule, 
    MatCardModule, 
    ReactiveFormsModule, 
    MatInputModule, 
    MatAutocompleteModule, 
    MatButtonModule, 
    CdkDropList, 
    CdkDrag, 
    MatGridListModule, 
    MatTableModule,
    MatIconModule // <-- AGREGAR ESTA IMPORTACIÓN AQUÍ
  ],
  templateUrl: './user-detail.component.html',
  styleUrl: './user-detail.component.scss'
})
export class UserDetailComponent implements OnInit {

  private readonly userService = inject(UserService);
  private readonly technologyService = inject(TechnologyService);
  private readonly productService = inject(ProductService);
  private readonly modelMapperService = inject(ModelMapperService);
  private readonly formStateService = inject(FormStateService);
  private readonly requestStateService = inject(RequestStateService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly activateRoute = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);

  form!: FormGroup;
  user!: User;
  isProfile = false;
  
      // Atributos reactivos expuestos al HTML
  readonly isSubmitting = this.formStateService.isSubmitting;
  readonly isLoading = this.requestStateService.isLoading;
  readonly showPasswordForm = signal(false);


  all_technologies: WritableSignal<Technology[]> = signal([]);
  my_technologies:  WritableSignal<Technology[]> = signal([]);
  products:         WritableSignal<Product[]> = signal([]);
  
  displayedColumns: string[] = ['product', 'technology', 'responsible', 'countProjects', 'countContributors', 'time'];
  showPassword = false;

  togglePasswordForm(): void {
    this.showPasswordForm.update(value => !value);
  }

  get isAdmin(): boolean {
    const roles = this.authService.user.roles;
    return Array.isArray(roles) && roles.includes('ROLE_ADMIN');
  }

  ngOnInit(): void {

    this.requestStateService.start();
    this.formStateService.clear();


    this.activateRoute.params.subscribe(params => {
      this.buildForm();
      const id = params['id'];
      this.isProfile = this.activateRoute.snapshot.routeConfig?.path === 'profile';
      
      if (this.isProfile) {
        this.userService.getMe().subscribe({
          next: (res: User) => this.loadUser(res),
          error: () => this.requestStateService.finish()
        });
      } else if (id) {
        this.userService.get(id).subscribe({
          next: (res: User) => this.loadUser(res),
          error: () => this.requestStateService.finish()
        });
      } else {
        this.user = new User();
        this.technologyService.getAll().subscribe({
          next: (all_technologies) => {
            this.all_technologies.set(this.modelMapperService.mapTechnologyList(all_technologies as unknown[]));
            this.requestStateService.finish();
          },
          error: () => this.requestStateService.finish()
        });
      }
    });
  }

  private loadUser(res: User): void {
    const mappedUser = this.modelMapperService.mapUser(res);
    this.form.patchValue({
      id: mappedUser.id,
      username: mappedUser.username,
      name: mappedUser.name,
      lastname: mappedUser.lastname,
      email: mappedUser.email,
      countProducts: mappedUser.countProducts,
      countProjects: mappedUser.countProjects,
      time: mappedUser.time
    });
    
    if (this.isProfile) {
      this.form.get('username')?.disable();
    }
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
      error: () => this.requestStateService.finish()
    });
  }

  private buildForm(): void {
    this.form = this.fb.group({
      id: [''],
      username: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(12)]],
      password: [''],
      currentPassword: [''],
      newPassword: ['', [Validators.minLength(8), Validators.maxLength(72)]],
      name: ['', [Validators.required]],
      lastname: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      countProducts: [{value: '', disabled: true}, Validators.required],
      countProjects: [{value: '', disabled: true}, Validators.required],
      time: [{value: '', disabled: true}, Validators.required]
    });  
  }  

  update(event: Event): void {
    event.preventDefault();
    if (this.form.valid) {
      this.formStateService.startSubmit();
      const payload = this.form.getRawValue();
      
      this.userService.updateMe({
        name: payload.name,
        lastname: payload.lastname,
        email: payload.email,
        technologies: this.my_technologies().map(technology => technology.id)
      }).subscribe({
        next: () => {
          this.formStateService.setSuccess('Usuario actualizado con éxito.');
          this.router.navigateByUrl('/pvt/user');
        },
        error: (err: any) => {
          this.handleFormError(err, 'No tienes permisos para realizar esta acción.');
        }
      });
    }
  }     

  changePassword(event: Event): void {
    event.preventDefault();
    const currentPassword = this.form.get('currentPassword')?.value;
    const newPassword = this.form.get('newPassword')?.value;
    
    if (!currentPassword || !newPassword || this.form.get('newPassword')?.invalid) {
      this.formStateService.setError('Indica la contraseña actual y una nueva de al menos 8 caracteres.');
      return;
    }

    this.formStateService.startSubmit();
    this.userService.changePassword(currentPassword, newPassword).subscribe({
      next: () => {
        this.form.get('currentPassword')?.reset();
        this.form.get('newPassword')?.reset();
        this.showPasswordForm.set(false);
        this.formStateService.setSuccess('Contraseña actualizada con éxito.');
      },
      error: () => this.formStateService.reset()
    });
  }

  create(event: Event): void {
    event.preventDefault();
    if (this.form.valid) {
      this.formStateService.startSubmit();
      const payload = this.form.getRawValue();
      this.user = User.fromObject(payload);
      this.userService.create({
        id: this.user.id,
        username: this.user.username,
        name: this.user.name,
        lastname: this.user.lastname,
        email: this.user.email,
        admin: payload.admin ?? false,
        technologies: this.my_technologies().map(technology => technology.id)
      }).subscribe({
        next: () => {
          this.formStateService.setSuccess('Usuario creado con éxito.');
          this.router.navigateByUrl('/pvt/user');
        },
        error: (err: any) => {
          this.handleFormError(err, 'No se pudo completar la solicitud debido a restricciones del sistema.');
        }
      });
    }    
  } 

  delete(event: Event): void {
    event.preventDefault();
    const id = this.form.get('id')?.value;
    if (id) {
      this.formStateService.startSubmit();
      this.userService.delete(id).subscribe({
        next: () => {
          this.formStateService.setSuccess('Usuario eliminado con éxito.');
          this.router.navigateByUrl('/pvt/user');
        },
        error: (err: any) => {
          this.handleFormError(err, 'No se pudo eliminar debido a dependencias en el sistema.');
        }
      });
    }
  }

  // 🔥 FACTOR COMÚN: Método centralizado para procesar fallos de la API en este formulario
  private handleFormError(err: any, fallbackMessage: string): void {
    if (err?.status === 403) {
      this.formStateService.setError('No tienes permisos para realizar esta acción.');
    } else {
      const backendMessage = err?.error?.message || fallbackMessage;
      this.formStateService.setError(backendMessage);
    }
  }




  drop(event: CdkDragDrop<Technology[]>): void {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex
      );
    }
  } 
}
