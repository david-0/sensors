package org.sensors.backend;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
import org.sensors.logic.LedButtonProcessor;
import org.sensors.logic.WlanButtonProcessor;
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

			
			LedStrip ledStripSchrank = new LedStrip(8).init();
			Button ledButton = new Button(gpio, RaspiPin.GPIO_27, true).init();
			Button wlanButton = new Button(gpio, RaspiPin.GPIO_24, false).init();
			DigialOutputDevice wlanButtonLed = new DigialOutputDevice(gpio, RaspiPin.GPIO_25).init();
			WlanControlOutputDevice wlanOutputDevice = new WlanControlOutputDevice();
			
			LedButtonProcessor ledButtonProcessor = new LedButtonProcessor(ledStripSchrank);
			WlanButtonProcessor wlanButtonProcessor = new WlanButtonProcessor(wlanButtonLed, wlanOutputDevice);

			ledButton.onChange(ledButtonProcessor::update);
			wlanButton.onChange(wlanButtonProcessor::update);

			StateStore stateStore = new StateStore();
			SensorController controller = new SensorController((id, value) -> stateStore.update(id, value));
			createMcp9808Sensors(bus).stream().forEach(controller::addIntervalBasedSource);
			createIna219Sensors(bus).stream().forEach(controller::addIntervalBasedSource);
			createOneWireSensors(master).stream().forEach(controller::addIntervalBasedSource);

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
}
