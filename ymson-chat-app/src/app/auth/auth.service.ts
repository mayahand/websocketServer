import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {Observable} from "rxjs";

@Injectable()
export class AuthService {

  private host: string = 'http://localhost:9101';

  constructor(private http: HttpClient) { }

  getToken (userId: string, password: string): Observable<string> {
    return this.http.post<string>(`${this.host}/auth/token`, {},
      {
        params:{
          userId: userId,
          password: password
        },
        withCredentials:true
      });
  }
}
