package org.sensors.backend;

import java.util.HashMap;
import java.util.Map;

import org.sensors.to.PowerValues;
import org.sensors.to.TempHumidity;
import org.sensors.to.TempHumidityPressure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StateStore {
	
	private static final Logger logger = LoggerFactory.getLogger(StateStore.class);
	
	private Map<String, Object> stateStore = new HashMap<>();
	
	public void update(String id, Object value) {
		stateStore.put(id, value);
		logger.info(id + " " + value.toString());
	}
	
	public Float getFloatValue(String id) {
		return (Float)stateStore.get(id);
	}
	
	public TempHumidity getTempHumidity(String id) {
		return (TempHumidity)stateStore.get(id);
	}
	
	public TempHumidityPressure getTempHumidityPressure(String id) {
		return (TempHumidityPressure)stateStore.get(id);
	}
	
	public PowerValues getPowerValues(String id) {
		return (PowerValues)stateStore.get(id);
	}
 }
