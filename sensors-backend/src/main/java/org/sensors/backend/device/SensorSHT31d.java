package org.sensors.backend.device;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.sensors.backend.sensor.handler.IntervalBasedSource;
import org.sensors.backend.to.TempHumidity;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;

public class SensorSHT31d implements IntervalBasedSource {

	private boolean initialized;

	private final I2CBus bus;
	private final int address;
	private final String id;
	private final String description;
	private Duration interval;
	private Consumer<Duration> intervalChangeListener;
	private I2CDevice device;

	public SensorSHT31d(I2CBus bus, int address, String id, String description) {
		this(bus, address, id, description, Duration.ofMillis(5000));
	}

	public SensorSHT31d(I2CBus bus, int address, String id, String description, Duration defaultInterval) {
		this.bus = bus;
		this.address = address;
		this.id = id;
		this.description = description;
		this.interval = defaultInterval;
	}

	public SensorSHT31d init() {
		if (initialized) {
			throw new IllegalStateException("Sensor '" + description + "' already initialized");
		}
		try {
			device = bus.getDevice(address);

			// Send high repeatability measurement command
			// Command msb, command lsb
			byte[] config = new byte[2];
			config[0] = (byte) 0x2C;
			config[1] = (byte) 0x06;
			device.write(config, 0, 2);

		} catch (IOException e) {
			throw new RuntimeException("init failed", e);
		}
		initialized = true;
		return this;
	}

	@Override
	public boolean onSettingChange(String key, String value) {
		if (key.equals(id + "-intervalInMs")) {
			setInterval(Duration.ofMillis(Integer.parseInt(value)));
			return true;
		}
		return false;
	}

	public TempHumidity readData() {
		// Read 6 bytes of data
		// temp msb, temp lsb, temp CRC, humidity msb, humidity lsb, humidity CRC
		byte[] data = new byte[6];
		try {
			device.read(data, 0, 6);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		// Convert the data
		double cTemp = ((((data[0] & 0xFF) * 256) + (data[1] & 0xFF)) * 175.0) / 65535.0 - 45.0;
//		double fTemp = ((((data[0] & 0xFF) * 256) + (data[1] & 0xFF)) * 315.0) / 65535.0 - 49.0;
		double humidity = ((((data[3] & 0xFF) * 256) + (data[4] & 0xFF)) * 100.0) / 65535.0;
		return new TempHumidity(cTemp, humidity);
	}

	@Override
	public Supplier<?> getDataProvider() {
		return this::readData;
	}

	@Override
	public String getId() {
		return id;
	}

	private void setInterval(Duration interval) {
		this.interval = interval;
		if (intervalChangeListener != null) {
			intervalChangeListener.accept(interval);
		}
	}

	@Override
	public Duration getInterval() {
		return interval;
	}

	@Override
	public void setIntervalChangeListener(Consumer<Duration> listener) {
		this.intervalChangeListener = listener;
	}
}
