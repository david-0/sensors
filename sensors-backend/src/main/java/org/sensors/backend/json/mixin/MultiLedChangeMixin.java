package org.sensors.backend.json.mixin;

import java.util.List;

import org.sensors.backend.device.ledstrip.OneLedChange;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MultiLedChangeMixin {
	public MultiLedChangeMixin(@JsonProperty("list") List<OneLedChange> list) {
	}
}
