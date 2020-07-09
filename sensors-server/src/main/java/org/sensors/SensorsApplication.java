package org.sensors;

import org.sensors.backend.SensorApp;
import org.sensors.backend.StateStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@SpringBootApplication
public class SensorsApplication {

	public static void main(String[] args) {
//		StateStore stateStore = new StateStore();
//		SensorApp app = new SensorApp();
//		app.init(stateStore);
//		app.start();
		SpringApplication.run(SensorsApplication.class, args);
//		app.stop();
	}
	

	@Bean
	@Scope("singleton")
	public StateStore stateStore() {
		return new StateStore();
	}
	
	@Autowired
	StateStore stateStore;

	@Bean
	@Scope("singleton")
	public SensorApp sensorApp() {
		 SensorApp sensorApp = new SensorApp();
		 sensorApp.init(stateStore);
		 sensorApp.start();
		 return sensorApp;
	}
}
