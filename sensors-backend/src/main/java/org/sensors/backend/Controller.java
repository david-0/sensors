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
import org.apache.kafka.clients.producer.RecordMetadata;
import org.sensors.backend.event.FrequencyEvent;
import org.sensors.backend.event.IntervalEvent;
import org.sensors.backend.sensor.handler.EventBasedSource;
import org.sensors.backend.sensor.handler.IntervalBasedSource;
import org.sensors.backend.sensor.handler.StateUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Controller {

	private static final Logger logger = LoggerFactory.getLogger(Controller.class);

	private static final String SETTING_TOPIC = "settings";

	private final List<ChangeEventListener> changeEventListeners = new ArrayList<>();
	private final List<IntervalBasedSource> intervalBasedSources = new ArrayList<>();
	private final List<StateUpdater> stateUpdaterSources = new ArrayList<>();
	private final List<EventBasedSource> eventBasedSources = new ArrayList<>();

	private boolean initialized;
	private final KafkaProducer<String, String> producer;

	private EventStore store;

	private Future<?> runFuture;

	private KafkaConsumer<String, String> consumer;

	public Controller(KafkaProducer<String, String> producer, KafkaConsumer<String, String> consumer) {
		this.producer = producer;
		this.consumer = consumer;
		store = new EventStore();
	}

	public void addSettingChangeEventListener(ChangeEventListener listener) {
		ensureNotAlreadyInitialized();
		this.changeEventListeners.add(listener);
	}

	public void addIntervalBasedSource(IntervalBasedSource source) {
		ensureNotAlreadyInitialized();
		this.intervalBasedSources.add(source);
		this.changeEventListeners.add(source);
	}

	public void addStateUpdaterSource(StateUpdater source) {
		ensureNotAlreadyInitialized();
		this.stateUpdaterSources.add(source);
		this.changeEventListeners.add(source);
	}

	public void addEventBasedSource(EventBasedSource source) {
		ensureNotAlreadyInitialized();
		this.eventBasedSources.add(source);
	}

	public void init() throws InterruptedException, ExecutionException {
		ensureNotAlreadyInitialized();
		for (IntervalBasedSource source : intervalBasedSources) {
			store.addEvent(new IntervalEvent(source, ZonedDateTime.now(), (id, state) -> sendMessage(id, null, state)));
			source.setIntervalChangeListener(
					interval -> store.updateInterval(source.getId(), interval, ZonedDateTime.now()));
		}
		for (StateUpdater updater : stateUpdaterSources) {
			store.addEvent(new FrequencyEvent(updater, ZonedDateTime.now()));
			updater.setFrequencyChangeListener(frequencyInHz -> store.updateFrequency(updater.getId(), frequencyInHz));
		}
		for (EventBasedSource source : eventBasedSources) {
			source.onChange((id, state) -> sendMessage("events", id, state.toString()));
		}
		consumer.subscribe(Arrays.asList(SETTING_TOPIC));
		initialized = true;
	}

	private Future<RecordMetadata> sendMessage(String topic, String key, String value) {
		return producer.send(new ProducerRecord<String, String>(topic, key, value));
	}

	public void run() {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		runFuture = executor.submit(this::runAsync);
	}

	private void ensureNotAlreadyInitialized() {
		if (initialized) {
			throw new IllegalStateException("alreay initialized");
		}
	}

	private void runAsync() {
		try {
			while (true) {
				Duration waitDuration = getNextWaitDuration();
				if (waitDuration.isNegative() || waitDuration.isZero()) {
					store.getNextEvent().exec();
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
				.filter(l -> l.onSettingChange(key, value)).findAny();
		if (findAny.isPresent()) {
			logger.warn("No handler for key: '{}', value: '{}'", key, value);
		}
	}

	private Duration getNextWaitDuration() {
		Duration maxWaitDuration = Duration.ofSeconds(1);
		if (store.hasNextEvent()) {
			Duration toNextExec = Duration.between(ZonedDateTime.now(), store.getNextExecutionTime());
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
