package org.sensors.backend.sensor.handler;

import java.time.Duration;
import java.util.function.Supplier;

public class IntervalBasedSource {
	private final Supplier<?> dataProvider;
	private final String id;
	private final Duration defaultInterval;

	public IntervalBasedSource(Supplier<?> dataProvider, String Id, Duration defaultInterval) {
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
