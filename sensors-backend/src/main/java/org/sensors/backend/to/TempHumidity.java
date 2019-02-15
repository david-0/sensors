package org.sensors.backend.to;

public class TempHumidity {

	private final Double temp;
	private final Double humidity;

	public TempHumidity(Double temp, Double humidity) {
		this.temp = temp;
		this.humidity = humidity;
	}

	public Double getHumidity() {
		return humidity;
	}

	public Double getTemp() {
		return temp;
	}

}
