package org.sensors.backend.device.ledstrip;

import java.util.List;

public class MultiLedChange {
	private List<OneLedChange> list;

	public MultiLedChange(List<OneLedChange> list) {
		this.list = list;
	}

	public List<OneLedChange> getList() {
		return list;
	}
}
