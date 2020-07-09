package org.sensors.logic;

import org.sensors.backend.device.LedStrip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LedButtonProcessor implements ButtonProcessor {
	private static final Logger logger = LoggerFactory.getLogger(LedButtonProcessor.class);

	private boolean on = false;
	private boolean stateBeforePressed = false;
	private int brightness = 255;
	private LedStrip ledStrip;

	private LedBrightnessFader fader;

	public LedButtonProcessor(LedStrip ledStrip) {
		this.ledStrip = ledStrip;
		fader = new LedBrightnessFader(this::getBrightness, this::setBrightness);
	}

	private int getBrightness() {
		return brightness;
	}
	
	private void setBrightness(int brightness) {
		this.brightness = brightness;
		ledStrip.onAll(brightness);
	}
	
	@Override
	public void update(ButtonState state) {
		logger.info("ledButtonChanged: " + state.name());
		if (state.isContactClosed()) {
			stateBeforePressed = on;
			fader.startFade();
			if (!on) {
				on = true;
			}
		} else {
			if (fader.isFaderStarted() && stateBeforePressed) {
				on = false;
			}
			fader.stopFade();
		}
		if (on) {
			ledStrip.onAll(brightness);
		} else {
			ledStrip.offAll();
		}
	}
}
