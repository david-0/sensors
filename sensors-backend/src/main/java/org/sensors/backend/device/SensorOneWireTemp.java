package org.sensors.backend.device;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.sensors.backend.sensor.handler.IntervalBasedSource;

import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.component.temperature.impl.TmpDS18B20DeviceType;
import com.pi4j.io.w1.W1Device;
import com.pi4j.io.w1.W1Master;

public class SensorOneWireTemp implements IntervalBasedSource {

	private TemperatureSensor tempSensor;
	private W1Device device;
	private String id;
	private String sensorId;
	private String description;
	private Duration interval;
	private Consumer<Duration> intervalChangeListener;
	private W1Master w1Master;

	public SensorOneWireTemp(W1Master w1Master, String sensorId, String id, String description) {
		this(w1Master, sensorId, id, description, Duration.ofMillis(5000));
	}

	public SensorOneWireTemp(W1Master w1Master, String sensorId, String id, String description,
			Duration defaultInterval) {
		this.w1Master = w1Master;
		this.sensorId = sensorId;
		this.id = id;
		this.description = description;
		this.interval = defaultInterval;
	}

	public SensorOneWireTemp init() {
		List<W1Device> w1Devices = w1Master.getDevices(TmpDS18B20DeviceType.FAMILY_CODE);
		for (W1Device device : w1Devices) {
			if (device.getId().trim().equals(sensorId)) {
				this.device = device;
				this.tempSensor = (TemperatureSensor) device;
			}
		}
		if (this.device == null) {
			throw new RuntimeException("device with '" + sensorId + "' konnte nicht gefunden werden.");
		}
		return this;
	}

	public Float readTemperature() {
		return Float.valueOf((float) tempSensor.getTemperature());
	}

	@Override
	public String getId() {
		return id;
	}

	public String getSensorId() {
		return sensorId;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public Duration getInterval() {
		return interval;
	}

	@Override
	public Supplier<?> getDataProvider() {
		return this::readTemperature;
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
