import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { MatInputModule } from '@angular/material/input';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

import { User } from '@core/model/user';
import { AuthService } from '@core/auth/auth.service';
import { UserService } from '@features/user/data/user.service';
import { FormStateService } from '@core/ui/form-state.service'; // Asegúrate de que esta ruta sea la correcta

@Component({
  selector: 'app-login',
  imports: [
    MatCardModule, 
    ReactiveFormsModule, 
    MatInputModule, 
    MatButtonModule, 
    MatIconModule
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent implements OnInit {

  // Inyección funcional moderna con modificadores óptimos y encapsulados
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly userService = inject(UserService);
  private readonly formStateService = inject(FormStateService);
  private readonly router = inject(Router);

  formGroup!: FormGroup;
  user!: User;
  showPassword = false;

  // Vinculamos el Signal de carga con el botón del HTML para mitigar dobles clicks
  readonly isSubmitting = this.formStateService.isSubmitting;

  ngOnInit(): void {
    // Inicializamos barriendo residuos de alertas antiguas
    this.formStateService.clear();

    this.formGroup = this.formBuilder.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required]]
    });
  }

  submit(event: Event): void {
    event.preventDefault();
    
    if (this.formGroup.valid) {
      this.formStateService.startSubmit();
      this.user = this.formGroup.value;

      this.authService.login(this.user).subscribe({
        next: (res: any) => {
          this.authService.saveUser(res.token);
          this.authService.saveToken(res.token);
          
          this.userService.getMe().subscribe({
            next: (profile: User) => {
              this.authService.updateUser(profile);
              this.formStateService.clear();
              this.router.navigateByUrl('/pvt');
            },
            error: () => this.formStateService.reset()
          });
        },
        error: (err: any) => {
          // Extraemos de forma segura el mensaje estructurado que envía tu Spring Boot 
          const backendMessage = err?.error?.message || err?.error || 'Usuario o contraseña incorrectos.';
          
          // Al invocar setError, saltará mágicamente tu nuevo Snackbar rojo flotante
          this.formStateService.setError(backendMessage);
        }
      });
    }
  }

  visibility(): void {
    this.showPassword = !this.showPassword;
  }
}
