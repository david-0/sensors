package org.sensors.logic;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Logic {

	private static final Logger logger = LoggerFactory.getLogger(Logic.class);

	final static CountDownLatch latch = new CountDownLatch(1);

	public static void main(final String[] args) {
		final ButtonProcessor processor = new ButtonProcessor();
		Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
			@Override
			public void run() {
				processor.stop();
				latch.countDown();
			}
		});
		try {
			processor.run();
			latch.await();
		} catch (Throwable e) {
			logger.error("Application aborted", e);
		}
	}
}
