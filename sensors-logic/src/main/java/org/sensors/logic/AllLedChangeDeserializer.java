package org.sensors.logic;

import java.io.IOException;
import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.sensors.api.AllLedChange;
import org.sensors.api.json.mixin.AllLedChangeMixin;
import org.sensors.api.json.mixin.ColorMixin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mbelling.ws281x.Color;

public class AllLedChangeDeserializer implements Deserializer<AllLedChange> {

	private static final Logger logger = LoggerFactory.getLogger(AllLedChangeDeserializer.class);

	private final ObjectMapper mapper = new ObjectMapper();

	public AllLedChangeDeserializer() {
		mapper.addMixIn(AllLedChange.class, AllLedChangeMixin.class);
		mapper.addMixIn(Color.class, ColorMixin.class);
	}

	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
	}

	@Override
	public AllLedChange deserialize(String topic, byte[] data) {
		try {
			return mapper.readValue(data, AllLedChange.class);
		} catch (IOException e) {
			logger.info("", e);
			return null;
		}
	}

	@Override
	public void close() {
	}

}
