import { Injectable, inject, signal } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root'
})
export class FormStateService {
  // Inyección funcional nativa de Angular 19 para el componente de alertas
  private readonly snackBar = inject(MatSnackBar);

  private readonly submittingSignal = signal(false);
  private readonly successSignal = signal<string | null>(null);
  private readonly errorSignal = signal<string | null>(null);

  readonly isSubmitting = this.submittingSignal.asReadonly();
  readonly successMessage = this.successSignal.asReadonly();
  readonly errorMessage = this.errorSignal.asReadonly();

  startSubmit(): void {
    this.successSignal.set(null);
    this.errorSignal.set(null);
    this.submittingSignal.set(true);
  }

  finishSubmit(): void {
    this.submittingSignal.set(false);
  }

  setSuccess(message: string): void {
    this.submittingSignal.set(false);
    this.errorSignal.set(null);
    this.successSignal.set(message);

    // Desplegamos la notificación flotante de éxito (Dura 4 segundos visible)
    this.snackBar.open(message, 'Cerrar', {
      duration: 4000,
      horizontalPosition: 'center',
      verticalPosition: 'bottom',
      panelClass: ['snackbar-success'] // Engancha con tus clases globales en styles.scss
    });
  }

  setError(message: string | null): void {
    this.submittingSignal.set(false);
    this.successSignal.set(null);
    this.errorSignal.set(message);

    // Solo disparamos el snackbar si el mensaje no es nulo
    if (message) {
      // Desplegamos la notificación flotante de error (Dura 5 segundos visible)
      this.snackBar.open(message, 'Cerrar', {
        duration: 5000,
        horizontalPosition: 'center',
        verticalPosition: 'bottom',
        panelClass: ['snackbar-error'] // Engancha con tus clases globales en styles.scss
      });
    }
  }

  clear(): void {
    this.submittingSignal.set(false);
    this.successSignal.set(null);
    this.errorSignal.set(null);
  }

  reset(): void {
    this.clear();
  }
}

