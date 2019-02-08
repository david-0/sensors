package org.sensors.backend.event;

import java.time.Duration;
import java.time.ZonedDateTime;

import org.sensors.backend.sensor.handler.StateUpdater;

public class FrequencyEvent extends Event {

	private StateUpdater source;

	public FrequencyEvent(StateUpdater source, ZonedDateTime start) {
		this(source, start, Duration.ofNanos(1_000_000_000 / source.getFrequencyInHz()));
	}
	
	private FrequencyEvent(StateUpdater source, ZonedDateTime start, Duration interval) {
		super(source.getId(), start, interval);
		this.source = source;
	}

	@Override
	public void exec() {
		source.updateState();
	}

	@Override
	public Event updateStartTime(ZonedDateTime start, Duration interval) {
		return new FrequencyEvent(source, start, interval);
	}

}
