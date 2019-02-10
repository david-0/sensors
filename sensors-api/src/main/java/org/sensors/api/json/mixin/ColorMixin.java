package org.sensors.api.json.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class ColorMixin {
	ColorMixin(@JsonProperty("red") int red, @JsonProperty("green") int green, @JsonProperty("blue") int blue) {
	}

	@JsonIgnore
	abstract int getColorBits(); // we don't need it!
}