package org.sensors.backend.to;

public class TempHumidityPressure {

	private final Double temp;
	private final Double humidity;
	private final Double pressure;

	public TempHumidityPressure(Double temp, Double humidity, Double pressure) {
		this.temp = temp;
		this.humidity = humidity;
		this.pressure = pressure;
	}

	public Double getHumidity() {
		return humidity;
	}

	public Double getTemp() {
		return temp;
	}

	public Double getPressure() {
		return pressure;
	}
}
