package org.sensors.backend;

public interface ChangeEventListener {
	boolean onSettingChange(String key, String value);
}
