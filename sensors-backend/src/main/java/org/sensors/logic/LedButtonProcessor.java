package org.sensors.logic;

import org.sensors.backend.device.LedStrip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LedButtonProcessor implements ButtonProcessor {
	private static final Logger logger = LoggerFactory.getLogger(LedButtonProcessor.class);

	private boolean stateBeforePressed = false;
	private int brightness = 255;
	private LedStrip ledStrip;

	private LedBrightnessDimmer dimmer;
	private LedTimeLimiter limiter;

	public LedButtonProcessor(LedStrip ledStrip) {
		this.ledStrip = ledStrip;
		dimmer = new LedBrightnessDimmer(this::getBrightness, this::dimmerSetBrightness);
		limiter = new LedTimeLimiter(this::limiterSwitchOff);
	}

	private int getBrightness() {
		return brightness;
	}

	private void dimmerSetBrightness(int brightness) {
		this.brightness = brightness;
		ledStrip.onAll(brightness);
		limiter.stop();
	}
	
	private void limiterSwitchOff() {
		if (ledStrip.isOn()) {
			ledStrip.offAll();
		}
	}

	@Override
	public void update(ButtonState state) {
		logger.info("ledButtonChanged: " + state.name());
		limiter.start();
		if (state.isContactClosed()) {
			stateBeforePressed = ledStrip.isOn();
			dimmer.start();
			if (!ledStrip.isOn()) {
				ledStrip.onAll(brightness);
			}
		} else {
			boolean dimmerStarted = dimmer.isDimming();
			dimmer.stop();
			if (!dimmerStarted && stateBeforePressed) {
				ledStrip.offAll();
				limiter.stop();
			}
		}
	}
}
