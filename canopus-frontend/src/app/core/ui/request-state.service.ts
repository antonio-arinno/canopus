import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class RequestStateService {
  private readonly loadingSignal = signal(false);
  private readonly errorSignal = signal<string | null>(null);

  readonly isLoading = this.loadingSignal.asReadonly();
  readonly errorMessage = this.errorSignal.asReadonly();

  start(): void {
    this.errorSignal.set(null);
    this.loadingSignal.set(true);
  }

  finish(): void {
    this.loadingSignal.set(false);
  }

  setError(error: unknown): void {
    this.loadingSignal.set(false);
    this.errorSignal.set(this.extractMessage(error));
  }

  clearError(): void {
    this.errorSignal.set(null);
  }

  reset(): void {
    this.loadingSignal.set(false);
    this.errorSignal.set(null);
  }

  private extractMessage(error: unknown): string {
    if (typeof error === 'string') {
      return error;
    }

    if (error instanceof Error) {
      return error.message;
    }

    const candidate = error as { error?: { message?: string }; message?: string };
    return candidate?.error?.message ?? candidate?.message ?? 'Ocurrió un error inesperado.';
  }
}
