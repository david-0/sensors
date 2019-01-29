package org.sensors.backend;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class  AsyncCombiner {

	private AsyncCombiner() {
	}
	
	public static Future<Void> allOf(Runnable... runnables) {
		List<CompletableFuture<Void>> futures = Stream.of(runnables)//
				.map(CompletableFuture::runAsync) //
				.collect(Collectors.toList());
		return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
	}
}
