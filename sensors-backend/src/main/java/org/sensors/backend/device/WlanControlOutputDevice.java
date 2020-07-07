package org.sensors.backend.device;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Switch off or on the wlan of the raspi
 */
public class WlanControlOutputDevice  {

	private static final Logger logger = LoggerFactory.getLogger(WlanControlOutputDevice.class);

	public WlanControlOutputDevice() {
	}

	public void switchWlanOff() {
		try {
			logger.info("Switch WLAN off");
			TimeUnit.MILLISECONDS.sleep(100);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public void switchWlanOn() {
		try {
			logger.info("Switch WLAN on");
			TimeUnit.MILLISECONDS.sleep(100);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
