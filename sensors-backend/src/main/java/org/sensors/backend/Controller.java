package org.sensors.backend;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;

import org.sensors.backend.event.IntervalEvent;
import org.sensors.backend.sensor.handler.IntervalBasedSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Controller {

	private static final Logger logger = LoggerFactory.getLogger(Controller.class);

	private final List<IntervalBasedSource> intervalBasedSources = new ArrayList<>();
	private boolean initialized;
	private EventStore store;
	private Future<?> execEventsFuture;
	private BiConsumer<String, Object> stateStore;

	public Controller(BiConsumer<String, Object> stateStore) {
		this.stateStore = stateStore;
		store = new EventStore();
	}

	public void addIntervalBasedSource(IntervalBasedSource source) {
		ensureNotAlreadyInitialized();
		this.intervalBasedSources.add(source);
	}

	public void init() throws InterruptedException, ExecutionException {
		ensureNotAlreadyInitialized();
		for (IntervalBasedSource source : intervalBasedSources) {
			store.addEvent(new IntervalEvent(source, ZonedDateTime.now(), (id, state) -> stateStore.accept(id, state)));
		}
		initialized = true;
	}

	public void run() {
		ExecutorService executor = Executors.newFixedThreadPool(2);
		execEventsFuture = executor.submit(this::execEvents);
	}

	public void waitMainThread() {
		try {
			execEventsFuture.get();
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

	public void stop() {
		logger.info("wait for shutdown ...");
		if (!execEventsFuture.isCancelled()) {
			execEventsFuture.cancel(true);
		}
		logger.info("shutdown completed");
	}
}
