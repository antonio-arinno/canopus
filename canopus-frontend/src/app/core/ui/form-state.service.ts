import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class FormStateService {
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
  }

  setError(message: string | null): void {
    this.submittingSignal.set(false);
    this.successSignal.set(null);
    this.errorSignal.set(message);
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
