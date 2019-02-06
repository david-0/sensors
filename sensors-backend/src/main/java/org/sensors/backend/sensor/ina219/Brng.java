package org.sensors.backend.sensor.ina219;

/**
 * Enum for the Bus Voltage Range setting (BRNG)
 */
public enum Brng {
	V16(0), // 16 Volts
	V32(1); // 32 Volts

	private int value;

	Brng(int val) {
		value = val;
	}

	int getValue() {
		return value;
	}
}