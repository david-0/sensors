package org.sensors.backend.device;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;

public class DigialOutputDevice {
	private GpioController gpio;
	private Pin pin;
	private GpioPinDigitalOutput outputDevice;

	public DigialOutputDevice(GpioController gpio, Pin pin) {
		this.gpio = gpio;
		this.pin = pin;
	}

	public DigialOutputDevice init() {
		outputDevice = gpio.provisionDigitalOutputPin(pin);
		return this;
	}

	public void on() {
		outputDevice.setState(true);
	}
	
	public void off() {
		outputDevice.setState(false);
	}
}
