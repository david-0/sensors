package org.sensors.restservice.controller.ina219;

import org.sensors.backend.StateStore;
import org.sensors.to.TempHumidityPressure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class p2ValueController {

	@Autowired
	private StateStore stateStore;

	@GetMapping("/power2/value")
	public TempHumidityPressure value() {
		return stateStore.getTempHumidityPressure("ina219-raspi");
	}
}
