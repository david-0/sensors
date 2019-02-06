package org.sensors.backend.sensor;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;

public class SensorMcp9808 {

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
			throw new IllegalStateException("Sensor '" + description + "' already initialized");
		}
		try {
			device = bus.getDevice(address);
		} catch (IOException e) {
			throw new RuntimeException("init failed", e);
		}
		initialized = true;
	}

	public Float readTemperature()  {
		// Read 2 bytes of data from address 0x05(05)
		// temp msb, temp lsb
		byte[] data = new byte[2];
		try {
			device.read(0x05, data, 0, 2);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// Convert the data to 13-bits
		int temp = ((data[0] & 0x1F) * 256 + (data[1] & 0xFF));
		if (temp > 4095) {
			temp -= 8192;
		}
		return Float.valueOf(temp * 0.0625f);
	}
}