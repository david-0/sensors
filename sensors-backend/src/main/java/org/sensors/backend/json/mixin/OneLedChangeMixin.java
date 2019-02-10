package org.sensors.backend.json.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.mbelling.ws281x.Color;

public class OneLedChangeMixin {
	public OneLedChangeMixin(@JsonProperty("number") int number, @JsonProperty("color") Color color) {
	}
}
