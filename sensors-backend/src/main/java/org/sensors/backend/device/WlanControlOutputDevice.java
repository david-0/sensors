package org.sensors.backend.device;

import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.sensors.backend.sensor.handler.ChangeEventListener;
import org.sensors.backend.sensor.handler.EventBasedSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WlanControlOutputDevice implements ChangeEventListener, EventBasedSource {

	private static final Logger logger = LoggerFactory.getLogger(WlanControlOutputDevice.class);

	private String id;

	private BiConsumer<String, String> eventChange;

	public WlanControlOutputDevice(String id) {
		this.id = id;
	}

	public WlanControlOutputDevice init() {
		return this;
	}

	/**
	 * valid values are: on = WLAN-ON and off =WLAN_OFF
	 */
	@Override
	public boolean onSettingChange(String key, String value) {
		if (id.equals(key)) {
			if ("on".equals(value)) {
				switchWlanOn();
			} else if ("off".equals(value)) {
				switchWlanOff();
			} else {
				throw new RuntimeException(
						"Invalid value '" + value + "' for " + WlanControlOutputDevice.class.getSimpleName()
								+ " with id '" + key + "', valid values: [on, off]");
			}
			return true;
		}
		return false;
	}

	private void switchWlanOff() {
		eventChange.accept(id + "-wlan", "switching on");
		try {
			logger.info("Switch WLAN off");
			TimeUnit.MILLISECONDS.sleep(100);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		eventChange.accept(id + "-wlan", "switched on");

	}

	private void switchWlanOn() {
		eventChange.accept(id + "-wlan", "switching off");
		try {
			logger.info("Switch WLAN on");
			TimeUnit.MILLISECONDS.sleep(100);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		eventChange.accept(id + "-wlan", "switched off");
	}

	@Override
	public void onChange(BiConsumer<String, String> eventChange) {
		this.eventChange = eventChange;
	}

}
