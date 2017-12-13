import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs/Observable';


@Injectable()
export class MarkService {
  constructor(private http: HttpClient) {
  }


  getAllMarks(): Observable<any> {
    return this.http.get('http://localhost:8080/marks');
  }
}
