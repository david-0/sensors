package org.sensors.backend.device.ina219;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.sensors.backend.sensor.handler.IntervalBasedSource;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;

public class SensorIna219 implements IntervalBasedSource {

	private static final float SHUNT_VOLTAGE_LSB = 10e-6f;
	private static final float BUS_VOLTAGE_LSB_IN_MV = 4;
	private static final int POWER_LSB_SCALE = 20;

	private boolean initialized;
	private float currentLSB;

	private I2CBus bus;
	private I2CDevice device;
	private int address;
	private String id;
	private String description;
	private Duration interval;
	private int frequencyInHz;
	private AverageCalculator avgCalc = new AverageCalculator();

	public SensorIna219(I2CBus bus, int address, String id, String description) {
		this(bus, address, id, description, Duration.ofMillis(5000));
	}

	public SensorIna219(I2CBus bus, int address, String id, String description, Duration defaultInterval) {
		this(bus, address, id, description, defaultInterval, 5);
	}

	public SensorIna219(I2CBus bus, int address, String id, String description, Duration interval, int frequencyInHz) {
		this.bus = bus;
		this.address = address;
		this.id = id;
		this.description = description;
		this.interval = interval;
		this.frequencyInHz = frequencyInHz;
	}

	public String getDescription() {
		return description;
	}

	public SensorIna219 init() {
		if (initialized) {
			throw new IllegalStateException("Sensor '" + description + "' already initialized");
		}
		try {
			device = bus.getDevice(address);
			double shuntResistance = 0.1;
			float maxExpectedCurrent = 3.2f;

			// see doc: http://www.ti.com/lit/ds/symlink/ina219.pdf
			currentLSB = (maxExpectedCurrent / 32768);
			configure(Brng.V32, Pga.GAIN_1, Adc.BITS_12, Adc.BITS_12);
			int cal = (int) (((0.04096 * 32768) / (maxExpectedCurrent * shuntResistance)));
			writeRegister(RegisterAddress.CALIBRATION, cal);

		} catch (IOException e) {
			throw new RuntimeException("init failed", e);
		}
		initialized = true;
		return this;
	}

	/**
	 * Configures the INA219 with the specified parameters.
	 * 
	 * @param busVoltageRange Bus voltage range to write to the configuration
	 *                        register.
	 * @param pga             Gain range to write to the configuration register.
	 * @param badc            Bus voltage ADC setting to write to the configuration
	 *                        register.
	 * @param sadc            Shunt voltage ADC setting to write to the
	 *                        configuration register.
	 * @throws IOException If the configuration register could not be written.
	 */
	private void configure(final Brng busVoltageRange, final Pga pga, final Adc badc, final Adc sadc)
			throws IOException {
		int regValue = (busVoltageRange.getValue() << 13) | (pga.getValue() << 11) | (badc.getValue() << 7)
				| (sadc.getValue() << 3) | 0x7;
		writeRegister(RegisterAddress.CONFIGURATION, regValue);
	}

	public Float readShuntVoltageInV() {
		short val = readSignedRegister(RegisterAddress.SHUNT_VOLTAGE);
		return Float.valueOf(val * SHUNT_VOLTAGE_LSB);
	}

	public Float readBusVoltageInW() {
		short val = readSignedRegister(RegisterAddress.BUS_VOLTAGE);
		int shiftedRegister = val >> 3;
		Float busVoltageInV = Float.valueOf(shiftedRegister * BUS_VOLTAGE_LSB_IN_MV / 1000);
		return busVoltageInV;
	}

	public Float readPowerInW() {
		return Float.valueOf(getPowerInW());
	}

	private float getPowerInW() {
		int rval = readRegister(RegisterAddress.POWER);
		return rval * POWER_LSB_SCALE * currentLSB;
	}

	public Float readCurrentInI() {
		int rval = readSignedRegister(RegisterAddress.CURRENT);
		return Float.valueOf(rval * currentLSB);
	}

	public int readConfig() {
		int val = readRegister(RegisterAddress.CONFIGURATION);
		return val;
	}

	public Values readAll() {
		avgCalc.add(readPowerInW());
		Float avg = Float.valueOf((float) avgCalc.getAveragePerSecAndReset());
		return new Values(readBusVoltageInW(), readPowerInW(), readCurrentInI(), avg);
	}

	public void writeRegister(final RegisterAddress ra, final int value) throws IOException {
		device.write(ra.getValue(), new byte[] { (byte) ((value >> 8) & 0xFF), (byte) (value & 0xFF) });
	}

	public int readRegister(final RegisterAddress ra) {
		byte[] buf = new byte[2];
		try {
			device.read(ra.getValue(), buf, 0, buf.length);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return ((buf[0] & 0xFF) << 8) | (buf[1] & 0xFF);
	}

	public short readSignedRegister(final RegisterAddress ra) {
		byte[] buf = new byte[2];
		try {
			device.read(ra.getValue(), buf, 0, buf.length);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return (short) ((buf[0] << 8) | (buf[1] & 0xFF));
	}

	@Override
	public Supplier<Values> getDataProvider() {
		return this::readAll;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Duration getInterval() {
		return interval;
	}

	public void setInterval(Duration interval) {
		this.interval = interval;
	}

	public void updateState() {
		avgCalc.add(getPowerInW());
	}

	public int getFrequencyInHz() {
		return frequencyInHz;
	}

	public void setFrequencyInHz(int frequencyInHz) {
		this.frequencyInHz = frequencyInHz;
	}
}
