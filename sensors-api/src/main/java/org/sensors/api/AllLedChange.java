package org.sensors.api;

import com.github.mbelling.ws281x.Color;

public class AllLedChange {
	private final Color color;

	public AllLedChange(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}
}
