package org.sensors.logic;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LedBrightnessFader {

	private static final int MIN_VALUE = 1;
	private static final int MAX_VALUE = 255;
	private static final Duration START_DELAY = Duration.ofMillis(500);
	private static final Duration REPETITION_DELAY = Duration.ofMillis(10);

	private Supplier<Integer> getBrightness;
	private Consumer<Integer> setBrightness;
	private boolean fadeDown = true;

	private ScheduledExecutorService faderExecutor;
	private Optional<ScheduledFuture<?>> faderFuture = Optional.empty();
	private Optional<ZonedDateTime> startTime = Optional.empty();

	public LedBrightnessFader(Supplier<Integer> getBrightness, Consumer<Integer> setBrightness) {
		this.getBrightness = getBrightness;
		this.setBrightness = setBrightness;
		faderExecutor = Executors.newSingleThreadScheduledExecutor();
	}

	public void startFade() {
		synchronized (faderExecutor) {
			faderFuture.ifPresent(this::cancelAndWait);
			startTime = Optional.of(ZonedDateTime.now());
			faderFuture = Optional.of(faderExecutor.scheduleAtFixedRate(this::fade, START_DELAY.toMillis(),
					REPETITION_DELAY.toMillis(), TimeUnit.MILLISECONDS));
		}
	}

	public boolean isFaderStarted() {
		synchronized (faderExecutor) {
			ZonedDateTime now = ZonedDateTime.now();
			Boolean result = startTime //
					.map(t -> t.plus(START_DELAY)) //
					.map(t -> t.isAfter(now)) //
					.orElse(false);
			return result;
		}
	}

	public void stopFade() {
		synchronized (faderExecutor) {
			faderFuture.ifPresent(this::cancelAndWait);
			faderFuture = Optional.empty();
		}
	}

	private void cancelAndWait(ScheduledFuture<?> future) {
		future.cancel(false);
		startTime = Optional.empty();
	}

	private void fade() {
		if (fadeDown) {
			if (getBrightness.get().intValue() == MIN_VALUE) {
				fadeDown = false;
			} else {
				setBrightness.accept(Integer.valueOf(getBrightness.get().intValue() - 1));
			}
		} else {
			if (getBrightness.get().intValue() == MAX_VALUE) {
				fadeDown = true;
			} else {
				setBrightness.accept(Integer.valueOf(getBrightness.get().intValue() + 1));
			}
		}
	}
}
