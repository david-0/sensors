package org.sensors.logic;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class LedTimeLimiter {
	
	private static final Duration SWITCH_OFF_DELAY = Duration.ofSeconds(4);

	private ScheduledExecutorService executor;
	private Optional<ScheduledFuture<?>> future = Optional.empty();

	private Runnable switchOff;

	public LedTimeLimiter(Runnable switchOff) {
		this.switchOff = switchOff;
		executor = Executors.newSingleThreadScheduledExecutor();
	}
	
	public void start() {
		synchronized (executor) {
			future.ifPresent(this::cancel);
			future = Optional.of(executor. schedule(switchOff::run, SWITCH_OFF_DELAY.toMillis(), TimeUnit.MILLISECONDS));
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
	}
}
