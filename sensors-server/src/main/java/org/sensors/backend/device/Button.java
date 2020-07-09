package org.sensors.backend.device;

import java.util.function.Consumer;

import org.sensors.logic.ButtonState;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class Button {
	private final GpioController gpio;
	private Consumer<ButtonState> eventChange;
	private Pin pin;
	private boolean pressedHigh;

	public Button(GpioController gpio, Pin pin, boolean pressedHigh) {
		this.gpio = gpio;
		this.pin = pin;
		this.pressedHigh = pressedHigh;
	}

	public void onChange(Consumer<ButtonState> eventChange) {
		this.eventChange = eventChange;
	}

	public Button init() {
		GpioPinDigitalInput ledButton = gpio.provisionDigitalInputPin(pin);
		ledButton.setShutdownOptions(true);
		ledButton.addListener(new GpioPinListenerDigital() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
				if (eventChange != null) {
					boolean state = PinState.HIGH.equals(event.getState());
					boolean pressed = pressedHigh ? state : !state;
					eventChange.accept(pressed?ButtonState.ON:ButtonState.OFF);
				}
			}
		});
		return this;
	}
}
