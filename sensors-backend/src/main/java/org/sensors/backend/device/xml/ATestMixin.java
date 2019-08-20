package org.sensors.backend.device.xml;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class ATestMixin {
	ATestMixin(@JsonProperty("number") int number, @JsonProperty("name") String name) {
	}
}
