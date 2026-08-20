import { Injectable } from '@angular/core';
import { Imputation } from '@core/model/imputation';

interface CalendarDayView {
  name: string;
  value: number;
  indexWeek: number;
  total: number;
  color: string | null;
  id: number | null;
}

@Injectable({
  providedIn: 'root'
})
export class ImputationCalendarService {

  getMonthCursor(month: number, year: number): Date {
    return new Date(year, month - 1, 1);
  }

  private isoWeekday(date: Date): number {
    const day = date.getDay();
    return day === 0 ? 7 : day;
  }

  private toCalendarDateKey(date: Date): string {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  }

  private findImputationForDay(dayKey: string, imputations: Imputation[]): Imputation | undefined {
    return imputations.find((imputation) => this.toCalendarDateKey(new Date(imputation.date)) === dayKey);
  }

  buildDaysFromDate(month: number, year: number, imputations: Imputation[]): CalendarDayView[] {
    const numberDays = new Date(year, month, 0).getDate();

    return Array.from({ length: numberDays }, (_, index) => {
      const day = index + 1;
      const dayObject = new Date(year, month - 1, day);
      const dayKey = this.toCalendarDateKey(dayObject);
      const matchingImputation = this.findImputationForDay(dayKey, imputations);

      return {
        name: dayObject.toLocaleDateString(undefined, { weekday: 'long' }),
        value: day,
        indexWeek: this.isoWeekday(dayObject),
        total: matchingImputation?.time ?? 0,
        color: matchingImputation?.time === 8 ? '#4caf50' : null,
        id: matchingImputation?.id ?? null
      };
    });
  }
}
