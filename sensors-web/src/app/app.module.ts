import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ServiceWorkerModule } from '@angular/service-worker';
import { environment } from '../environments/environment';
import {HttpClientModule} from "@angular/common/http";
import { Temperature1SensorComponent } from './sensors/temperature1-sensor/temperature1-sensor.component';
import { Temperature2SensorComponent } from './sensors/temperature2-sensor/temperature2-sensor.component';
import { Temperature3SensorComponent } from './sensors/temperature3-sensor/temperature3-sensor.component';
import { Temperature4SensorComponent } from './sensors/temperature4-sensor/temperature4-sensor.component';
import { Temperature5SensorComponent } from './sensors/temperature5-sensor/temperature5-sensor.component';

@NgModule({
  declarations: [
    AppComponent,
    Temperature1SensorComponent,
    Temperature2SensorComponent,
    Temperature3SensorComponent,
    Temperature4SensorComponent,
    Temperature5SensorComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    ServiceWorkerModule.register('ngsw-worker.js', { enabled: environment.production })
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
