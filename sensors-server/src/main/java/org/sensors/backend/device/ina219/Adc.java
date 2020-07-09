package org.sensors.backend.device.ina219;

/**
 * Enum for the Bus and Shunt ADC Resolution/Averaging settings.
 */
public enum Adc {
	BITS_9(0), // 9 bit samples
	BITS_10(1), // 10 bit samples
	BITS_11(2), // 11 bit samples
	BITS_12(3), // 12 bit samples
	SAMPLES_2(9), // 2 sample average
	SAMPLES_4(10), // 4 sample average
	SAMPLES_8(11), // 8 sample average
	SAMPLES_16(12), // 16 sample average
	SAMPLES_32(13), // 32 sample average
	SAMPLES_64(14), // 64 sample average
	SAMPLES_128(15); // 128 sample average

	private int value;

	Adc(int val) {
		value = val;
	}

	int getValue() {
		return value;
	}
}