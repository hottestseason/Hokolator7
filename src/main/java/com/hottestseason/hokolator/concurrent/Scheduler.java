package com.hottestseason.hokolator.concurrent;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Scheduler {
	private static final int awaitTime = 60 * 60;

	public static void update(Set<? extends Item> items) throws InterruptedException {
		executeWithThreadPool(items);
	}

	public static void executeWithThreadPool(Collection<? extends Runnable> runnables) throws InterruptedException {
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		for (Runnable runnable : runnables) executor.execute(runnable);
		executor.shutdown();
		if (!executor.awaitTermination(awaitTime, TimeUnit.SECONDS)) throw new RuntimeException("Cannot finished in " + awaitTime);
	}
}
