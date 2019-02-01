package org.sensors.backend;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class EventStore {
	private SortedSet<Event> events = new TreeSet<Event>();
	private Map<String, Event> lookup = new HashMap<>();

	public void addEvent(String id, ZonedDateTime executionTime,
			Duration interval, Execution exec) {
		Event event = new Event(id, executionTime, interval, exec);
		events.add(event);
		lookup.put(id, event);
	}

	public boolean hasNextEvent() {
		return !events.isEmpty();
	}

	public int size() {
		return events.size();
	}

	public void updateInterval(String id, Duration newInterval,
			ZonedDateTime now) {
		Event event = lookup.get(id);
		if (!removeEvent(id)) {
			throw new RuntimeException(
					"Event could not be removed from events.");
		}
		addEvent(id, computeNewExecTime(newInterval, event, now), newInterval,
				event.getExec());
	}

	private ZonedDateTime computeNewExecTime(Duration newInterval, Event event,
			ZonedDateTime now) {
		ZonedDateTime newExecTime = event.getExecutionTme()
				.minus(event.getIntervall()).plus(newInterval);
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
			throw new RuntimeException(
					"Event could not be removed from events.");
		}
		if (lookup.remove(first.getId()) == null) {
			throw new RuntimeException(
					"Event could not be removed from lookup.");
		}
		if (first.getIntervall() != null) {
			Event event = new Event(first.getId(),
					first.getExecutionTme().plus(first.getIntervall()),
					first.getIntervall(), first.getExec());
			events.add(event);
			lookup.put(first.getId(), event);
		}
		return first;
	}
}
