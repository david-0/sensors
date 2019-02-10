package org.sensors.api.json.mixin;

import java.util.List;

import org.sensors.api.OneLedChange;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MultiLedChangeMixin {
	public MultiLedChangeMixin(@JsonProperty("list") List<OneLedChange> list) {
	}
}
