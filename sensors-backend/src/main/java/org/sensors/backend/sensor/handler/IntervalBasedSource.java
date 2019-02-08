package org.sensors.backend.sensor.handler;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.sensors.backend.ChangeEventListener;

public interface IntervalBasedSource extends ChangeEventListener {

	Supplier<?> getDataProvider();

	String getId();

	Duration getInterval();

	void setIntervalChangeListener(Consumer<Duration> listener);
}
