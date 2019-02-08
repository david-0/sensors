package org.sensors.backend.sensor.ina219;

public class Values {

	private final Float busVoltage;
	private final Float power;
	private final Float current;
	private final Float averagePowerPerSec;

	public Values(Float busVoltage, Float power, Float current, Float averagePowerPerSec) {
		this.busVoltage = busVoltage;
		this.power = power;
		this.current = current;
		this.averagePowerPerSec = averagePowerPerSec;
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
	
	public Float getAveragePowerPerSec() {
		return averagePowerPerSec;
	}
}
