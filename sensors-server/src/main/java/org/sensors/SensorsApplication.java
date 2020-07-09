package org.sensors;

import org.sensors.backend.SensorApp;
import org.sensors.backend.StateStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SensorsApplication {

	public static void main(String[] args) {
		StateStore stateStore = new StateStore();
		SensorApp app = new SensorApp();
		app.init(stateStore);
		app.start();
		SpringApplication.run(SensorsApplication.class, args);
		app.stop();
	}
}
