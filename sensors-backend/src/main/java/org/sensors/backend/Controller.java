package org.sensors.backend;

import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.sensors.backend.sensor.handler.EventBasedSource;
import org.sensors.backend.sensor.handler.IntervalBasedSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Controller {

	private static final Logger logger = LoggerFactory
			.getLogger(Controller.class);

	private static final String SETTING_TOPIC = "settings";

	private final List<ChangeEventListener> changeEventListeners = new ArrayList<>();
	private final List<IntervalBasedSource> intervalBasedSources = new ArrayList<>();
	private final List<EventBasedSource> eventBasedSources = new ArrayList<>();

	private boolean initialized;
	private final KafkaProducer<String, String> producer;

	private EventStore store;

	private Future<?> runFuture;

	private KafkaConsumer<String, String> consumer;

	public Controller(KafkaProducer<String, String> producer,
			KafkaConsumer<String, String> consumer) {
		this.producer = producer;
		this.consumer = consumer;
		store = new EventStore();
		changeEventListeners.add((k, v) -> updateSensorReadingInterval(k, v));
	}

	public void addSettingChangeEventListener(ChangeEventListener listener) {
		this.changeEventListeners.add(listener);
	}

	public void addIntervalBasedSource(IntervalBasedSource source) {
		this.intervalBasedSources.add(source);
	}

	public void addEventBasedSource(EventBasedSource source) {
		this.eventBasedSources.add(source);
	}

	public void init() throws InterruptedException, ExecutionException {
		if (initialized) {
			throw new IllegalStateException("Controller already initialized");
		}
		for (IntervalBasedSource source : intervalBasedSources) {
			Execution exec = new Execution(producer,
					"interval-" + source.getId(), source.getDataProvider());
			store.addEvent(source.getId(), ZonedDateTime.now(),
					source.getDefaultInterval(), exec);
		}
		for (EventBasedSource source : eventBasedSources) {
			source.setEventChange((id, state) -> producer
					.send(new ProducerRecord<String, String>("event-" + id,
							state.toString())));
		}
		consumer.subscribe(Arrays.asList(SETTING_TOPIC));
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
				} else {
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
				onMessage(record.key(), record.value());
			}
		}
	}

	private void onMessage(String key, String value) {
		Optional<ChangeEventListener> findAny = changeEventListeners.stream() //
				.filter(l -> l.changeEventProcessed(key, value)).findAny();
		if (findAny.isPresent()) {
			logger.warn("No handler for key: '{}', value: '{}'", key, value);
		}
	}

	private boolean updateSensorReadingInterval(String key, String value) {
		if (store.hasId(key)) {
			try {
				Integer interval = new ObjectMapper().readValue(value,
						Integer.class);
				store.updateInterval(key,
						Duration.ofMillis(interval.intValue()),
						ZonedDateTime.now());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return true;
		}
		return false;
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
