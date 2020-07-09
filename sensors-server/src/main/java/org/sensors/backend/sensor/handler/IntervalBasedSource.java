package org.sensors.backend.sensor.handler;

import java.time.Duration;
import java.util.function.Supplier;

public interface IntervalBasedSource  {

	Supplier<?> getDataProvider();

	String getId();

	Duration getInterval();
}
