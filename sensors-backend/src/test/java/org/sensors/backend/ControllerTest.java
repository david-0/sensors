package org.sensors.backend;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyInt;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;

public class ControllerTest {
	private I2CBus bus;
	private KafkaProducer<String, String> producer;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() throws IOException {
		bus = mock(I2CBus.class);
		when(bus.getDevice(anyInt())).thenReturn(mock(I2CDevice.class));
		producer = mock(KafkaProducer.class);
	}

	@Test
	public void testRun() throws InterruptedException, ExecutionException,
			JsonProcessingException {
		Controller controller = new Controller(bus, producer);
		controller.init();
		controller.run();
		Thread.sleep(4000);
		controller.stop();
	}
}
