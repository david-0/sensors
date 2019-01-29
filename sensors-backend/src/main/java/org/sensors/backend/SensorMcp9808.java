package org.sensors.backend;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;

public class SensorMcp9808 {
	
	private static final Logger log = LoggerFactory.getLogger(SensorMcp9808.class); 
	private I2CBus bus;
	private I2CDevice device;
	private int address;
	private boolean initialized;
	private String description;

	public SensorMcp9808(I2CBus bus, int address, String description) {
		this.bus = bus;
		this.address = address;
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}

	public void init() {
		if (initialized) {
			throw new IllegalStateException("Sensor already initialized");
		}
		try {
			log.info("before init: {}", address);
			device = bus.getDevice(address);
			log.info("after init: {}", address);
			Thread.sleep(300);
			log.info("after sleep: {}", address);
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException("init failed", e);
		}
	}

	public double readTemperature() throws IOException {
		// Read 2 bytes of data from address 0x05(05)
		// temp msb, temp lsb
		byte[] data = new byte[2];
		device.read(0x05, data, 0, 2);

		// Convert the data to 13-bits
		int temp = ((data[0] & 0x1F) * 256 + (data[1] & 0xFF));
		if (temp > 4095) {
			temp -= 8192;
		}
		return temp * 0.0625;
	}
}
