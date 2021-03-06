package org.sensors.backend;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.sensors.backend.device.Button;
import org.sensors.backend.device.DigialOutputDevice;
import org.sensors.backend.device.LedStrip;
import org.sensors.backend.device.SensorBME280;
import org.sensors.backend.device.SensorMcp9808;
import org.sensors.backend.device.SensorMe2O2;
import org.sensors.backend.device.SensorOneWireTemp;
import org.sensors.backend.device.SensorSHT31d;
import org.sensors.backend.device.WlanControlOutputDevice;
import org.sensors.backend.device.ina219.SensorIna219;
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
			controller.addEventBasedSource(new Button(gpio, RaspiPin.GPIO_27, true, "led-button").init());
			controller.addEventBasedSource(new Button(gpio, RaspiPin.GPIO_24, false, "wlan-button").init());
			controller.addSettingChangeEventListener(
					new DigialOutputDevice(gpio, RaspiPin.GPIO_25, "wlan-button-led").init());
			controller.addSettingChangeEventListener(new LedStrip("led-strip", 8).init());
			Stream.of(new WlanControlOutputDevice("wlan").init()) //
					.peek(controller::addSettingChangeEventListener) //
					.forEach(controller::addEventBasedSource);

			controller.addIntervalBasedSource(new SensorMe2O2(bus, 0x04, "o2", "O2 Sensor", 84.0).init());
			controller.addIntervalBasedSource(new SensorSHT31d(bus, 0x44, "sht31d", "temp / humidity Sensor").init());
			controller.addIntervalBasedSource(
					new SensorBME280(bus, 0x77, "bme280", "temp / humidity / pressure Sensor").init());
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
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "test");
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
		props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.StringDeserializer");
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.StringDeserializer");
		return props;
	}

	private static Properties createProducerProperties() {
		Properties props = new Properties();
		props.put("bootstrap.servers", "localhost:9092");
		props.put("acks", "all");
		props.put("buffer.memory", 33554432);
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		return props;
	}
}
