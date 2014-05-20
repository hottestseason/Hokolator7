package com.hottestseason.hokolator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AgentsScheduler {
	private static final int awaitTime = 60 * 60;

	public static void update(Set<? extends Agent> agents, final double time) throws InterruptedException {
		List<Runnable> runnables = new ArrayList<>();
		for (final Agent agent : agents) {
			runnables.add(new Runnable() {
				@Override
				public void run() {
					try {
						agent.update(time);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			});
		}
		executeWithThreadPool(runnables);
	}

	public static void executeWithThreadPool(Collection<Runnable> runnables) throws InterruptedException {
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		for (Runnable runnable : runnables) {
			executor.execute(runnable);
		}
		executor.shutdown();
		if (!executor.awaitTermination(awaitTime, TimeUnit.SECONDS)) throw new RuntimeException("Cannot finished in " + awaitTime);
		System.gc();
	}
}
