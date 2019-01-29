package org.sensors.backend;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class App {
	public static void main(String[] args)
			throws UnsupportedBusNumberException, IOException, InterruptedException, ExecutionException {
		I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
		SensorMcp9808 sensor1 = new SensorMcp9808(bus, 0x18, "Controller");
		SensorMcp9808 sensor2 = new SensorMcp9808(bus, 0x1C, "Unbekannt");

		StopWatch watch = StopWatch.createStarted();
		AsyncCombiner.allOf(sensor1::init, sensor2::init).get();
		watch.stop();
		System.out.printf("Init duration: %d ms %n", watch.getTime(TimeUnit.MILLISECONDS));
		

		System.out.printf("Temperature (%s) in Celsius is : %.2f C %n", sensor1.getDescription(), sensor1.readTemperature());
		System.out.printf("Temperature (%s) in Celsius is : %.2f C %n", sensor2.getDescription(), sensor2.readTemperature());
	}
}
