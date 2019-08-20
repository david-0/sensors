package org.sensors.logic;

import java.io.IOException;
import java.util.Map;

import org.apache.kafka.common.serialization.Serializer;
import org.sensors.api.AllLedChange;
import org.sensors.api.json.mixin.AllLedChangeMixin;
import org.sensors.api.json.mixin.ColorMixin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mbelling.ws281x.Color;

public class AllLedChangeSerializer implements Serializer<AllLedChange> {

	private static final Logger logger = LoggerFactory.getLogger(AllLedChangeSerializer.class);

	private final ObjectMapper mapper = new ObjectMapper();

	public AllLedChangeSerializer() {
		mapper.addMixIn(AllLedChange.class, AllLedChangeMixin.class);
		mapper.addMixIn(Color.class, ColorMixin.class);
	}

	@Override
	public void close() {
	}

	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
	}

	@Override
	public byte[] serialize(String topic, AllLedChange data) {
		try {
			return mapper.writeValueAsBytes(data);
		} catch (IOException e) {
			logger.info("", e);
			return null;
		}
	}
}
