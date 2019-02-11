package org.sensors.backend.sensor.handler;

import java.util.function.Consumer;

public interface StateUpdater extends ChangeEventListener {

	String getId();

	void updateState();

	int getFrequencyInHz();
	
	void setFrequencyChangeListener(Consumer<Integer> listener);
}
