package org.sensors.api.json.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.mbelling.ws281x.Color;

public class AllLedChangeMixin {
	public AllLedChangeMixin(@JsonProperty("color") Color color) {
	}
}
