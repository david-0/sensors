package org.sensors.backend.device.ledstrip;

import java.io.IOException;

import org.sensors.backend.ChangeEventListener;
import org.sensors.backend.json.mixin.AllLedChangeMixin;
import org.sensors.backend.json.mixin.BrightnessChangeMixin;
import org.sensors.backend.json.mixin.ColorMixin;
import org.sensors.backend.json.mixin.MultiLedChangeMixin;
import org.sensors.backend.json.mixin.OneLedChangeMixin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mbelling.ws281x.Color;
import com.github.mbelling.ws281x.LedStripType;
import com.github.mbelling.ws281x.Ws281xLedStrip;

public class LedStrip implements ChangeEventListener {
	private String id;
	private int ledCount;
	private Ws281xLedStrip leds;
	private ObjectMapper mapper;

	public LedStrip(String id, int ledCount) {
		this.id = id;
		this.ledCount = ledCount;
		mapper = new ObjectMapper();
		mapper.addMixIn(Color.class, ColorMixin.class);
		mapper.addMixIn(OneLedChange.class, OneLedChangeMixin.class);
		mapper.addMixIn(AllLedChange.class, AllLedChangeMixin.class);
		mapper.addMixIn(MultiLedChange.class, MultiLedChangeMixin.class);
		mapper.addMixIn(BrightnessChange.class, BrightnessChangeMixin.class);
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
				multi.getList().stream().forEach(o -> leds.setPixel(o.getNumber(), o.getColor()));
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
