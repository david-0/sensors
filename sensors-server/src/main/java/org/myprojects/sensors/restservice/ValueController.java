package org.myprojects.sensors.restservice;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ValueController {

	@GetMapping("/value")
	public Value value(@RequestParam(value = "id", defaultValue = "bme280") String id) {
		return new Value(1.1f);
	}
	
	@GetMapping("/values")
	public List<Value> values(@RequestParam(value = "id", defaultValue = "bme280") String id) {
		return Arrays.asList(new Value(1.1f));
	}
}
