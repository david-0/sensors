package org.sensors.backend;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mbelling.ws281x.Color;
import com.github.mbelling.ws281x.LedStripType;
import com.github.mbelling.ws281x.Ws281xLedStrip;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class App {
	private static final Logger logger = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) throws UnsupportedBusNumberException,
			IOException, InterruptedException, ExecutionException {
		I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);

		Ws281xLedStrip ws281xLedStrip = new Ws281xLedStrip(8, 18, 100000, 1, 100, 0, false, LedStripType.WS2811_STRIP_RGB, false);
		ws281xLedStrip.setPixel(0, Color.BLUE);
		
		Controller controller = new Controller(bus,
				new KafkaProducer<>(App.createProducerProperties()),
				new KafkaConsumer<>(createConsumerProperties()));
		controller.init();
		controller.run();
		logger.info("controller started");
	}

	private static Properties createConsumerProperties() {
		Properties props = new Properties();
		props.put("bootstrap.servers", "localhost:9092");
		props.put("group.id", "test");
		props.put("enable.auto.commit", "true");
		props.put("auto.commit.interval.ms", "1000");
		props.put("key.deserializer",
				"org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer",
				"org.apache.kafka.common.serialization.StringDeserializer");
		return props;
	}

	private static Properties createProducerProperties() {
		Properties props = new Properties();
		props.put("bootstrap.servers", "localhost:9092");
		props.put("acks", "all");
		props.put("delivery.timeout.ms", 30000);
		props.put("batch.size", 16384);
		props.put("linger.ms", 1);
		props.put("buffer.memory", 33554432);
		props.put("key.serializer",
				"org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer",
				"org.apache.kafka.common.serialization.StringSerializer");
		return props;
	}
}
