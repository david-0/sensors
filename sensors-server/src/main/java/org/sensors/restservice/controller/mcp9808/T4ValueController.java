package org.sensors.restservice.controller.mcp9808;

import org.sensors.backend.StateStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class T4ValueController {

	@Autowired
	private StateStore stateStore;
	
	@GetMapping("/t4/value")
	public Float value() {
		return stateStore.getFloatValue("controller");
	}
}
