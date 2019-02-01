package org.sensors.backend;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pi4j.io.i2c.I2CBus;

public class Controller {

	private static final Logger logger = LoggerFactory
			.getLogger(Controller.class);

	private static final String SENSOR1_TOPIC = "sensor1";
	private static final String SENSOR1_ID = "S1";
	private static final String SENSOR2_TOPIC = "sensor2";
	private static final String SENSOR2_ID = "S2";

	private boolean initialized;
	private final I2CBus bus;
	private final KafkaProducer<String, String> producer;

	private SensorMcp9808 sensor1;
	private SensorMcp9808 sensor2;
	private EventStore store;

	private Future<?> runFuture;

	public Controller(I2CBus bus, KafkaProducer<String, String> producer) {
		this.bus = bus;
		this.producer = producer;
		store = new EventStore();
	}

	public void init() throws InterruptedException, ExecutionException {
		if (initialized) {
			throw new IllegalStateException("Controller already initialized");
		}
		sensor1 = new SensorMcp9808(bus, 0x18, "Controller");
		sensor2 = new SensorMcp9808(bus, 0x1C, "Unbekannt");
		sensor1.init();
		store.addEvent(SENSOR1_ID,
				ZonedDateTime.now().plus(Duration.ofMillis(300)),
				Duration.ofMillis(10_000), new Execution(producer,
						SENSOR1_TOPIC, sensor1::readTemperature));
		sensor2.init();
		store.addEvent(SENSOR2_ID,
				ZonedDateTime.now().plus(Duration.ofMillis(300)),
				Duration.ofMillis(10_000), new Execution(producer,
						SENSOR2_TOPIC, sensor2::readTemperature));
	}

	public void run() {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		runFuture = executor.submit(this::runAsync);
	}

	private void runAsync() {
		try {
			while (true) {
				Duration waitDuration = getNextWaitDuration();
				if (waitDuration.isNegative() || waitDuration.isZero()) {
					Event event = store.getNextEvent();
					event.getExec().run();
					logger.info("Exec id={} --> topic={}", event.getId(),
							event.getExec().getTopic());
				} else {
					logger.info("Wait {} ns", waitDuration.toNanos());
					TimeUnit.NANOSECONDS.sleep(waitDuration.toNanos());
				}
			}
		} catch (JsonProcessingException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private Duration getNextWaitDuration() {
		Duration maxWaitDuration = Duration.ofSeconds(1);
		if (store.hasNextEvent()) {
			Duration toNextExec = store.getDurationToNextEvent();
			if (toNextExec.minus(maxWaitDuration).isNegative()) {
				return toNextExec;
			}
		}
		return maxWaitDuration;
	}

	public void stop() {
		logger.info("wait for shutdown ...");
		if (!runFuture.isCancelled()) {
			runFuture.cancel(true);
		}
		logger.info("shutdown completed");
	}

}
