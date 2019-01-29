package org.sensors.backend;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
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
		SensorMcp9808 sensor1 = new SensorMcp9808(bus, 0x18, "Controller");
		SensorMcp9808 sensor2 = new SensorMcp9808(bus, 0x1C, "Unbekannt");

		
		logger.info("before commonPool");
		ForkJoinPool commonPool = ForkJoinPool.commonPool();
		logger.info("commonPool # {}", commonPool.getActiveThreadCount());
		logger.info("start init ...");
		logger.info("commonPool # {}", commonPool.getActiveThreadCount());
		StopWatch watch = StopWatch.createStarted();
		Future<Void> allOf = AsyncCombiner.allOf(sensor1::init, sensor2::init);
		logger.info("... in the middle ...");
		logger.info("commonPool # {}", commonPool.getActiveThreadCount());
		allOf.get();
		watch.stop();
		logger.info("... init finished");
		logger.info("commonPool # {}", commonPool.getActiveThreadCount());
		System.out.printf("Init duration: %d ms %n", watch.getTime(TimeUnit.MILLISECONDS));
		

		System.out.printf("Temperature (%s) in Celsius is : %.2f C %n", sensor1.getDescription(), sensor1.readTemperature());
		System.out.printf("Temperature (%s) in Celsius is : %.2f C %n", sensor2.getDescription(), sensor2.readTemperature());
	}
}
