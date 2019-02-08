package org.sensors.backend;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.sensors.backend.event.Event;

public class EventStoreTest {

	private ZonedDateTime execTime = ZonedDateTime.of(2019, 01, 2, 7, 56, 32, 0, ZoneId.of("GMT"));
	private Event event;

	@Before
	public void setup() {
		event = createEventMock(execTime, Duration.ofMillis(200), "id");
	}

	@Test
	public void testAddEvent() {
		EventStore store = new EventStore();
		store.addEvent(event);
		assertThat(store.size(), is(1));
	}

	@Test
	public void testHasNextEvent_whenNotEmpty() {
		EventStore store = new EventStore();
		store.addEvent(event);
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
		store.addEvent(event);
		assertThat(store.getNextExecutionTime(), is(execTime));
	}

	@Test
	public void testNextExecutionTime_whenOneEventFetch() {
		EventStore store = new EventStore();
		Duration interval = Duration.ofSeconds(10);
		when(event.getIntervall()).thenReturn(interval);
		store.addEvent(event);
		assertThat(store.getNextEvent().getExecutionTme(), is(execTime));
		assertThat(store.getNextExecutionTime(), is(execTime.plus(interval)));
	}

	@Test
	public void testUpdateInterval_whenNewExecutionTimeInTheFuture() {
		EventStore store = new EventStore();
		store.addEvent(event);
		store.getNextEvent();
		ZonedDateTime now = execTime.plus(Duration.ofMillis(50));
		store.updateInterval("id", Duration.ofMillis(200), now);
		assertThat(store.getNextExecutionTime(), is(now.plus(Duration.ofMillis(150))));
		assertThat(store.getNextEvent().getIntervall(), is(Duration.ofMillis(200)));
	}

	@Test
	public void testUpdateInterval_whenNewExecutionTimeInThePast() {
		EventStore store = new EventStore();
		store.addEvent(event);
		store.getNextEvent();
		ZonedDateTime now = execTime.plus(Duration.ofMillis(150));
		store.updateInterval("id", Duration.ofMillis(100), now);
		assertThat(store.getNextExecutionTime(), is(now));
		assertThat(store.getNextEvent().getIntervall(), is(Duration.ofMillis(100)));
	}

	@Test
	public void testRemoveEvent() {
		EventStore store = new EventStore();
		store.addEvent(event);
		assertThat(store.removeEvent("id"), is(true));
	}

	@Test
	public void testGetNextEvent() {
		EventStore store = new EventStore();
		store.addEvent(event);
		assertThat(store.getNextEvent().getId(), is("id"));
	}

	@Test
	public void testSize_whenEventWithNoIntervallIsFetched() {
		EventStore store = new EventStore();
		store.addEvent(event);
		when(event.getIntervall()).thenReturn(null);
		store.getNextEvent();
		assertThat(store.size(), is(0));
	}

	private Event createEventMock(ZonedDateTime execTime, Duration interval, String id) {
		event = mock(Event.class);
		when(event.getId()).thenReturn(id);
		when(event.getExecutionTme()).thenReturn(execTime);
		when(event.getIntervall()).thenReturn(interval);
		when(event.compareTo(any())).thenCallRealMethod();
		when(event.updateStartTime(any(), any())).thenAnswer(this::answerMock);
		return event;
	}

	private Event answerMock(InvocationOnMock invocation) {
		return createEventMock(invocation.getArgument(0), invocation.getArgument(1), "id");
	}

}
