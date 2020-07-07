package org.sensors.backend.device;

import com.github.mbelling.ws281x.Color;
import com.github.mbelling.ws281x.LedStripType;
import com.github.mbelling.ws281x.Ws281xLedStrip;

/**
 * settings for the led-strip:<br/>
 * <i>At least the last setting of every key is available after restart,
 * possible more than one. The order is always respected.</i>
 * <ul>
 * <li><b>key:</b> led-strip-one-[0-n]<br/>
 * <b>value:</b> {@link OneLedChange} in JSON</li>
 * <li><b>key:</b> led-strip-all<br/>
 * <b>value:</b> {@link AllLedChange} in JSON</li>
 * <li><b>key:</b> led-strip-multi<br/>
 * <b>value:</b> {@link MultiLedChange} in JSON</li>
 * <li><b>key:</b> led-strip-brightness<br/>
 * <b>value:</b> {@link BrightnessChange} in JSON</li>
 * </ul>
 */

public class LedStrip {

	private int ledCount;
	private Ws281xLedStrip leds;

	public LedStrip(int ledCount) {
		this.ledCount = ledCount;
	}

	public LedStrip init() {
		leds = new Ws281xLedStrip(ledCount, 18, 800000, 10, 255, 0, false, LedStripType.WS2811_STRIP_GRB, true);
		return this;
	}

	public void onAll() {
		leds.setStrip(new Color(255,255,255));
		leds.setBrightness(255);
		leds.render();
	}
	
	public void offAll() {
		leds.setStrip(new Color(255,255,255));
//		leds.setPixel(one.getNumber(), one.getColor());
		leds.setBrightness(0);
		leds.render();
	}
}
