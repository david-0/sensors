package org.sensors.backend.event;

import java.time.Duration;
import java.time.ZonedDateTime;

public abstract class Event implements Comparable<Event> {
	private final ZonedDateTime executionTime;
	private final Duration interval;
	private final String id;

	public Event(String id, ZonedDateTime executionTme, Duration interval) {
		this.id = id;
		this.executionTime = executionTme;
		this.interval = interval;
	}

	public String getId() {
		return id;
	}

	public ZonedDateTime getExecutionTme() {
		return executionTime;
	}

	public Duration getIntervall() {
		return interval;
	}
	
	public abstract void exec();
	
	public abstract Event updateStartTime(ZonedDateTime start, Duration interval);

	@Override
	public int compareTo(Event e) {
		if (!getExecutionTme().equals(e.getExecutionTme())) {
			return executionTime.compareTo(e.getExecutionTme());
		}
		return hashCode() - e.hashCode();
	}
}
