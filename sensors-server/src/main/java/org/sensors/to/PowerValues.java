package org.sensors.to;

public class PowerValues {

	private final Float busVoltage;
	private final Float power;
	private final Float current;
	private final Float averagePowerPerSec;

	public PowerValues(Float busVoltage, Float power, Float current, Float averagePowerPerSec) {
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
	
	@Override
	public String toString() {
		return  format(busVoltage)+ "V, "+format(current) +"A, "+format(power)+"W, "+format(averagePowerPerSec)+"W/s"; 
	}
	
	private String format(Float value) {
		return String.format("%.2f", value);
	}
}
