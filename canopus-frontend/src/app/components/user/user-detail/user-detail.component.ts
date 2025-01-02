import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { ActivatedRoute, Router } from '@angular/router';
import { User } from '@core/model/user';
import { UserService } from '@core/services/user.service';

@Component({
  selector: 'app-user-detail',
  imports: [CommonModule, MatCardModule, ReactiveFormsModule, MatInputModule, MatAutocompleteModule, MatButtonModule],
  templateUrl: './user-detail.component.html',
  styleUrl: './user-detail.component.scss'
})
export class UserDetailComponent {

  userService = inject(UserService);
  router = inject(Router);
  activateRoute = inject(ActivatedRoute);
  fb = inject(FormBuilder);
  form!: FormGroup;
  user!: User;

    
  error!: string;
  message!: string;
  message2!: string;

  ngOnInit(): void {
    this.activateRoute.params.subscribe(params => {
      this.buildForm();
      let id = params['id']
      if(id){
        this.userService.get(id).subscribe({
          next:(res: User)=> {
            this.form.get('id')?.setValue(res.id);
            this.form.get('username')?.setValue(res.username);
            this.form.get('password')?.setValue(res.password);
            this.form.get('name')?.setValue(res.name);
            this.form.get('lastname')?.setValue(res.lastname);
            this.form.get('email')?.setValue(res.email);
          },
          error: (err: any) => console.log(err)
        });
      }else{
        this.user = new User();
      }
    });
  }

  private buildForm(){
    this.form = this.fb.group({
      id: [''],
      username: [''],
      password: [''],
      name: ['', [Validators.required]],
      lastname: ['', [Validators.required]],
      email: ['', [Validators.required]]
    });  
  }  

  update(event: Event): void {
  event.preventDefault();
  if(this.form.valid){
    this.user = this.form.value;
    console.log(this.user);
    this.userService.update(this.user).subscribe({
      next: (res: any) => {
        this.router.navigateByUrl('/pvt/user');
      },
      error: (err: any) => {
        this.error = err.error.error;
        this.message = err.error.message;
        this.message2 = err.message;
      },
    });
    }
  }     

  create(event: Event): void {
    event.preventDefault();
    if(this.form.valid){
      this.user = this.form.value;
      this.userService.create(this.user).subscribe({
        next: (res: any) => {
          this.router.navigateByUrl('/pvt/user');
        },
        error: (err: any) => {
          this.error = err.error.error;
          this.message = err.error.message;
          this.message2 = err.message;
        },
      });
    }    
  } 

  delete(event: Event): void {
    event.preventDefault();
    if(this.form.valid){
      this.user = this.form.value;
      this.userService.delete(this.user.id).subscribe({
        next: (res: any) => {
          this.router.navigateByUrl('/pvt/user');
        },
        error: (err: any) => {
          this.error = err.error.error;
          this.message = err.error.message;
          this.message2 = err.message;
        },
      });
    }
  }


} 
