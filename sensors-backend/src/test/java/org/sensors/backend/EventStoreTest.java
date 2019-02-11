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
	public void testGetNextEvent() {
		EventStore store = new EventStore();
		store.addEvent(event);
		assertThat(store.getNextEvent().getId(), is("id"));
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
