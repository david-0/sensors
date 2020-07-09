package org.sensors.restservice.controller.sht31d;

import org.sensors.backend.StateStore;
import org.sensors.to.TempHumidity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Th1ValueController {

	@Autowired
	private StateStore stateStore;
	
	@GetMapping("/th1/value")
	public TempHumidity value() {
		return stateStore.getTempHumidity("sht31d");
	}
}
