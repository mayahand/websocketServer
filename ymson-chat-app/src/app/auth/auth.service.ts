import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {Observable} from "rxjs";
import {environment} from "../../environments/environment";

@Injectable()
export class AuthService {

  constructor(private http: HttpClient) { }

  getToken (userId: string, password: string): Observable<string> {
    return this.http.post<string>(`${environment.chatApiDomain}/auth/token`, {},
      {
        params:{
          userId: userId,
          password: password
        },
        withCredentials:true
      });
  }
}
