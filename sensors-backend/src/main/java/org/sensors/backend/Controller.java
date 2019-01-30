package org.sensors.backend;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.pi4j.io.i2c.I2CBus;

public class Controller {
	private boolean initialized;
	private I2CBus bus;
	private SensorMcp9808 sensor1;
	private SensorMcp9808 sensor2;
	private KafkaProducer<Float, Float> producer;
	private ScheduledExecutorService executor;

	public Controller(I2CBus bus) {
		this.bus = bus;
	}

	public void init() throws InterruptedException, ExecutionException {
		if (initialized) {
			throw new IllegalStateException("Controller already initialized");
		}
		sensor1 = new SensorMcp9808(bus, 0x18, "Controller");
		sensor2 = new SensorMcp9808(bus, 0x1C, "Unbekannt");
		AsyncCombiner.allOf(sensor1::init, sensor2::init).get();
		producer = new KafkaProducer<>(createProducerProperties());
		executor = Executors.newSingleThreadScheduledExecutor();
	}

	public void run() {
		executor.scheduleAtFixedRate(() -> producer.send(createRecord(sensor1.readTemperature())), 1000, 1000,
				TimeUnit.MILLISECONDS);
	}

	public ProducerRecord<Float, Float> createRecord(double value) {
		Float temp = Float.valueOf(Double.valueOf(value).floatValue());
		return new ProducerRecord<>("sensor1", temp);
	}

	private Properties createProducerProperties() {
		Properties props = new Properties();
		props.put("bootstrap.servers", "localhost:9092");
		props.put("acks", "all");
		props.put("delivery.timeout.ms", 30000);
		props.put("batch.size", 16384);
		props.put("linger.ms", 1);
		props.put("buffer.memory", 33554432);
		props.put("key.serializer", "org.apache.kafka.common.serialization.FloatSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.FloatSerializer");
		return props;
	}
}
