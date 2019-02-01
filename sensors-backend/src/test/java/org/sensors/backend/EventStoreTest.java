package org.sensors.backend;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Test;

public class EventStoreTest {

	@Test
	public void testAddEvent() {
		EventStore store = new EventStore();
		store.addEvent("id", ZonedDateTime.now(), Duration.ofMillis(100),
				new Execution(null, "topic", () -> 10));
		assertThat(store.size(), is(1));
	}

	@Test
	public void testHasNextEvent_whenNotEmpty() {
		EventStore store = new EventStore();
		store.addEvent("id", ZonedDateTime.now(), Duration.ofMillis(100),
				new Execution(null, "topic", () -> 10));
		assertThat(store.hasNextEvent(), is(true));
	}

	@Test
	public void testHasNextEvent_whenEmpty() {
		EventStore store = new EventStore();
		assertThat(store.hasNextEvent(), is(false));
	}
	
	@Test
	public void testNextExecutionTime() {
		EventStore store = new EventStore();
		ZonedDateTime execTime = ZonedDateTime.of(2019, 01, 2, 7, 56, 32, 0,
				ZoneId.of("GMT"));
		store.addEvent("id", execTime, Duration.ofMillis(100),
				new Execution(null, "topic", () -> 10));
		assertThat(store.getNextExecutionTime(), is(execTime));
	}

	@Test
	public void testNextExecutionTime_whenOneEventFetch() {
		EventStore store = new EventStore();
		ZonedDateTime execTime = ZonedDateTime.of(2019, 01, 2, 7, 56, 32, 0,
				ZoneId.of("GMT"));
		store.addEvent("id", execTime, Duration.ofMillis(100),
				new Execution(null, "topic", () -> 10));
		assertThat(store.getNextEvent().getExecutionTme(), is(execTime));
		assertThat(store.getNextExecutionTime(),
				is(execTime.plus(Duration.ofMillis(100))));
	}
	
	@Test
	public void testUpdateInterval_whenNewExecutionTimeInTheFuture() {
		EventStore store = new EventStore();
		ZonedDateTime execTime = ZonedDateTime.of(2019, 01, 2, 7, 56, 32, 0,
				ZoneId.of("GMT"));
		store.addEvent("id", execTime, Duration.ofMillis(100),
				new Execution(null, "topic", () -> 10));
		store.getNextEvent();
		ZonedDateTime now = execTime.plus(Duration.ofMillis(50));
		store.updateInterval("id", Duration.ofMillis(200), now);
		assertThat(store.getNextExecutionTime(), is(now.plus(Duration.ofMillis(150))));
		assertThat(store.getNextEvent().getIntervall(), is(Duration.ofMillis(200)));
	}
	
	@Test
	public void testUpdateInterval_whenNewExecutionTimeInThePast() {
		EventStore store = new EventStore();
		ZonedDateTime execTime = ZonedDateTime.of(2019, 01, 2, 7, 56, 32, 0,
				ZoneId.of("GMT"));
		store.addEvent("id", execTime, Duration.ofMillis(200),
				new Execution(null, "topic", () -> 10));
		store.getNextEvent();
		ZonedDateTime now = execTime.plus(Duration.ofMillis(150));
		store.updateInterval("id", Duration.ofMillis(100), now);
		assertThat(store.getNextExecutionTime(), is(now));
		assertThat(store.getNextEvent().getIntervall(), is(Duration.ofMillis(100)));
	}

	@Test
	public void testRemoveEvent() {
		EventStore store = new EventStore();
		store.addEvent("id", ZonedDateTime.now(), Duration.ofMillis(100),
				new Execution(null, "topic", () -> 10));
		assertThat(store.removeEvent("id"), is(true));
	}

	@Test
	public void testGetNextEvent() {
		EventStore store = new EventStore();
		store.addEvent("id", ZonedDateTime.now(), Duration.ofMillis(100),
				new Execution(null, "topic", () -> 10));
		assertThat(store.getNextEvent().getId(), is("id"));
	}

	@Test
	public void testSize_whenEventWithNoIntervallIsFetched() {
		EventStore store = new EventStore();
		store.addEvent("id", ZonedDateTime.now(), null,
				new Execution(null, "topic", () -> 10));
		store.getNextEvent();
		assertThat(store.size(), is(0));
	}

}
