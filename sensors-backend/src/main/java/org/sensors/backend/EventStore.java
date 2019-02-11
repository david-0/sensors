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
	private Object o = new Object();

	public void addEvent(Event event) {
		synchronized (o) {
			events.add(event);
			lookup.put(event.getId(), event);
			o.notify();
		}
	}

	public boolean updateInterval(String id, Duration newInterval, ZonedDateTime start) {
		synchronized (o) {
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
	}

	public boolean updateFrequency(String id, Integer frequencyInHz) {
		synchronized (o) {
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
	}

	private ZonedDateTime computeNewExecTime(Duration newInterval, Event event, ZonedDateTime now) {
		ZonedDateTime newExecTime = event.getExecutionTme().minus(event.getIntervall()).plus(newInterval);
		if (newExecTime.isBefore(now)) {
			newExecTime = now;
		}
		return newExecTime;
	}

	private boolean removeEvent(String id) {
		Event event = lookup.get(id);
		return events.remove(event);
	}

	/**
	 * This methode blocks until the next event is ready
	 * 
	 * @return The next event
	 */
	public Event getNextEvent() {
		synchronized (o) {
			Event first = events.first();
			waitEventIsExecutable(first);
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

	private void waitEventIsExecutable(Event first) {
		try {
			Duration timeToWait = timeToWait(first);
			while (isPositive(timeToWait)) {
				o.wait(timeToWait.toMillis(), timeToWait.getNano());
				timeToWait = timeToWait(first);
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean isPositive(Duration timeToWait) {
		return timeToWait.negated().isNegative();
	}

	private Duration timeToWait(Event first) {
		return Duration.between(ZonedDateTime.now(), first.getExecutionTme());
	}
}
