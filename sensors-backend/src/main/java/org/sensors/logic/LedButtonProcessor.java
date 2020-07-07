package org.sensors.logic;

import org.sensors.backend.device.LedStrip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LedButtonProcessor implements ButtonProcessor{
	private static final Logger logger = LoggerFactory.getLogger(LedButtonProcessor.class);

	private boolean on = false;
	private LedStrip ledStrip;
	
	public LedButtonProcessor(LedStrip ledStrip) {
		this.ledStrip = ledStrip;
	}
	
	@Override
	public void update(ButtonState state) {
		logger.info("ledButtonChanged: " + state.name());
		on ^= state.isContactClosed();
		if (on) {
			ledStrip.onAll();
		}
		if (!on) {
			ledStrip.offAll();
		}
	}
}
