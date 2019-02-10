package org.sensors.api;

import com.github.mbelling.ws281x.Color;

public class AllLedChange {
	private final Color color;
	private int brightness;

	public AllLedChange(int brightness, Color color) {
		this.brightness = brightness;
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public int getBrightness() {
		return brightness;
	}
}
