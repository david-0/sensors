package org.sensors.backend.sensor.handler;

public interface ChangeEventListener {
	boolean onSettingChange(String key, String value);
}
