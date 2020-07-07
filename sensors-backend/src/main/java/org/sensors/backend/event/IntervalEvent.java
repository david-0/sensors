package org.sensors.backend.event;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.function.BiConsumer;

import org.sensors.backend.sensor.handler.IntervalBasedSource;

public class IntervalEvent extends Event {

	private IntervalBasedSource source;
	private BiConsumer<String, Object> sender;

	public IntervalEvent(IntervalBasedSource source, ZonedDateTime start, BiConsumer<String, Object> sender) {
		this(source, source.getInterval(), start, sender);
	}

	private IntervalEvent(IntervalBasedSource source, Duration interval, ZonedDateTime start,
			BiConsumer<String, Object> sender) {
		super(source.getId(), start, interval);
		this.source = source;
		this.sender = sender;
	}

	@Override
	public void exec() {
		sender.accept(source.getId(), source.getDataProvider().get());
	}

	@Override
	public Event updateStartTime(ZonedDateTime start, Duration interval) {
		return new IntervalEvent(source, interval, start, sender);
	}

}
