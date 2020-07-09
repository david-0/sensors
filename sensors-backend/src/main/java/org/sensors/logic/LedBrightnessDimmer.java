package org.sensors.logic;

import java.time.Duration;
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
	private boolean isDimming = false;

	private ScheduledExecutorService executor;
	private Optional<ScheduledFuture<?>> future = Optional.empty();

	public LedBrightnessDimmer(Supplier<Integer> getBrightness, Consumer<Integer> setBrightness) {
		this.getBrightness = getBrightness;
		this.setBrightness = setBrightness;
		executor = Executors.newSingleThreadScheduledExecutor();
	}

	public void start() {
		synchronized (executor) {
			future.ifPresent(this::cancel);
			isDimming = false;
			future = Optional.of(executor.scheduleAtFixedRate(this::dimm, START_DELAY.toMillis(),
					REPETITION_DELAY.toMillis(), TimeUnit.MILLISECONDS));
		}
	}

	public boolean isDimming() {
		return isDimming;
	}

	public void stop() {
		synchronized (executor) {
			future.ifPresent(this::cancel);
			future = Optional.empty();
			isDimming = false;
		}
	}

	private void cancel(ScheduledFuture<?> future) {
		future.cancel(false);
	}

	private void dimm() {
		isDimming = true;
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
