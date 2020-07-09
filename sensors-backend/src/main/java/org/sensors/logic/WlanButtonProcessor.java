package org.sensors.logic;

import org.sensors.backend.device.DigialOutputDevice;
import org.sensors.backend.device.WlanControlOutputDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WlanButtonProcessor {
	private static final Logger logger = LoggerFactory.getLogger(WlanButtonProcessor.class);

	private boolean stateBeforePressed = false;

	private DigialOutputDevice wlanButtonLed;
	private WlanControlOutputDevice wlanOutputDevice;

	public WlanButtonProcessor(DigialOutputDevice wlanButtonLed, WlanControlOutputDevice wlanOutputDevice) {
		this.wlanButtonLed = wlanButtonLed;
		this.wlanOutputDevice = wlanOutputDevice;
	}

	public void update(ButtonState state) {
		logger.info("wlanButtonChanged: " + state.name());
		if (state.isContactClosed()) {
			stateBeforePressed = wlanButtonLed.isOn();
			if (!wlanButtonLed.isOn()) {
				wlanButtonLed.on();
				wlanOutputDevice.switchWlanOn();
			}
		} else {
			if (stateBeforePressed) {
				wlanButtonLed.off();
				wlanOutputDevice.switchWlanOff();
			}
		}
	}
}
