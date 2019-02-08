package org.sensors.backend;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.sensors.backend.event.Event;

public class EventStore {
	private SortedSet<Event> events = new TreeSet<Event>();
	private Map<String, Event> lookup = new HashMap<>();

	public void addEvent(Event event) {
		events.add(event);
		lookup.put(event.getId(), event);
	}

	public boolean hasNextEvent() {
		return !events.isEmpty();
	}

	public int size() {
		return events.size();
	}

	public boolean updateInterval(String id, Duration newInterval, ZonedDateTime start) {
		final Event event = lookup.get(id);
		if (event == null) {
			return false;
		}
		if (!removeEvent(id)) {
			throw new RuntimeException("Event could not be removed from events.");
		}
		addEvent(event.updateStartTime(computeNewExecTime(newInterval, event, start), newInterval));
		return true;
	}

	public boolean updateFrequency(String id, Integer frequencyInHz) {
		final Event event = lookup.get(id);
		if (event == null) {
			return false;
		}
		if (!removeEvent(id)) {
			throw new RuntimeException("Event could not be removed from events.");
		}
		Duration interval = Duration.ofNanos(1_000_000_000 / frequencyInHz);
		addEvent(event.updateStartTime(computeNewExecTime(interval, event, ZonedDateTime.now()), interval));
		return true;
	}

	private ZonedDateTime computeNewExecTime(Duration newInterval, Event event, ZonedDateTime now) {
		ZonedDateTime newExecTime = event.getExecutionTme().minus(event.getIntervall()).plus(newInterval);
		if (newExecTime.isBefore(now)) {
			newExecTime = now;
		}
		return newExecTime;
	}

	public ZonedDateTime getNextExecutionTime() {
		return events.first().getExecutionTme();
	}

	public boolean removeEvent(String id) {
		Event event = lookup.get(id);
		return events.remove(event);
	}

	public Event getNextEvent() {
		Event first = events.first();
		if (!events.remove(first)) {
			throw new RuntimeException("Event could not be removed from events.");
		}
		if (lookup.remove(first.getId()) == null) {
			throw new RuntimeException("Event could not be removed from lookup.");
		}
		if (first.getIntervall() != null) {
			Event event = first.updateStartTime(first.getExecutionTme().plus(first.getIntervall()),
					first.getIntervall());
			events.add(event);
			lookup.put(first.getId(), event);
		}
		return first;
	}

}
