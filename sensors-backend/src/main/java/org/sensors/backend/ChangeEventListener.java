package org.sensors.backend;

public interface ChangeEventListener {
	boolean changeEventProcessed(String key, String value);
}
