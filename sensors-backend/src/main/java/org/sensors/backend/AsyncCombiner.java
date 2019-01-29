package org.sensors.backend;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Stream;

public final class  AsyncCombiner {

	private AsyncCombiner() {
	}
	
	public static Future<Void> allOf(Runnable... runnables) {
		return CompletableFuture.allOf(Stream.of(runnables)//
				.map(CompletableFuture::runAsync) //
				.toArray(s -> new CompletableFuture[s]));
	}
}
