import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { Router } from '@angular/router';

import { ImputationService } from '@features/imputation/data/imputation.service';
import { ImputationCalendarService } from '@features/imputation/data/imputation-calendar.service';
import { Imputation } from '@core/model/imputation';
import { RequestStateService } from '@core/ui/request-state.service';

// Definimos una interfaz interna para el tipado estricto de los días del calendario
interface CalendarDay {
  value: number;
  indexWeek: number;
  id: number | null;
  total: number;
}

@Component({
  selector: 'app-app-imputation',
  imports: [CommonModule, MatCardModule],
  templateUrl: './imputation.component.html',
  styleUrl: './imputation.component.scss'
})
export class ImputationComponent implements OnInit {

  // Inyección funcional encapsulada con modificadores de solo lectura
  private readonly imputationService = inject(ImputationService);
  private readonly imputationCalendarService = inject(ImputationCalendarService);
  private readonly requestStateService = inject(RequestStateService);
  private readonly router = inject(Router);

  // Declaración estricta de constantes con strings inmutables
  readonly week: string[] = [
    "Lunes",
    "Martes",
    "Miércoles",
    "Jueves",
    "Viernes",
    "Sábado",
    "Domingo"
  ];

  monthSelect!: CalendarDay[];
  dateSelect!: Date;
  dateValue: unknown;

  imputations: Imputation[] = [];
  
  // Enlaces directos a los Signals de estado reactivo global del Core
  readonly isLoading = this.requestStateService.isLoading;
  readonly errorMessage = this.requestStateService.errorMessage;

  
  private readonly today = new Date();

  ngOnInit(): void {
    this.loadImputations();
  }

  private loadImputations(): void {
    this.requestStateService.start();
    this.imputations = [];

    this.imputationService.getAll().subscribe({
      next: (res: Imputation[]) => {
        this.imputations = res ?? [];
        const todayDate = new Date();
        this.getDaysFromDate(todayDate.getMonth() + 1, todayDate.getFullYear());
        this.requestStateService.finish();
      }
      // NOTA: Callback 'error:' eliminado por completo. 
      // El interceptor global actualiza automáticamente el estado si ocurre un problema HTTP
    });
  }

  getDaysFromDate(month: number, year: number): void {
    this.dateSelect = this.imputationCalendarService.getMonthCursor(month, year);
    this.monthSelect = this.imputationCalendarService.buildDaysFromDate(month, year, this.imputations);
  }

  changeMonth(flag: number): void {
    if (flag < 0) {
      const prevDate = new Date(this.dateSelect.getFullYear(), this.dateSelect.getMonth() - 1, 1);
      this.getDaysFromDate(prevDate.getMonth() + 1, prevDate.getFullYear());
    } else {
      const nextDate = new Date(this.dateSelect.getFullYear(), this.dateSelect.getMonth() + 1, 1);
      this.getDaysFromDate(nextDate.getMonth() + 1, nextDate.getFullYear());
    }
  }

  clickDay(day: CalendarDay): void {
    if (day.id != null) {
      this.router.navigate(['/pvt/imputation/detail', day.id]);
    } else {
      const monthYear = `${this.dateSelect.getFullYear()}-${String(this.dateSelect.getMonth() + 1).padStart(2, '0')}`;
      const parse = `${monthYear}-${day.value.toString().padStart(2, '0')}`;
      this.router.navigate(['/pvt/imputation/detail', parse]);
    }
  }

  onDaySpace(event: Event, day: CalendarDay): void {
    event.preventDefault();
    this.clickDay(day);
  }

  goToCurrentMonth(): void {
    this.getDaysFromDate(this.today.getMonth() + 1, this.today.getFullYear());
  }

  createTodayImputation(): void {
    const parse = `${this.today.getFullYear()}-${String(this.today.getMonth() + 1).padStart(2, '0')}-${String(this.today.getDate()).padStart(2, '0')}`;
    this.router.navigate(['/pvt/imputation/detail', parse]);
  }

  isToday(day: CalendarDay): boolean {
    return this.dateSelect?.getFullYear() === this.today.getFullYear()
      && this.dateSelect?.getMonth() === this.today.getMonth()
      && day.value === this.today.getDate();
  }

  getDayTotalLabel(day: { total: number }): string {
    return day.total ? `${day.total} h` : 'Sin imputar';
  }

  getDayAriaLabel(day: CalendarDay): string {
    const totalText = day.total ? `${day.total} horas imputadas` : 'sin imputación';
    const actionText = day.id ? 'Abrir imputación del día' : 'Crear imputación del día';
    return `Día ${day.value}, ${totalText}. ${actionText}.`;
  }

  hasItems(): boolean {
    return this.imputations.length > 0;
  }
}


