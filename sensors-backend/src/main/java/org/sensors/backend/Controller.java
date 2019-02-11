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
import org.apache.kafka.common.TopicPartition;
import org.sensors.backend.event.FrequencyEvent;
import org.sensors.backend.event.IntervalEvent;
import org.sensors.backend.sensor.handler.ChangeEventListener;
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

	private Future<?> execEventsFuture;
	private Future<?> consumeMessagesFuture;

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
			source.onChange((id, state) -> sendMessage("events", id, state));
		}
		TopicPartition topicPartition = new TopicPartition(SETTING_TOPIC, 0);
		consumer.assign(Arrays.asList(topicPartition));
		consumer.seekToBeginning(Arrays.asList(topicPartition));
		initialized = true;
	}

	private Future<RecordMetadata> sendMessage(String topic, String key, String value) {
		return producer.send(new ProducerRecord<String, String>(topic, key, value));
	}

	public void run() {
		ExecutorService executor = Executors.newFixedThreadPool(2);
		execEventsFuture = executor.submit(this::execEvents);
		consumeMessagesFuture = executor.submit(this::consumeMessages);
	}

	public void waitMainThread() {
		try {
			execEventsFuture.get();
			consumeMessagesFuture.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private void ensureNotAlreadyInitialized() {
		if (initialized) {
			throw new IllegalStateException("alreay initialized");
		}
	}

	private void execEvents() {
		while (!execEventsFuture.isCancelled()) {
			store.getNextEvent().exec();
		}
	}

	private void consumeMessages() {
		try {
			while (true) {
				consumeRecords(Duration.ofSeconds(1));
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
		logger.info("onMessage: key={}, value={}", key, value);
		Optional<ChangeEventListener> findAny = changeEventListeners.stream() //
				.filter(l -> l.onSettingChange(key, value)).findAny();
		if (!findAny.isPresent()) {
			logger.warn("No handler for key: '{}', value: '{}'", key, value);
		}
	}

	public void stop() {
		logger.info("wait for shutdown ...");
		if (!execEventsFuture.isCancelled()) {
			execEventsFuture.cancel(true);
		}
		logger.info("shutdown completed");
	}

}
