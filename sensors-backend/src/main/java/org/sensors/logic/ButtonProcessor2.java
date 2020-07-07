package org.sensors.logic;

public class ButtonProcessor2 {

	public boolean updateButton(String key, Boolean pressed) {
		if ("led-button".equals(key)) {
			return updateLedButton(pressed.booleanValue());
		}
		if ("wlan-button".equals(key)) {
			return updateWlanButton(pressed.booleanValue());
		}
		return false;
	}
	
	private boolean updateLedButton(final boolean pressed) {
		return pressed;
	}

	private boolean updateWlanButton(final boolean pressed) {
		return pressed;
	}
}
