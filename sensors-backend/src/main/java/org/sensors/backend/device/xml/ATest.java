package org.sensors.backend.device.xml;

public class ATest implements ITest {

	private final int number;
	private final String name;

	public ATest(final int number, final String name) {
		this.number = number;
		this.name = name;
	}

	@Override
	public int getNumber() {
		return number;
	}

	public String getName() {
		return name;
	}
}
