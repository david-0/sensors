package org.sensors.backend;

public class App {
	public static void main(String[] args) {
		StateStore stateStore = new StateStore();
		SensorApp app = new SensorApp();
		app.init(stateStore);
		app.start();
		app.stop();
	}
}
