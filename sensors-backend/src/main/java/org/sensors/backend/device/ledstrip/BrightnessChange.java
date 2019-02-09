package org.sensors.backend.device.ledstrip;

public class BrightnessChange {
	private final int brightness;
	private final boolean render;

	public BrightnessChange(int brightness, boolean render) {
		this.brightness = brightness;
		this.render = render;
	}

	public int getBrightness() {
		return brightness;
	}

	public boolean isRender() {
		return render;
	}

}
