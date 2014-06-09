package com.hottestseason.hokolator.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ThreadPool {
	private final BlockingQueue<Runnable> workerQueue;
	private final Counter workingCounter;
	private final CountDownLatch countDownLatch;
	private final List<Worker> workers;

	public ThreadPool() {
		this(Runtime.getRuntime().availableProcessors());
	}

	public ThreadPool(int numThreads) {
		workerQueue = new LinkedBlockingQueue<>();
		workingCounter = new Counter();
		countDownLatch = new CountDownLatch(1);
		workers = new ArrayList<>();
		for (int i = 0; i < numThreads; i++) {
			workers.add(new Worker());
		}
	}

	public ThreadPool(Collection<? extends Runnable> runnables) {
		this();
		for (Runnable runnable : runnables) add(runnable);
	}

	public void add(Runnable runnable) {
		workerQueue.add(runnable);
	}

	public ThreadPool start() {
		for (Worker worker : workers) {
			worker.start();
		}
		return this;
	}

	public ThreadPool shutdown() {
		for (Worker worker : workers) {
			worker.shutdown();
		}
		return this;
	}

	public ThreadPool await() throws InterruptedException {
		countDownLatch.await();
		return this;
	}

	private class Worker extends Thread {
		private volatile boolean done = false;

		@Override
		public void run() {
			while (!done) {
				try {
					Runnable runnable = workerQueue.poll(10, TimeUnit.MILLISECONDS);
					if (runnable != null) {
						workingCounter.increment();
						runnable.run();
						workingCounter.decrement();
						if (workingCounter.get() == 0 && workerQueue.isEmpty()) countDownLatch.countDown();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		public void shutdown() {
			done = true;
		}
	}
}