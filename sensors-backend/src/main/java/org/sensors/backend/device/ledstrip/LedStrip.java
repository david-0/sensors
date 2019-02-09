package org.sensors.backend.device.ledstrip;

import java.io.IOException;

import org.sensors.backend.ChangeEventListener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mbelling.ws281x.LedStripType;
import com.github.mbelling.ws281x.Ws281xLedStrip;

public class LedStrip implements ChangeEventListener {
	private String id;
	private int ledCount;
	private Ws281xLedStrip leds;

	public LedStrip(String id, int ledCount) {
		this.id = id;
		this.ledCount = ledCount;
	}

	public LedStrip init() {
		leds = new Ws281xLedStrip(ledCount, 18, 800000, 10, 255, 0, false, LedStripType.WS2811_STRIP_GBR, true);
		return this;
	}

	@Override
	public boolean onSettingChange(String key, String value) {
		try {
			if ((id + "-one").equals(key)) {
				OneLedChange one = new ObjectMapper().readValue(value, OneLedChange.class);
				leds.setPixel(one.getNumber(), one.getColor());
				leds.render();
				return true;
			}
			if ((id + "-all").equals(key)) {
				AllLedChange all = new ObjectMapper().readValue(value, AllLedChange.class);
				leds.setStrip(all.getColor());
				leds.render();
				return true;
			}
			if ((id + "-multi").equals(key)) {
				MultiLedChange multi = new ObjectMapper().readValue(value, MultiLedChange.class);
				multi.getMultiple().stream().forEach(o -> leds.setPixel(o.getNumber(), o.getColor()));
				leds.render();
				return true;
			}
			if ((id + "-brightness").equals(key)) {
				BrightnessChange brightness = new ObjectMapper().readValue(value, BrightnessChange.class);
				leds.setBrightness(brightness.getBrightness());
				if (brightness.isRender()) {
					leds.render();
				}
				return true;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return false;
	}

}
