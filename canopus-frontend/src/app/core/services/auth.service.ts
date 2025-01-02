import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { User } from '@core/model/user';
import { URL_BACKEND } from '@shared/config';

@Injectable({
  providedIn: 'root'
})

export class AuthService {

  private _user: User | undefined;
  private _token: string | undefined;

  private http = inject(HttpClient);

  public get user() : User{
    if(this._user != null){
      return this._user;
    } else 
      if(this._user == null && sessionStorage.getItem('user') != null){
        this._user = JSON.parse(sessionStorage.getItem('user')!) as User;
        return this._user;
    } 
    return new User();
  }

  public get token(): string{

    if(this._token != null){
      return this._token;
    } else if(this._token == null && sessionStorage.getItem('token') != null){
      this._token = sessionStorage.getItem('token')!;
      return this._token;
    }
    return null!;
  }   

  login(user: User) {
    const username = user.username;
    const password = user.password;
    const url: string = URL_BACKEND + '/login';
    return this.http.post<any>(url, { username, password });
/*
    const urlEndpoint = URL_BACKEND + '/oauth/oauth/token';
    const credentials = btoa('frontendapp:123456');
    const httpHeaders = new HttpHeaders({
      'Content-Type': 'application/x-www-form-urlencoded',
      'Authorization': 'Basic ' + credentials
    });
    let params = new URLSearchParams();
    params.set('grant_type', 'password');

    params.set('username', user.username);
    params.set('password', user.password);

    return this.http.post<any>(urlEndpoint, params.toString(), { headers: httpHeaders });
    */
  }

  
  saveUser(accessToken: string): void {
    let payload = this.getTokenData(accessToken);
    this._user = new User();
    this._user.id = payload.id;
    this._user.name = payload.name;
    this._user.email = payload.email;
    this._user.username = payload.username;
    this._user.roles = payload.authorities;
    this._user.company = payload.company;
    sessionStorage.setItem('user', JSON.stringify(this._user));
  }

  getTokenData(accessToken: string): any {
    if(accessToken != null){
      return JSON.parse(atob(accessToken.split(".")[1]));
    }
    return null;
  }

  saveToken(accessToken: string): void {
    this._token = accessToken;
    sessionStorage.setItem('token', accessToken);
  }

  isAuthenticated():boolean {
    let payload = this.getTokenData(this.token);
    if(payload != null && payload.username && payload.username.length>0){
      return true;
    }
    return false;
  }  

  isTokenExpired(): boolean{
    let token = this.token;
    let payload = this.getTokenData(token);
    let now = new Date().getTime() / 1000;
    if (payload.exp < now) {
      return true;
    }
    return false;
  }  

  logout():void{
    this._token = null!;
    this._user = null!;
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('user');
  }
  
}
