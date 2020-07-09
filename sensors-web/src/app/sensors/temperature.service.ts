import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export class TemperatureService {

  constructor(private url: String, private http: HttpClient) {
  }

  public findLast(): Observable<number> {
    return this.http.get<number>(this.url+"/value");
  }
}
