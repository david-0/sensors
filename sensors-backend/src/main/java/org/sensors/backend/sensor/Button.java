package org.sensors.backend.sensor;

import java.util.function.BiConsumer;

import org.sensors.backend.sensor.handler.EventBasedSource;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class Button implements EventBasedSource {
	private final GpioController gpio;
	private BiConsumer<String, Boolean> eventChange;
	private Pin pin;
	private PinPullResistance resistance;
	private String id;

	public Button(GpioController gpio, Pin pin, PinPullResistance resistance, String id) {
		this.gpio = gpio;
		this.pin = pin;
		this.resistance = resistance;
		this.id = id;
	}

	public void onChange(BiConsumer<String, Boolean> eventChange) {
		this.eventChange = eventChange;
	}

	public void init() {
		GpioPinDigitalInput ledButton = gpio.provisionDigitalInputPin(pin, resistance);
		ledButton.setShutdownOptions(true);
		ledButton.addListener(new GpioPinListenerDigital() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
				if (eventChange != null) {
					eventChange.accept(id, PinState.HIGH.equals(event.getState()));
				}
			}
		});
	}
}
