package org.sensors.backend;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class App {
	private static final Logger logger = LoggerFactory.getLogger(App.class);
	public static void main(String[] args)
			throws UnsupportedBusNumberException, IOException, InterruptedException, ExecutionException {
		I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
		Controller controller = new Controller(bus);
		controller.init();
		controller.run();
		logger.info("controller started");
		
/*		SensorMcp9808 sensor1 = new SensorMcp9808(bus, 0x18, "Controller");
		SensorMcp9808 sensor2 = new SensorMcp9808(bus, 0x1C, "Unbekannt");
		AsyncCombiner.allOf(sensor1::init, sensor2::init).get();
		logger.info("Sensors initialized");
		
		System.out.printf("Temperature (%s) in Celsius is : %.2f C %n", sensor1.getDescription(), sensor1.readTemperature());
		System.out.printf("Temperature (%s) in Celsius is : %.2f C %n", sensor2.getDescription(), sensor2.readTemperature());*/
	}
}
