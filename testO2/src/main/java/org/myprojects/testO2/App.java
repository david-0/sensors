package org.myprojects.testO2;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class App {

	private static final Logger logger = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) throws UnsupportedBusNumberException, IOException, InterruptedException {
		final I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
		I2CDevice device = bus.getDevice(0x04);
		while (true) {
			int readValue = readValue(device);
			logger.info("Value: {}, calibrated: {}", readValue, readValue / 84.0);
			Thread.sleep(100);
		}
	}

	public static int readValue(I2CDevice device) {
		byte[] data = new byte[2];
		try {
			device.read(0x10, data, 0, 2);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		logger.info("byte[0]: {}, byte[1]: {}", Integer.toBinaryString(data[0] & 0xFF),
				Integer.toBinaryString(data[1] & 0xFF));
		// Convert the data to 12-bits
		return ((data[1] & 0x0F) * 256 + (data[0] & 0xFF));
	}
}
