package org.sensors.restservice;

import java.util.Arrays;
import java.util.List;

import org.sensors.backend.SensorApp;
import org.sensors.backend.StateStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ValueController {

	private static final Logger logger = LoggerFactory.getLogger(SensorApp.class);
	
	@Autowired
	private StateStore stateStore;
	
	@GetMapping("/value")
	public Value value(@RequestParam(value = "id", defaultValue = "controller") String id) {
		Float value = (Float)stateStore.getValue(id);
		logger.info("Value: "+value);
		return new Value(value);
	}
	
	@GetMapping("/values")
	public List<Value> values(@RequestParam(value = "id", defaultValue = "controller") String id) {
		Float value = (Float)stateStore.getValue(id);
		logger.info("Values: "+value);
		return Arrays.asList(new Value(value));
	}
}
