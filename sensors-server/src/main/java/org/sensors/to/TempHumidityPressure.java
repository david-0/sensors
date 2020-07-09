package org.sensors.to;

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

	@Override
	public String toString() {
		return format(temp) + "C°, " + format(humidity) + "% rel, " + format(pressure) + "kPa";
	}

	private String format(Double value) {
		return String.format("%.2f", value);
	}
}
