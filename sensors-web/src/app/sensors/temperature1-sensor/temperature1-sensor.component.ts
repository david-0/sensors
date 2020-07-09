import { Component, OnInit } from '@angular/core';
import {Temperature1Service} from "../temperature1.service";

@Component({
  selector: 'app-temperature1-sensor',
  templateUrl: './temperature1-sensor.component.html',
  styleUrls: ['./temperature1-sensor.component.scss']
})
export class Temperature1SensorComponent implements OnInit {

  public temp: number;

  constructor(public service: Temperature1Service) { }

  ngOnInit(): void {
    this.service.findLast().subscribe( v => {
      this.temp = v;
    });
  }
}
