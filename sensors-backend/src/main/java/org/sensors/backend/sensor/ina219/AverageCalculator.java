package org.sensors.backend.sensor.ina219;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class AverageCalculator {

	private List<Data> data = new ArrayList<>();

	public void reset() {
		data.clear();
	}

	public void add(float powerInW) {
		data.add(new Data(Instant.now(), powerInW));
	}

	public double getAveragePerSecAndReset() {
		double current = 0;
		if (data.size() == 0) {
			return current;
		}
		for (int i = 0; i < data.size() - 1; i++) {
			Duration between = Duration.between(data.get(i).getTimestamp(), data.get(i + 1).getTimestamp());
			current += data.get(i).getValue() * between.toNanos() / 1000000000;
		}
		Instant now = Instant.now();
		Duration between = Duration.between(data.get(data.size() - 1).getTimestamp(), now);
		current += data.get(data.size() - 1).getValue() * between.toNanos() / 1_000_000_000;

		Duration total = Duration.between(data.get(0).getTimestamp(), now);
		reset();
		return current / total.toNanos() * 1_000_000_000;
	}

	private static class Data {
		private final Instant timestamp;
		private final float value;

		Data(Instant timestamp, float value) {
			this.timestamp = timestamp;
			this.value = value;
		}

		Instant getTimestamp() {
			return timestamp;
		}

		float getValue() {
			return value;
		}
	}

}
