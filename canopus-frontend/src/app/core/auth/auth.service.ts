import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { User } from '@core/model/user';
import { environment } from '@environments/environment';
import { StorageService } from '@core/storage/storage.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly storage = inject(StorageService);
  private readonly http = inject(HttpClient);

  private _user?: User;
  private _token?: string;

  public get user(): User {
    if (this._user) {
      return this._user;
    }

    const storedUser = this.storage.get<User>('user');
    if (storedUser) {
      this._user = storedUser;
      return this._user;
    }

    return new User();
  }

  public get token(): string {
    if (this._token) {
      return this._token;
    }

    const storedToken = this.storage.get<string>('token');
    if (storedToken) {
      this._token = storedToken;
      return this._token;
    }

    return null!;
  }

  login(user: User) {
    const username = user.username;
    const password = user.password;
    const url = `${environment.apiUrl}/login`;

    return this.http.post<any>(url, { username, password });
  }

  saveUser(accessToken: string): void {
    const payload = this.getTokenData(accessToken);
    const user = new User();
    user.username = payload.username;
    user.roles = this.getRoles(payload.authorities);

    this._user = user;
    this.storage.set('user', user);
  }

  private getRoles(authorities: unknown): string[] {
    if (Array.isArray(authorities)) {
      return authorities.map(authority =>
        typeof authority === 'string' ? authority : authority?.authority
      ).filter((role): role is string => typeof role === 'string');
    }

    if (typeof authorities === 'string') {
      try {
        return this.getRoles(JSON.parse(authorities));
      } catch {
        return [authorities];
      }
    }

    return [];
  }

  updateUser(user: User): void {
    const storedUser = this.storage.get<User>('user');
    if (!storedUser) {
      return;
    }

    this._user = { ...storedUser, ...user };
    this.storage.set('user', this._user);
  }

  getTokenData(accessToken: string): any {
    if (accessToken) {
      const payloadBase64 = accessToken.split('.')[1];
      return JSON.parse(atob(payloadBase64));
    }

    return null;
  }

  saveToken(accessToken: string): void {
    this._token = accessToken;
    this.storage.set('token', accessToken);
  }

  isAuthenticated(): boolean {
    const payload = this.getTokenData(this.token);
    return Boolean(payload?.username?.length);
  }

  isTokenExpired(): boolean {
    const token = this.token;
    const payload = this.getTokenData(token);
    const now = Date.now() / 1000;

    return Boolean(payload?.exp && payload.exp < now);
  }

  logout(): void {
    this._token = undefined;
    this._user = undefined;
    this.storage.remove('token');
    this.storage.remove('user');
  }
}
