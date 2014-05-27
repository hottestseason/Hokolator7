package com.hottestseason.hokolator;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AgentsScheduler {
	private static final int awaitTime = 60 * 60;

	public static void update(Set<? extends Agent> agents, final double time) throws InterruptedException {
		Queue<Runnable> runnables = new LinkedList<>();
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

	public static void executeWithThreadPool(Queue<Runnable> tasks) throws InterruptedException {
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		while (!tasks.isEmpty()) {
			executor.execute(tasks.poll());
		}
		executor.shutdown();
		if (!executor.awaitTermination(awaitTime, TimeUnit.SECONDS)) throw new RuntimeException("Cannot finished in " + awaitTime);
		System.gc();
	}
}
