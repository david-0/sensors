package org.sensors.backend.ina219;

/**
 * Enum for the PGA gain and range setting options.
 */
public enum Pga {
	GAIN_1(0), // 1
	GAIN_2(1), // /2
	GAIN_4(2), // /4
	GAIN_8(3); // /8

	private int value;

	Pga(int val) {
		value = val;
	}

	int getValue() {
		return value;
	}
}