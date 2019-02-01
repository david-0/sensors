package org.sensors.backend;

import java.util.function.Supplier;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Execution {
	private static final Logger logger = LoggerFactory
			.getLogger(Execution.class);
	private final KafkaProducer<String, String> kafkaProducer;
	private Supplier<?> valueProvider;
	private String topic;

	public Execution(KafkaProducer<String, String> kafkaProducer, String topic,
			Supplier<?> valueProvider) {
		this.kafkaProducer = kafkaProducer;
		this.topic = topic;
		this.valueProvider = valueProvider;
	}

	public void run() throws JsonProcessingException {
		Object value = valueProvider.get();
		String json = new ObjectMapper().writeValueAsString(value);
		logger.info("topic: {}, json: {}", topic, json);
		ProducerRecord<String, String> record = new ProducerRecord<>(topic,
				json);
		kafkaProducer.send(record);
	}

	public String getTopic() {
		return topic;
	}
}
