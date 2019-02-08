package org.sensors.backend.event;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.function.BiConsumer;

import org.sensors.backend.sensor.handler.IntervalBasedSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class IntervalEvent extends Event {

	private IntervalBasedSource source;
	private BiConsumer<String, String> sender;

	public IntervalEvent(IntervalBasedSource source, ZonedDateTime start, BiConsumer<String, String> sender) {
		this(source, source.getInterval(), start, sender);
	}
	
	private IntervalEvent(IntervalBasedSource source, Duration interval, ZonedDateTime start, BiConsumer<String, String> sender) {
		super(source.getId(), start, interval);
		this.source = source;
		this.sender = sender;
	}

	@Override
	public void exec() {
		try {
			String json = new ObjectMapper().writeValueAsString(source.getDataProvider().get());
			sender.accept(source.getId(), json);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Event updateStartTime(ZonedDateTime start, Duration interval) {
		return new IntervalEvent(source, interval, start, sender);
	}

}
