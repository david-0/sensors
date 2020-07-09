import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { TemperatureHumidity } from './model/temperaturehumidity';
import { TemperatureService } from './temperature.service';

@Injectable({
  providedIn: 'root'
})
@Injectable()
export class Temperature3Service extends TemperatureService {

  constructor(http: HttpClient) {
    super('http://192.168.43.194:8080/t3', http);
  }
}
