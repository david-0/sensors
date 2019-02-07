package org.sensors.backend;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.sensors.backend.sensor.SensorMcp9808;
import org.sensors.backend.sensor.SensorOneWireTemp;
import org.sensors.backend.sensor.handler.IntervalSensor;
import org.sensors.backend.sensor.ina219.SensorIna219;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import com.pi4j.io.w1.W1Master;

public class App {
	private static final Logger logger = LoggerFactory.getLogger(App.class);

	private static boolean wlanOn;

	public static void main(String[] args) throws UnsupportedBusNumberException,
			IOException, InterruptedException, ExecutionException {
		I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);

		Controller controller = new Controller(
				new KafkaProducer<>(App.createProducerProperties()),
				new KafkaConsumer<>(createConsumerProperties()));
		createIntervalSensors(bus).stream()
				.forEach(controller::addIntervalSensor);
		controller.init();
		controller.run();
		logger.info("controller started");
		initGpio();
	}

	private static List<IntervalSensor> createIntervalSensors(I2CBus bus) {
		List<IntervalSensor> sensors = new ArrayList<>();
		SensorMcp9808 sensorMcp9808 = new SensorMcp9808(bus, 0x18,
				"Temp Controller");
		sensorMcp9808.init();
		sensors.add(new IntervalSensor(sensorMcp9808::readTemperature,
				"tempController", Duration.ofMillis(5000)));
		sensorMcp9808 = new SensorMcp9808(bus, 0x1C, "Temp Unbekannt");
		sensorMcp9808.init();
		sensors.add(new IntervalSensor(sensorMcp9808::readTemperature,
				"tempUnbekannt", Duration.ofMillis(5000)));
		SensorIna219 ina1 = new SensorIna219(bus, 0x40, "INA219 1 - LED");
		ina1.init();
		sensors.add(new IntervalSensor(ina1::readAll, "ina219-led",
				Duration.ofMillis(500)));
		SensorIna219 ina2 = new SensorIna219(bus, 0x41, "INA219 2 - Raspi");
		ina2.init();
		sensors.add(new IntervalSensor(ina2::readAll, "ina219-raspi",
				Duration.ofMillis(500)));
		SensorIna219 ina3 = new SensorIna219(bus, 0x45, "INA219 3 - Input");
		ina3.init();
		sensors.add(new IntervalSensor(ina3::readAll, "ina219-input",
				Duration.ofMillis(500)));

		W1Master master = new W1Master();
		SensorOneWireTemp temp = new SensorOneWireTemp(master,
				"28-0000046d50e7", "T1", "Abwassertank aussen");
		sensors.add(new IntervalSensor(temp::getTemperature, "DS18B20-T1",
				Duration.ofMillis(5000)));
		temp = new SensorOneWireTemp(master, "28-0000093001f5", "T2",
				"Aussentemperatur");
		sensors.add(new IntervalSensor(temp::getTemperature, "DS18B20-T2",
				Duration.ofMillis(5000)));
		temp = new SensorOneWireTemp(master, "28-00000a25c18f", "T3",
				"Abwassertank innen");
		sensors.add(new IntervalSensor(temp::getTemperature, "DS18B20-T3",
				Duration.ofMillis(5000)));
		return sensors;
	}

	private static void initGpio() {
		final GpioController gpio = GpioFactory.getInstance();

		final GpioPinDigitalInput ledButton = gpio.provisionDigitalInputPin(
				RaspiPin.GPIO_03, PinPullResistance.PULL_DOWN);
		ledButton.setShutdownOptions(true);
		ledButton.addListener(new GpioPinListenerDigital() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent(
					GpioPinDigitalStateChangeEvent event) {
				System.out.println(" --> GPIO PIN (LED Button) STATE CHANGE: "
						+ event.getPin() + " = " + event.getState());
			}

		});

		final GpioPinDigitalOutput wlanLED = gpio
				.provisionDigitalOutputPin(RaspiPin.GPIO_02);
		final GpioPinDigitalInput wlanButton = gpio.provisionDigitalInputPin(
				RaspiPin.GPIO_00, PinPullResistance.PULL_DOWN);
		wlanButton.setShutdownOptions(true);
		wlanButton.addListener(new GpioPinListenerDigital() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent(
					GpioPinDigitalStateChangeEvent event) {
				System.out.println(" --> GPIO PIN (WLAN Button) STATE CHANGE: "
						+ event.getPin() + " = " + event.getState());
				if (event.getState().equals(PinState.LOW)) {
					wlanOn = !wlanOn;
				}
				wlanLED.setState(wlanOn);
			}

		});
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
