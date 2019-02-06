package org.sensors.backend.sensor.ina219;

public class Values {

	private final Float busVoltage;
	private final Float power;
	private final Float current;

	public Values(Float busVoltage, Float power, Float current) {
		this.busVoltage = busVoltage;
		this.power = power;
		this.current = current;
	}

	public Float getBusVoltage() {
		return busVoltage;
	}

	public Float getPower() {
		return power;
	}

	public Float getCurrent() {
		return current;
	}
}
