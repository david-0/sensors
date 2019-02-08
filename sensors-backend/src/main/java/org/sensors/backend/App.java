package org.sensors.backend;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

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

	public static void main(String[] args)
			throws UnsupportedBusNumberException, IOException, InterruptedException, ExecutionException {
		I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
		W1Master master = new W1Master();
		final GpioController gpio = GpioFactory.getInstance();

		Controller controller = new Controller(new KafkaProducer<>(App.createProducerProperties()),
				new KafkaConsumer<>(createConsumerProperties()));
		createMcp9808Sensors(bus).stream().forEach(controller::addIntervalBasedSource);
		createIna219Sensors(bus).stream()//
				.peek(controller::addStateUpdaterSource) //
				.forEach(controller::addIntervalBasedSource);
		createOneWireSensors(master).stream().forEach(controller::addIntervalBasedSource);
		controller.addEventBasedSource(new Button(gpio, RaspiPin.GPIO_03, PinPullResistance.PULL_DOWN, "led-button"));
		controller.addEventBasedSource(new Button(gpio, RaspiPin.GPIO_00, PinPullResistance.PULL_DOWN, "wlan-button"));
		controller.addSettingChangeEventListener(new DigialOutputDevice(gpio, RaspiPin.GPIO_02, "wlan-button-led"));
		WlanControlOutputDevice wlanControlOutputDevice = new WlanControlOutputDevice("wlan");
		controller.addSettingChangeEventListener(wlanControlOutputDevice);
		controller.addEventBasedSource(wlanControlOutputDevice);
		controller.init();
		controller.run();
		logger.info("controller started");
		initGpio();
	}

	private static List<SensorMcp9808> createMcp9808Sensors(I2CBus bus) {
		return Arrays.asList(//
				new SensorMcp9808(bus, 0x18, "controller", "Temp Controller").init(),
				new SensorMcp9808(bus, 0x1C, "unknown", "Temp Unbekannt").init());
	}

	private static List<SensorIna219> createIna219Sensors(I2CBus bus) {
		return Arrays.asList(//
				new SensorIna219(bus, 0x40, "ina219-led", "INA219 1 - LED"),
				new SensorIna219(bus, 0x41, "ina219-raspi", "INA219 2 - Raspi"),
				new SensorIna219(bus, 0x45, "ina219-input", "INA219 3 - Input"));
	}

	private static List<SensorOneWireTemp> createOneWireSensors(W1Master master) {
		return Arrays.asList(//
				new SensorOneWireTemp(master, "28-0000046d50e7", "T1-aussen", "Abwassertank aussen"),
				new SensorOneWireTemp(master, "28-0000093001f5", "T2-luft", "Aussentemperatur"),
				new SensorOneWireTemp(master, "28-00000a25c18f", "T3-innen", "Abwassertank innen"));
	}

	private static void initGpio() {
		final GpioController gpio = GpioFactory.getInstance();

		final GpioPinDigitalInput ledButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_03,
				PinPullResistance.PULL_DOWN);
		ledButton.setShutdownOptions(true);
		ledButton.addListener(new GpioPinListenerDigital() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
				System.out.println(
						" --> GPIO PIN (LED Button) STATE CHANGE: " + event.getPin() + " = " + event.getState());
			}

		});

		final GpioPinDigitalOutput wlanLED = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02);
		final GpioPinDigitalInput wlanButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_00,
				PinPullResistance.PULL_DOWN);
		wlanButton.setShutdownOptions(true);
		wlanButton.addListener(new GpioPinListenerDigital() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
				System.out.println(
						" --> GPIO PIN (WLAN Button) STATE CHANGE: " + event.getPin() + " = " + event.getState());
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
