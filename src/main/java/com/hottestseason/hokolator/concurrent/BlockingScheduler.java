package com.hottestseason.hokolator.concurrent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;

public class BlockingScheduler extends Scheduler {
	private static final BlockingScheduler instance = new BlockingScheduler();

	private final Map<String, BarrierScheduler> barrierSchedulerMap = new HashMap<>();
	private final Map<String, OrderedScheduler> orderedSchedulerMap = new HashMap<>();

	public static void update(Set<? extends Item> items) throws InterruptedException {
		clear();
		List<Thread> threads = new ArrayList<>();
		for (Item item : items) {
			threads.add(new Thread(item));
		}
		for (Thread thread : threads) thread.start();
		for (Thread thread : threads) thread.join();
	}

	public static void clear() {
		instance.barrierSchedulerMap.clear();
		instance.orderedSchedulerMap.clear();
	}

	public static void finished(String tag, Item item) {
		instance.getOrRegisterWaitersScheduler(tag).finished(item);
	}

	public static void barrier(String tag, Item waiter, Set<? extends Item> items) throws InterruptedException {
		instance.getOrRegisterWaitersScheduler(tag).barrier(waiter, items);
	}

	public static void ordered(String tag, Item item, Set<? extends Item> items, Comparator<Item> comparator, Runnable runnable) throws InterruptedException {
		instance.getOrRegisterSequentialScheduler(tag).ordered(item, items, comparator, runnable);
	}

	private BarrierScheduler getOrRegisterWaitersScheduler(String tag) {
		synchronized (barrierSchedulerMap) {
			if (!barrierSchedulerMap.containsKey(tag)) {
				barrierSchedulerMap.put(tag, new BarrierScheduler());
			}
		}
		return barrierSchedulerMap.get(tag);
	}

	private OrderedScheduler getOrRegisterSequentialScheduler(String tag) {
		synchronized (orderedSchedulerMap) {
			if (!orderedSchedulerMap.containsKey(tag)) {
				orderedSchedulerMap.put(tag, new OrderedScheduler(tag));
			}
		}
		return orderedSchedulerMap.get(tag);
	}

	class BarrierScheduler {
		private final Map<Item, CountDownLatch> countDownLatches = new ConcurrentHashMap<>();
		private final Map<Item, Boolean> finishedFlags = new HashMap<>();
		private final Map<Item, Set<Item>> waitersMap = new HashMap<>();

		private synchronized void finished(Item item) {
			finishedFlags.put(item, true);
			if (waitersMap.containsKey(item)) {
				for (Item waiter : waitersMap.get(item)) {
					countDownLatches.get(waiter).countDown();
				}
			}
		}

		private void barrier(Item waiter, Set<? extends Item> items) {
			if (items.size() == 1 && items.contains(waiter)) return;
			int latchSize = items.size();
			synchronized (this) {
				for (Item other : items) {
					if (finishedFlags.containsKey(other) && finishedFlags.get(other)) {
						latchSize--;
					} else {
						if (!waitersMap.containsKey(other)) {
							waitersMap.put(other, new HashSet<Item>());
						}
						waitersMap.get(other).add(waiter);
					}
				}
				if (latchSize == 0) return;
				countDownLatches.put(waiter, new CountDownLatch(latchSize));
			}
			try {
				countDownLatches.get(waiter).await();
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	class OrderedScheduler {
		private final String tag;
		private final Map<Item, SortedSet<Item>> barrierMap = new ConcurrentHashMap<>();
		private final Map<Item, Runnable> runnableMap = new ConcurrentHashMap<>();

		public OrderedScheduler(String tag) {
			this.tag = tag;
		}

		private void ordered(Item item, Set<? extends Item> items, Comparator<Item> comparator, Runnable runnable) throws InterruptedException {
			if (items.size() == 1 && items.contains(item)) {
				runnable.run();
				return;
			}

			runnableMap.put(item, runnable);
			SortedSet<Item> sorted = new ConcurrentSkipListSet<>(comparator);
			sorted.addAll(items);
			barrierMap.put(item, sorted);

			finished(tag, item);
			recursiveBarrier(item);

			if (sorted.first() == item) {
				for (Item _item : sorted) {
					runnableMap.get(_item).run();
				}
			}
		}

		private void recursiveBarrier(Item item) throws InterruptedException {
			SortedSet<Item> items = barrierMap.get(item);
			barrier(tag, item, items);
			int beforeSize, afterSize;
			synchronized (items) {
				beforeSize = items.size();
				SortedSet<Item> _items = new ConcurrentSkipListSet<>(items.comparator());
				for (Item waiting : items) {
					_items.addAll(barrierMap.get(waiting));
				}
				items.addAll(_items);
				afterSize = items.size();
			}
			if (beforeSize != afterSize) recursiveBarrier(item);
		}
	}
}
