package org.sensors.backend;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;

public class ControllerTest {
	private I2CBus bus;
	private KafkaProducer<String, String> producer;
	private KafkaConsumer<String, String> consumer;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() throws IOException {
		bus = mock(I2CBus.class);
		I2CDevice device = mock(I2CDevice.class);
		when(device.read(anyInt(), Mockito.any(), anyInt(), anyInt()))
				.thenAnswer(new Answer<Object>() {

					@Override
					public Object answer(InvocationOnMock invocation)
							throws Throwable {
						byte[] data = (byte[]) invocation.getArgument(1);
						data[0] = 10;
						data[1] = 0;
						return null;
					}
				});

		when(bus.getDevice(anyInt())).thenReturn(device);
		producer = mock(KafkaProducer.class);
		consumer = mock(KafkaConsumer.class);
		doAnswer(new Answer<ConsumerRecords<String, String>>() {
			@Override
			public ConsumerRecords<String, String> answer(
					InvocationOnMock invocation) throws Throwable {
				Duration duration = invocation.getArgument(0);
				TimeUnit.NANOSECONDS.sleep(duration.toNanos());
				return new ConsumerRecords<>(Collections.emptyMap());
			}
		}).when(consumer).poll(any(Duration.class));
	}

	@Test
	public void testRun() throws InterruptedException, ExecutionException,
			JsonProcessingException {
		Controller controller = new Controller(bus, producer, consumer);
		controller.init();
		controller.run();
		Thread.sleep(10000);
		controller.stop();
	}
}
