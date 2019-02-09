package org.sensors.backend.device.ledstrip;

import java.util.List;

public class MultiLedChange {
	private List<OneLedChange> multiple;

	public MultiLedChange(List<OneLedChange> multiple) {
		this.multiple = multiple;
	}

	public List<OneLedChange> getMultiple() {
		return multiple;
	}
}
