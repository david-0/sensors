package org.sensors.backend;

import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pi4j.io.i2c.I2CBus;

public class Controller {

	private static final Logger logger = LoggerFactory
			.getLogger(Controller.class);

	private static final String SENSOR1_TOPIC = "sensor1";
	private static final String SENSOR1_ID = "S1";
	private static final String SENSOR2_TOPIC = "sensor2";
	private static final String SENSOR2_ID = "S2";

	private static final String SETTING_TOPIC = "settings";
	private static final String SENSOR_READING_INTERVAL_TOPIC = "sensorReadingIntervals";
	private static final String BUTTON_EVENT_TOPIC = "buttonEvents";

	private boolean initialized;
	private final I2CBus bus;
	private final KafkaProducer<String, String> producer;

	private SensorMcp9808 sensor1;
	private SensorMcp9808 sensor2;
	private EventStore store;

	private Future<?> runFuture;

	private KafkaConsumer<String, String> consumer;

	public Controller(I2CBus bus, KafkaProducer<String, String> producer,
			KafkaConsumer<String, String> consumer) {
		this.bus = bus;
		this.producer = producer;
		this.consumer = consumer;
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
		consumer.subscribe(Arrays.asList(SETTING_TOPIC,
				SENSOR_READING_INTERVAL_TOPIC, BUTTON_EVENT_TOPIC));
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
					logger.info("Wait for messages for max. {} ns",
							waitDuration.toNanos());
					consumeRecords(waitDuration);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void consumeRecords(Duration waitDuration) throws IOException {
		ConsumerRecords<String, String> records = consumer.poll(waitDuration);
		for (ConsumerRecord<String, String> record : records) {
			if (SETTING_TOPIC.equals(record.topic())) {
				updateSettings(record.key(), record.value());
			} else if (SENSOR_READING_INTERVAL_TOPIC.equals(record.topic())) {
				updateSensorReadingInterval(record.key(), record.value());
			} else if (BUTTON_EVENT_TOPIC.equals(record.topic())) {
				updateButtonEvent(record.key(), record.value());
			}
		}
	}

	private void updateButtonEvent(String key, String value) {
		// TODO Auto-generated method stub
	}

	private void updateSensorReadingInterval(String key, String value)
			throws IOException {
		Integer interval = new ObjectMapper().readValue(value, Integer.class);
		store.updateInterval(key, Duration.ofMillis(interval.intValue()),
				ZonedDateTime.now());
	}

	private void updateSettings(String key, String value) {
		// TODO Auto-generated method stub
	}

	private Duration getNextWaitDuration() {
		Duration maxWaitDuration = Duration.ofSeconds(1);
		if (store.hasNextEvent()) {
			Duration toNextExec = Duration.between(ZonedDateTime.now(),
					store.getNextExecutionTime());
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
