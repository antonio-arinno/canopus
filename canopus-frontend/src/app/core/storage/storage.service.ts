import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class StorageService {
  private readonly storage = globalThis.sessionStorage;

  get<T>(key: string): T | null {
    const rawValue = this.storage.getItem(key);

    if (!rawValue) {
      return null;
    }

    try {
      return JSON.parse(rawValue) as T;
    } catch {
      return rawValue as T;
    }
  }

  set<T>(key: string, value: T): void {
    this.storage.setItem(key, typeof value === 'string' ? value : JSON.stringify(value));
  }

  remove(key: string): void {
    this.storage.removeItem(key);
  }
}
