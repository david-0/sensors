package org.sensors.backend;

import java.time.Duration;
import java.time.ZonedDateTime;

public class Event implements Comparable<Event> {
	private final ZonedDateTime executionTime;
	private final Duration intervall;
	private final Execution exec;
	private final String id;

	public Event(String id, ZonedDateTime executionTme, Duration intervall,
			Execution exec) {
		this.id = id;
		this.executionTime = executionTme;
		this.intervall = intervall;
		this.exec = exec;
	}

	public String getId() {
		return id;
	}

	public ZonedDateTime getExecutionTme() {
		return executionTime;
	}

	public Duration getIntervall() {
		return intervall;
	}

	public Execution getExec() {
		return exec;
	}

	@Override
	public int compareTo(Event e) {
		if (!executionTime.equals(e.getExecutionTme())) {
			return executionTime.compareTo(e.getExecutionTme());
		}
		return hashCode() - e.hashCode();
	}
}
