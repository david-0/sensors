package org.sensors.backend.device.xml;

public class BTest implements ITest {

	private final int number;
	private final String description;

	public BTest(final int number, final String description) {
		this.number = number;
		this.description = description;
	}

	@Override
	public int getNumber() {
		return number;
	}

	public String getDescription() {
		return description;
	}
}
