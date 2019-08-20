package org.sensors.backend.device.xml;

import java.util.List;

public class TestParent {

	private final List<ITest> tests;

	public TestParent(List<ITest> tests) {
		this.tests = tests;
	}

	public List<ITest> getTests() {
		return tests;
	}
}
