package org.myprojects.testIna219;

import java.io.IOException;

import org.sensors.backend.ina219.SensorIna219;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class App {
	private static final Logger logger = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) throws UnsupportedBusNumberException, IOException, InterruptedException {
		I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
		SensorIna219 sensor = new SensorIna219(bus, 0x40, "test 1");
		sensor.init();
		int config = sensor.readConfig();
		logger.info("LED Ina219: config: " + String.format("%X", config));
		while (true) {
			Float busVoltage = sensor.readBusVoltageInW();
			Float shuntVoltage = sensor.readShuntVoltageInV();
			Float current = sensor.readCurrentInI();
			Float power = sensor.readPowerInW();
//			logger.info("Vb: {}V, Vs: {}V, P: {}W, I: {}", busVoltage, shuntVoltage, String.format("%.02f", power),
//					String.format("%.02f", current));
			System.out.println("Vb: " + String.format("%.02f", busVoltage) + "V, Vs: "
					+ String.format("%.02f", shuntVoltage) + "V, P: " + String.format("%.02f", power) + "W, I: "
					+ String.format("%.02f", current) + "A");
			Thread.sleep(5000);
		}
	}
}
