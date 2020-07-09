package org.sensors.restservice.controller.onewire;

import org.sensors.backend.StateStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class T2ValueController {

	@Autowired
	private StateStore stateStore;

	@GetMapping("/t2/value")
	public Float value() {
		return stateStore.getFloatValue("T2-luft");
	}
}
