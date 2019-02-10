package org.sensors.backend.device;

import org.sensors.backend.ChangeEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;

public class DigialOutputDevice implements ChangeEventListener {
	private static final Logger logger = LoggerFactory.getLogger(DigialOutputDevice.class);

	private GpioController gpio;
	private Pin pin;
	private String id;
	private GpioPinDigitalOutput outputDevice;

	public DigialOutputDevice(GpioController gpio, Pin pin, String id) {
		this.gpio = gpio;
		this.pin = pin;
		this.id = id;
	}

	public DigialOutputDevice init() {
		outputDevice = gpio.provisionDigitalOutputPin(pin);
		return this;
	}

	/**
	 * Valid values are: 0 = LOW and 1 = HIGH
	 */
	@Override
	public boolean onSettingChange(String key, String value) {
		if (id.equals(key)) {
			if ("0".equals(value)) {
				outputDevice.setState(false);
			} else if ("1".equals(value)) {
				outputDevice.setState(true);
			} else {
				logger.warn("Ignore: Invalid value '" + value + "' for digitalOutputDevice with id '" + key
						+ "', valid values: [0, 1]");
			}
			return true;
		}
		return false;
	}
}
