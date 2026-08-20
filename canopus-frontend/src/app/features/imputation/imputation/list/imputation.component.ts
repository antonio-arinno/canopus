import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';

import { Router } from '@angular/router';
import { ImputationService } from '@features/imputation/data/imputation.service';
import { ImputationCalendarService } from '@features/imputation/data/imputation-calendar.service';
import { Imputation } from '@core/model/imputation';
import { RequestStateService } from '@core/ui/request-state.service';



@Component({
  selector: 'app-imputation',
  imports: [CommonModule, MatCardModule],
  templateUrl: './imputation.component.html',
  styleUrl: './imputation.component.scss'
})
export class ImputationComponent {

  imputationService = inject(ImputationService);
  imputationCalendarService = inject(ImputationCalendarService);
  requestStateService = inject(RequestStateService);
  router = inject(Router);

  week: any = [
    "Lunes",
    "Martes",
    "Miércoles",
    "Jueves",
    "Viernes",
    "Sábado",
    "Domingo"
  ];

  monthSelect!: any[];
  dateSelect: any;
  dateValue: any;

  imputations: Imputation[] = [];
  readonly isLoading = this.requestStateService.isLoading;
  readonly errorMessage = this.requestStateService.errorMessage;
  readonly emptyMessage = 'No hay imputaciones registradas.';
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
        const today = new Date(Date.now());
        this.getDaysFromDate(today.getMonth() + 1, today.getFullYear());
        this.requestStateService.finish();
      },
      error: (err: any) => this.requestStateService.setError(err),
    });
  }

  getDaysFromDate(month: number, year: number) {
    this.dateSelect = this.imputationCalendarService.getMonthCursor(month, year);
    this.monthSelect = this.imputationCalendarService.buildDaysFromDate(month, year, this.imputations);
  }

  changeMonth(flag: number) {
    if (flag < 0) {
      const prevDate = new Date(this.dateSelect.getFullYear(), this.dateSelect.getMonth() - 1, 1);
      this.getDaysFromDate(prevDate.getMonth() + 1, prevDate.getFullYear());
    } else {
      const nextDate = new Date(this.dateSelect.getFullYear(), this.dateSelect.getMonth() + 1, 1);
      this.getDaysFromDate(nextDate.getMonth() + 1, nextDate.getFullYear());
    }
  }

  clickDay(day: { value: any; id:any}) {
    if(day.id != null){
      this.router.navigate(['/pvt/imputation/detail', day.id])
    }else{
      const monthYear = `${this.dateSelect.getFullYear()}-${String(this.dateSelect.getMonth() + 1).padStart(2, '0')}`;
      const parse = `${monthYear}-${day.value.toString().padStart(2, '0')}`;
      this.router.navigate(['/pvt/imputation/detail', parse]);
    }
  }

  onDaySpace(event: Event, day: { value: any; id: any }): void {
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

  isToday(day: { value: number }): boolean {
    return this.dateSelect?.getFullYear() === this.today.getFullYear()
      && this.dateSelect?.getMonth() === this.today.getMonth()
      && day.value === this.today.getDate();
  }

  getDayTotalLabel(day: { total: number }): string {
    return day.total ? `${day.total} h` : 'Sin imputar';
  }

  getDayAriaLabel(day: { value: number; total: number; id: number | null }): string {
    const totalText = day.total ? `${day.total} horas imputadas` : 'sin imputación';
    const actionText = day.id ? 'Abrir imputación del día' : 'Crear imputación del día';
    return `Día ${day.value}, ${totalText}. ${actionText}.`;
  }

  hasItems(): boolean {
    return this.imputations.length > 0;
  }

}

