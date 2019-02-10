package org.sensors.backend.device;

import java.io.IOException;

import org.sensors.api.AllLedChange;
import org.sensors.api.BrightnessChange;
import org.sensors.api.MultiLedChange;
import org.sensors.api.OneLedChange;
import org.sensors.api.json.mixin.AllLedChangeMixin;
import org.sensors.api.json.mixin.BrightnessChangeMixin;
import org.sensors.api.json.mixin.ColorMixin;
import org.sensors.api.json.mixin.MultiLedChangeMixin;
import org.sensors.api.json.mixin.OneLedChangeMixin;
import org.sensors.backend.ChangeEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
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

public class LedStrip implements ChangeEventListener {

	private static final Logger logger = LoggerFactory.getLogger(LedStrip.class);

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
		leds = new Ws281xLedStrip(ledCount, 18, 800000, 10, 255, 0, false, LedStripType.WS2811_STRIP_GRB, true);
		return this;
	}

	@Override
	public boolean onSettingChange(String key, String value) {
		try {
			if (key.startsWith(id + "-one-")) {
				OneLedChange one = mapper.readValue(value, OneLedChange.class);
				leds.setPixel(one.getNumber(), one.getColor());
				leds.render();
				return true;
			}
			if ((id + "-all").equals(key)) {
				AllLedChange all = mapper.readValue(value, AllLedChange.class);
				leds.setStrip(all.getColor());
				leds.render();
				return true;
			}
			if ((id + "-multi").equals(key)) {
				MultiLedChange multi = mapper.readValue(value, MultiLedChange.class);
				multi.getList().stream().forEach(o -> leds.setPixel(o.getNumber(), o.getColor()));
				leds.render();
				return true;
			}
			if ((id + "-brightness").equals(key)) {
				BrightnessChange brightness = mapper.readValue(value, BrightnessChange.class);
				leds.setBrightness(brightness.getBrightness());
				if (brightness.isRender()) {
					leds.render();
				}
				return true;
			}
			return false;
		} catch (IOException e) {
			logger.warn("ignore key: " + key + ", value: " + value, e);
			return true;
		}
	}

}
