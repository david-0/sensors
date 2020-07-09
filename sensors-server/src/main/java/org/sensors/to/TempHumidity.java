package org.sensors.to;

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
	
	@Override
	public String toString() {
		return  format(temp)+ "C°, "+format(humidity) +"% rel"; 
	}
	
	private String format(Double value) {
		return String.format("%.2f", value);
	}


}
