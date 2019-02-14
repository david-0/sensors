package org.sensors.backend.device;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.sensors.backend.sensor.handler.IntervalBasedSource;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;

public class SensorMe2O2 implements IntervalBasedSource {

	private I2CBus bus;
	private I2CDevice device;
	private int address;
	private boolean initialized;
	private String description;
	private Duration interval;
	private String id;
	private Consumer<Duration> intervalChangeListener;
	private double voltageCalibration;

	public SensorMe2O2(I2CBus bus, int address, String id, String description, double voltageCalibration) {
		this(bus, address, id, description, Duration.ofMillis(5000), voltageCalibration);
	}

	public SensorMe2O2(I2CBus bus, int address, String id, String description, Duration defaultInterval,
			double voltageCalibration) {
		this.bus = bus;
		this.address = address;
		this.interval = defaultInterval;
		this.id = id;
		this.description = description;
		this.voltageCalibration = voltageCalibration;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String getId() {
		return id;
	}

	public SensorMe2O2 init() {
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

	public Double readConcetration() {
		byte[] data = new byte[2];
		try {
			device.read(0x00, data, 0, 2);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return Double.valueOf(((data[1] & 0x0F) * 256 + (data[0] & 0xFF)) / voltageCalibration);
	}

	@Override
	public Supplier<?> getDataProvider() {
		return this::readConcetration;
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
		} else if (key.equals(id + "-voltageCalibration")) {
			voltageCalibration = Double.parseDouble(value);
			return true;
		}
		return false;
	}

	@Override
	public void setIntervalChangeListener(Consumer<Duration> listener) {
		this.intervalChangeListener = listener;
	}
}
