package org.sensors.backend.sensor;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.sensors.backend.sensor.handler.IntervalBasedSource;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;

public class SensorMcp9808 implements IntervalBasedSource {

	private I2CBus bus;
	private I2CDevice device;
	private int address;
	private boolean initialized;
	private String description;
	private Duration interval;
	private String id;
	private Consumer<Duration> intervalChangeListener;

	public SensorMcp9808(I2CBus bus, int address, String id, String description) {
		this(bus, address, id, description, Duration.ofMillis(5000));
	}

	public SensorMcp9808(I2CBus bus, int address, String id, String description, Duration defaultInterval) {
		this.bus = bus;
		this.address = address;
		this.interval = defaultInterval;
		this.id = id;
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String getId() {
		return id;
	}

	public SensorMcp9808 init() {
		if (initialized) {
			throw new IllegalStateException("Sensor '" + description + "' already initialized");
		}
		try {
			device = bus.getDevice(address);
		} catch (IOException e) {
			throw new RuntimeException("init failed", e);
		}
		initialized = true;
		return this;
	}

	public Float readTemperature() {
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

	@Override
	public Supplier<?> getDataProvider() {
		return this::readTemperature;
	}

	@Override
	public Duration getInterval() {
		return interval;
	}

	private void setInterval(Duration interval) {
		this.interval = interval;
		if (intervalChangeListener != null) {
			intervalChangeListener.accept(interval);
		}
	}

	@Override
	public boolean onSettingChange(String key, String value) {
		if (key.equals(id + "-intervalInMs")) {
			setInterval(Duration.ofMillis(Integer.parseInt(value)));
			return true;
		}
		return false;
	}

	@Override
	public void setIntervalChangeListener(Consumer<Duration> listener) {
		this.intervalChangeListener = listener;
	}
}
