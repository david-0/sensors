package org.myprojects.testIna219;

import java.io.IOException;

import org.sensors.backend.ina219.SensorIna219;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class App 
{
	private static final Logger logger = LoggerFactory.getLogger(App.class);
	
    public static void main( String[] args ) throws UnsupportedBusNumberException, IOException
    {
        I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
        SensorIna219 sensor = new SensorIna219(bus, 0x45, "test 5");
        sensor.init();
        int config = sensor.readConfig();
        logger.info("config: " + String.format("%X", config));
        Float readBusVoltage = sensor.readBusVoltageInW();
        logger.info("readBusV: {}", readBusVoltage);
        Float readShuntVoltage = sensor.readShuntVoltageInV();
        logger.info("readShuntV: {}", readShuntVoltage);
        Float readCurrent= sensor.readCurrentInI();
        logger.info("readCurrent: {}", readCurrent);
        Float readPower= sensor.readPowerInW();
        logger.info("readPower: {}", readPower);
    }
}
