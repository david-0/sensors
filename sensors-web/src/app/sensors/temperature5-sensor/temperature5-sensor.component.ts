import { Component, OnInit } from '@angular/core';
import {Temperature5Service} from "../temperature5.service";

@Component({
  selector: 'app-temperature5-sensor',
  templateUrl: './temperature5-sensor.component.html',
  styleUrls: ['./temperature5-sensor.component.scss']
})
export class Temperature5SensorComponent implements OnInit {
  public temp: number;

  constructor(public service: Temperature5Service) {
  }

  ngOnInit(): void {
    this.service.findLast().subscribe(v => {
      this.temp = v;
    });
  }
}
