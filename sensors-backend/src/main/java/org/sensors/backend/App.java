package org.sensors.backend;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.sensors.backend.device.Button;
import org.sensors.backend.device.DigialOutputDevice;
import org.sensors.backend.device.SensorMcp9808;
import org.sensors.backend.device.SensorOneWireTemp;
import org.sensors.backend.device.WlanControlOutputDevice;
import org.sensors.backend.sensor.ina219.SensorIna219;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import com.pi4j.io.w1.W1Master;

public class App {
	private static final Logger logger = LoggerFactory.getLogger(App.class);

	public static void main(String[] args)
			throws UnsupportedBusNumberException, IOException, InterruptedException, ExecutionException {
		try {
			final I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
			final W1Master master = new W1Master();
			final GpioController gpio = GpioFactory.getInstance();

			Controller controller = new Controller(new KafkaProducer<>(App.createProducerProperties()),
					new KafkaConsumer<>(createConsumerProperties()));
			createMcp9808Sensors(bus).stream().forEach(controller::addIntervalBasedSource);
			createIna219Sensors(bus).stream()//
					.peek(controller::addStateUpdaterSource) //
					.forEach(controller::addIntervalBasedSource);
			createOneWireSensors(master).stream().forEach(controller::addIntervalBasedSource);
			controller.addEventBasedSource(new Button(gpio, RaspiPin.GPIO_03, true, "led-button").init());
			controller.addEventBasedSource(new Button(gpio, RaspiPin.GPIO_00, false, "wlan-button").init());
			controller.addSettingChangeEventListener(
					new DigialOutputDevice(gpio, RaspiPin.GPIO_02, "wlan-button-led").init());
			Stream.of(new WlanControlOutputDevice("wlan").init()) //
					.peek(controller::addSettingChangeEventListener) //
					.forEach(controller::addEventBasedSource);
			controller.init();
			controller.run();
			controller.waitMainThread();
			logger.info("controller started");
		} catch (Exception e) {
			logger.error("Abort main", e);
		}
	}

	private static List<SensorMcp9808> createMcp9808Sensors(I2CBus bus) {
		return Arrays.asList(//
				new SensorMcp9808(bus, 0x18, "controller", "Temp Controller").init(),
				new SensorMcp9808(bus, 0x1C, "unknown", "Temp Unbekannt").init());
	}

	private static List<SensorIna219> createIna219Sensors(I2CBus bus) {
		return Arrays.asList(//
				new SensorIna219(bus, 0x40, "ina219-led", "INA219 1 - LED").init(),
				new SensorIna219(bus, 0x41, "ina219-raspi", "INA219 2 - Raspi").init(),
				new SensorIna219(bus, 0x45, "ina219-input", "INA219 3 - Input").init());
	}

	private static List<SensorOneWireTemp> createOneWireSensors(W1Master master) {
		return Arrays.asList(//
				new SensorOneWireTemp(master, "28-0000046d50e7", "T1-aussen", "Abwassertank aussen").init(),
				new SensorOneWireTemp(master, "28-0000093001f5", "T2-luft", "Aussentemperatur").init(),
				new SensorOneWireTemp(master, "28-00000a25c18f", "T3-innen", "Abwassertank innen").init());
	}

	private static Properties createConsumerProperties() {
		Properties props = new Properties();
		props.put("bootstrap.servers", "localhost:9092");
		props.put("group.id", "test");
		props.put("enable.auto.commit", "true");
		props.put("auto.commit.interval.ms", "1000");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
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
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		return props;
	}
}
