package org.sensors.logic;

import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.sensors.backend.device.LedStrip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LedButtonProcessor implements ButtonProcessor {
	private static final Logger logger = LoggerFactory.getLogger(LedButtonProcessor.class);

	private boolean on = false;
	private boolean stateBeforePressed = false;
	private int brightness = 255;
	private LedStrip ledStrip;
	private ZonedDateTime lastPressed;

	private ScheduledExecutorService faderExecutor;

	private ScheduledFuture<?> faderFuture;

	public LedButtonProcessor(LedStrip ledStrip) {
		this.ledStrip = ledStrip;
		faderExecutor = Executors.newSingleThreadScheduledExecutor();
	}

	@Override
	public void update(ButtonState state) {
		logger.info("ledButtonChanged: " + state.name());
		ZonedDateTime now = ZonedDateTime.now();
		if (state.isContactClosed()) {
			lastPressed = now;
			stateBeforePressed = on;
			if (faderFuture != null) {
				faderFuture.cancel(false);
			}
			if (brightness == 255) {
				faderFuture = faderExecutor.scheduleAtFixedRate(this::fadeDown, 500, 10, TimeUnit.MILLISECONDS);
			} else {
				faderFuture = faderExecutor.scheduleAtFixedRate(this::fadeUp, 500, 10, TimeUnit.MILLISECONDS);
			}
			if (!on) {
				on = true;
			}
		} else {
			faderFuture.cancel(false);
			if (stateBeforePressed && lastPressed.isAfter(now.minusNanos(500_000_000))) {
				on = false;
			}
		}
		if (on) {
			ledStrip.onAll(brightness);
		} else {
			ledStrip.offAll();
		}
	}

	private void fadeDown() {
		if (brightness > 1) {
			brightness -= 1;
			ledStrip.onAll(brightness);
		}
	}

	private void fadeUp() {
		if (brightness < 255) {
			brightness += 1;
			ledStrip.onAll(brightness);
		}
	}
}
