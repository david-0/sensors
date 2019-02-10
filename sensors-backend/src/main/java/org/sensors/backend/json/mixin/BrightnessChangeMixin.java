package org.sensors.backend.json.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BrightnessChangeMixin {
	public BrightnessChangeMixin(@JsonProperty("brightness") int brigthness, @JsonProperty("render") boolean render) {
	}
}
