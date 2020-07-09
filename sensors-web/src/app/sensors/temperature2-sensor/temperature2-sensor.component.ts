import { Component, OnInit } from '@angular/core';
import {Temperature1Service} from "../temperature1.service";
import {Temperature2Service} from "../temperature2.service";

@Component({
  selector: 'app-temperature2-sensor',
  templateUrl: './temperature2-sensor.component.html',
  styleUrls: ['./temperature2-sensor.component.scss']
})
export class Temperature2SensorComponent implements OnInit {
  public temp: number;

  constructor(public service: Temperature2Service) { }

  ngOnInit(): void {
    this.service.findLast().subscribe( v => {
      this.temp = v;
    });
  }
}
