package org.sensors.backend.sensor.handler;

import java.util.function.BiConsumer;

public interface EventBasedSource {
	void onChange(BiConsumer<String, Boolean> eventChange);
}
