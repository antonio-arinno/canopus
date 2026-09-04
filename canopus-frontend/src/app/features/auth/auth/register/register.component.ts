import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '@core/auth/auth.service';
import { FormStateService } from '@core/ui/form-state.service';

@Component({
  selector: 'app-register',
  imports: [MatCardModule, ReactiveFormsModule, MatInputModule, MatButtonModule, MatIconModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent implements OnInit {

  private formBuilder = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  formStateService = inject(FormStateService);

  formGroup!: FormGroup;
  showPassword = false;

  readonly isSubmitting = this.formStateService.isSubmitting;
  readonly successMessage = this.formStateService.successMessage;
  readonly errorMessage = this.formStateService.errorMessage;

  ngOnInit(): void {
    this.formStateService.reset();
    this.formGroup = this.formBuilder.group({
      companyName: ['', [Validators.required, Validators.maxLength(120)]],
      name: ['', [Validators.required]],
      lastname: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      username: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(12)]],
      password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]]
    });
  }

  visibility(): void {
    this.showPassword = !this.showPassword;
  }

  submit(event: Event): void {
    event.preventDefault();
    if (this.formGroup.invalid) {
      return;
    }

    this.formStateService.startSubmit();
    this.authService.register(this.formGroup.value).subscribe({
      next: () => {
        this.formStateService.setSuccess('Empresa creada con éxito. Ya puedes iniciar sesión.');
        setTimeout(() => this.router.navigateByUrl('/auth/login'), 1500);
      },
      error: (err: any) => {
        const message = err.error?.message ?? 'No se pudo completar el registro.';
        this.formStateService.setError(message);
      }
    });
  }
}
