package org.sensors.backend.sensor.handler;

import java.util.function.BiConsumer;

public interface EventBasedSource {
	void setEventChange(BiConsumer<String, Boolean> eventChange);
}
