package org.sensors.logic;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LedBrightnessDimmer {

	private static final int MIN_VALUE = 1;
	private static final int MAX_VALUE = 255;
	private static final Duration START_DELAY = Duration.ofMillis(500);
	private static final Duration REPETITION_DELAY = Duration.ofMillis(10);

	private Supplier<Integer> getBrightness;
	private Consumer<Integer> setBrightness;
	private boolean dimmDown = true;

	private ScheduledExecutorService executor;
	private Optional<ScheduledFuture<?>> future = Optional.empty();
	private Optional<ZonedDateTime> startTime = Optional.empty();

	public LedBrightnessDimmer(Supplier<Integer> getBrightness, Consumer<Integer> setBrightness) {
		this.getBrightness = getBrightness;
		this.setBrightness = setBrightness;
		executor = Executors.newSingleThreadScheduledExecutor();
	}

	public void start() {
		synchronized (executor) {
			future.ifPresent(this::cancel);
			startTime = Optional.of(ZonedDateTime.now());
			future = Optional.of(executor.scheduleAtFixedRate(this::dimm, START_DELAY.toMillis(),
					REPETITION_DELAY.toMillis(), TimeUnit.MILLISECONDS));
		}
	}

	public boolean isStarted() {
		synchronized (executor) {
			ZonedDateTime now = ZonedDateTime.now();
			Boolean result = startTime //
					.map(t -> t.plus(START_DELAY)) //
					.map(t -> t.isAfter(now)) //
					.orElse(false);
			return result;
		}
	}

	public void stop() {
		synchronized (executor) {
			future.ifPresent(this::cancel);
			future = Optional.empty();
		}
	}

	private void cancel(ScheduledFuture<?> future) {
		future.cancel(false);
		startTime = Optional.empty();
	}

	private void dimm() {
		if (dimmDown) {
			if (getBrightness.get().intValue() == MIN_VALUE) {
				dimmDown = false;
			} else {
				setBrightness.accept(Integer.valueOf(getBrightness.get().intValue() - 1));
			}
		} else {
			if (getBrightness.get().intValue() == MAX_VALUE) {
				dimmDown = true;
			} else {
				setBrightness.accept(Integer.valueOf(getBrightness.get().intValue() + 1));
			}
		}
	}
}
