package org.sensors.backend.sensor.handler;

import java.time.Duration;
import java.util.function.Supplier;

public class IntervalSensor {
	private final Supplier<?> dataProvider;
	private final String id;
	private final Duration defaultInterval;

	public IntervalSensor(Supplier<?> dataProvider, String Id, Duration defaultInterval) {
		this.dataProvider = dataProvider;
		id = Id;
		this.defaultInterval = defaultInterval;
	}

	public Supplier<?> getDataProvider() {
		return dataProvider;
	}

	public String getId() {
		return id;
	}

	public Duration getDefaultInterval() {
		return defaultInterval;
	}
}
