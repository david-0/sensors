package org.sensors.backend.device.ledstrip;

import com.github.mbelling.ws281x.Color;

public class OneLedChange {
	private final int number;
	private final Color color;

	public OneLedChange(int number, Color color) {
		this.number = number;
		this.color = color;
	}

	public int getNumber() {
		return number;
	}

	public Color getColor() {
		return color;
	}
}
