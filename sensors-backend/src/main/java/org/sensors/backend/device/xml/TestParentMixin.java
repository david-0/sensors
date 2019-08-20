package org.sensors.backend.device.xml;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class TestParentMixin {
	TestParentMixin(@JsonProperty("tests") List<ITest> tests) {
	}
}
