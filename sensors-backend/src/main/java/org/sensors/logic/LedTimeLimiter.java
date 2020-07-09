package org.sensors.logic;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LedTimeLimiter {
	
	private static final Logger logger = LoggerFactory.getLogger(LedTimeLimiter.class);
	private static final Duration SWITCH_OFF_DELAY = Duration.ofSeconds(4);

	private ScheduledExecutorService executor;
	private Optional<ScheduledFuture<?>> future = Optional.empty();
	private Optional<ZonedDateTime> startTime = Optional.empty();

	private Runnable switchOff;

	public LedTimeLimiter(Runnable switchOff) {
		this.switchOff = switchOff;
		executor = Executors.newSingleThreadScheduledExecutor();
	}
	
	public void start() {
		synchronized (executor) {
			if (startTime.isPresent()) {
				logger.info("restart");
			} else {
				logger.info("start");
			}
			future.ifPresent(this::cancel);
			startTime = Optional.of(ZonedDateTime.now());
			future = Optional.of(executor. schedule(this::switchOff, SWITCH_OFF_DELAY.toMillis(), TimeUnit.MILLISECONDS));
		}
	}

	public boolean isStarted() {
		synchronized (executor) {
			ZonedDateTime now = ZonedDateTime.now();
			Boolean result = startTime //
					.map(t -> t.plus(SWITCH_OFF_DELAY)) //
					.map(t -> t.isAfter(now)) //
					.orElse(false);
			return result;
		}
	}

	public void stop() {
		synchronized (executor) {
			if (startTime.isPresent()) {
				logger.info("stop");
			}
			startTime = Optional.empty();
			future.ifPresent(this::cancel);
			future = Optional.empty();
		}
	}	
	
	private void cancel(ScheduledFuture<?> future) {
		future.cancel(false);
		startTime = Optional.empty();
	}
	
	private void switchOff() {
		switchOff.run();
	}
}
