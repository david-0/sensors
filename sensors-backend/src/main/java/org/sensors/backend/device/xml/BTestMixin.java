package org.sensors.backend.device.xml;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class BTestMixin {
	BTestMixin(@JsonProperty("number") int number, @JsonProperty("description") String description) {
	}
}
