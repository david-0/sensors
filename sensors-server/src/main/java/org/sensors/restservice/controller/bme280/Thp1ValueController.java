package org.sensors.restservice.controller.bme280;

import org.sensors.backend.StateStore;
import org.sensors.to.TempHumidityPressure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Thp1ValueController {

	@Autowired
	private StateStore stateStore;

	@GetMapping("/thp1/value")
	public TempHumidityPressure value() {
		return stateStore.getTempHumidityPressure("bme280");
	}
}
