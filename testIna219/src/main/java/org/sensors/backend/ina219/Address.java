package org.sensors.backend.ina219;

/**
 * Enumeration of the valid I2C bus addresses to use with the INA219.
 * 
 * Note: this implementation does not include support addresses that have the A1
 * or A0 pins connected to SDA or SCL.
 *
 */
public enum Address {
	ADDR_40(0x40), ADDR_41(0x41), ADDR_44(0x44), ADDR_45(0x45);

	private final int addr;

	Address(final int a) {
		addr = a;
	}

	int getValue() {
		return addr;
	}

	static public Address getAddress(int value) {
		switch (value) {
		case 0x40:
			return ADDR_40;
		case 0x41:
			return ADDR_41;
		case 0x44:
			return ADDR_44;
		case 0x45:
			return ADDR_45;
		default:
			return null;
		}

	}
}