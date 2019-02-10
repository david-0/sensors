package org.sensors.logic;

import java.io.IOException;
import java.util.Properties;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.sensors.api.AllLedChange;
import org.sensors.api.BrightnessChange;
import org.sensors.api.MultiLedChange;
import org.sensors.api.OneLedChange;
import org.sensors.api.json.mixin.AllLedChangeMixin;
import org.sensors.api.json.mixin.BrightnessChangeMixin;
import org.sensors.api.json.mixin.ColorMixin;
import org.sensors.api.json.mixin.MultiLedChangeMixin;
import org.sensors.api.json.mixin.OneLedChangeMixin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mbelling.ws281x.Color;

public class ButtonProcessor {

	private static final Logger logger = LoggerFactory.getLogger(ButtonProcessor.class);

	private static final String WLAN_BUTTON = "wlan-button";
	private static final String LED_BUTTON = "led-button";
	private static final Class<Boolean> LED_BUTTON_CLASS = Boolean.class;
	private static final Class<Boolean> WLAN_BUTTON_CLASS = Boolean.class;

	private final ObjectMapper mapper;

	public ButtonProcessor() {
		mapper = new ObjectMapper();
		mapper.addMixIn(Color.class, ColorMixin.class);
		mapper.addMixIn(OneLedChange.class, OneLedChangeMixin.class);
		mapper.addMixIn(AllLedChange.class, AllLedChangeMixin.class);
		mapper.addMixIn(MultiLedChange.class, MultiLedChangeMixin.class);
		mapper.addMixIn(BrightnessChange.class, BrightnessChangeMixin.class);
	}

	public void run() {
		final Properties props = new Properties();
		props.put(StreamsConfig.APPLICATION_ID_CONFIG, "sensor-application");
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "10.1.1.135:9092");
		props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
		props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

		final StreamsBuilder builder = new StreamsBuilder();
		final KStream<String, String> events = builder.stream("events");
		events.filter((k, v) -> ledButtonCheck(k, v) || wlanButtonCheck(k, v))//
				.map((k, v) -> updateSettings(k, v)) //
				.to("settings", Produced.with(Serdes.String(), Serdes.String()));

		final KafkaStreams streams = new KafkaStreams(builder.build(), props);
		streams.start();
	}

	private boolean wlanButtonCheck(final String k, final String v) {
		return check(WLAN_BUTTON, WLAN_BUTTON_CLASS, k, v);
	}

	private boolean ledButtonCheck(final String k, final String v) {
		return check(LED_BUTTON, LED_BUTTON_CLASS, k, v);
	}

	private KeyValue<String, String> updateSettings(final String k, final String v) {
		try {
			logger.info("key/value: {}/{}", k, v);
			if (LED_BUTTON.equals(k)) {
				String value;
				final Boolean pressed = mapper.readValue(v, LED_BUTTON_CLASS);
				if (pressed) {
					value = mapper.writeValueAsString(new AllLedChange(255, new Color(255, 255, 255)));
				} else {
					value = mapper.writeValueAsString(new AllLedChange(0, new Color(0, 0, 0)));
				}
				return new KeyValue<String, String>("led-strip-all", value);
			}
			if (WLAN_BUTTON.equals(k)) {
				final Boolean pressed = mapper.readValue(v, WLAN_BUTTON_CLASS);
				final String value = mapper.writeValueAsString(pressed ? 1 : 0);
				return new KeyValue<String, String>("wlan-button-led", value);
			}
			throw new RuntimeException("no valid key/value pair: key=" + k + ", value=" + v);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean check(final String expectedKey, final Class<?> expectedClass, final String k, final String v) {
		try {
			if (expectedKey.equals(k)) {
				mapper.readValue(v, expectedClass);
				return true;
			}
		} catch (final IOException e) {
			logger.error("Invalid event: expected object of wrong typ. key=" + expectedKey + ", value="
					+ expectedClass.getSimpleName() + ", receivedValue:" + v, e);
		}
		return false;
	}
}
