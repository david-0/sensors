package org.sensors.backend.sensor;

import java.util.List;

import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.component.temperature.impl.TmpDS18B20DeviceType;
import com.pi4j.io.w1.W1Device;
import com.pi4j.io.w1.W1Master;

public class SensorOneWireTemp {

	private TemperatureSensor tempSensor;
	private W1Device device;
	private String id;
	private String sensorId;
	private String description;

	public SensorOneWireTemp(W1Master w1Master, String sensorId, String id,
			String description) {
		this.sensorId = sensorId;
		this.id = id;
		this.description = description;
		List<W1Device> w1Devices = w1Master
				.getDevices(TmpDS18B20DeviceType.FAMILY_CODE);
		for (W1Device device : w1Devices) {
			if (device.getId().trim().equals(sensorId)) {
				this.device = device;
				this.tempSensor = (TemperatureSensor) device;
			}
		}
		if (this.device == null) {
			throw new RuntimeException("device with '" + sensorId
					+ "' konnte nicht gefunden werden.");
		}
	}

	public Float getTemperature() {
		return Float.valueOf((float) tempSensor.getTemperature());
	}

	public String getId() {
		return id;
	}

	public String getSensorId() {
		return sensorId;
	}

	public String getDescription() {
		return description;
	}
}
