package org.sensors.backend;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StateStore {
	
	private static final Logger logger = LoggerFactory.getLogger(StateStore.class);
	
	private Map<String, Object> stateStore = new HashMap<>();
	
	public void update(String id, Object value) {
		stateStore.put(id, value);
		logger.info(id + " " + value.toString());
	}
	
	public String getLastState() {
		return stateStore.toString();
	}
 }
