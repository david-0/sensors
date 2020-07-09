import { Component, OnInit } from '@angular/core';
import {Temperature4Service} from "../temperature4.service";

@Component({
  selector: 'app-temperature4-sensor',
  templateUrl: './temperature4-sensor.component.html',
  styleUrls: ['./temperature4-sensor.component.scss']
})
export class Temperature4SensorComponent implements OnInit {
  public temp: number;

  constructor(public service: Temperature4Service) { }

  ngOnInit(): void {
    this.service.findLast().subscribe( v => {
      this.temp = v;
    });
  }
}
