import { Component, OnInit } from '@angular/core';
import {Temperature3Service} from "../temperature3.service";

@Component({
  selector: 'app-temperature3-sensor',
  templateUrl: './temperature3-sensor.component.html',
  styleUrls: ['./temperature3-sensor.component.scss']
})
export class Temperature3SensorComponent implements OnInit {
  public temp: number;

  constructor(public service: Temperature3Service) { }

  ngOnInit(): void {
    this.service.findLast().subscribe( v => {
      this.temp = v;
    });
  }
}
