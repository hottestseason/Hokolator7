package com.hottestseason.hokolator.concurrent;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import com.hottestseason.hokolator.util.ThreadPool;

public class NonblockingScheduler extends Scheduler {
	private static final NonblockingScheduler instance = new NonblockingScheduler();
	private static ThreadPool threadPool;

	private final Map<String, BarrierScheduler> barrierSchedulerMap = new HashMap<>();
	private final Map<String, OrderedScheduler> orderedSchedulerMap = new HashMap<>();

	public static void update(Set<? extends Item> items) throws InterruptedException {
		clear();
		threadPool = new ThreadPool();
		for (Item item : items) {
			threadPool.add(item);
		}
		threadPool.start().await().shutdown();
	}

	public static void clear() {
		instance.barrierSchedulerMap.clear();
		instance.orderedSchedulerMap.clear();
	}

	public static void finished(String tag, Item item) {
		instance.getOrRegisterWaitersScheduler(tag).finished(item);
	}

	public static void barrier(String tag, Item waiter, Set<? extends Item> items, Runnable block) {
		instance.getOrRegisterWaitersScheduler(tag).barrier(waiter, items, block);
	}

	public static void ordered(String tag, Item item, Set<? extends Item> items, Comparator<Item> comparator, Runnable runnable) {
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
		private final Map<Item, Boolean> finishedFlags = new HashMap<>();
		private final Map<Item, Set<Item>> waitersMap = new HashMap<>();
		private final Map<Item, Set<? extends Item>> waitingsMap = new HashMap<>();
		private final Map<Item, Runnable> blockMap = new HashMap<>();

		private synchronized void finished(Item item) {
			finishedFlags.put(item, true);
			if (waitersMap.containsKey(item)) {
				for (Item waiter : waitersMap.get(item)) {
					waitingsMap.get(waiter).remove(item);
					if (waitingsMap.get(waiter).isEmpty()) {
						threadPool.add(blockMap.get(waiter));
					}
				}
			}
		}

		private synchronized void barrier(Item waiter, Set<? extends Item> others, Runnable block) {
			Set<? extends Item> waitings = new HashSet<>(others);
			for (Item other : others) {
				if (finishedFlags.containsKey(other)) {
					waitings.remove(other);
				} else {
					if (!waitersMap.containsKey(other)) {
						waitersMap.put(other, new HashSet<Item>());
					}
					waitersMap.get(other).add(waiter);
				}
			}
			if (waitings.isEmpty()) {
				threadPool.add(block);
			} else {
				waitingsMap.put(waiter, waitings);
				blockMap.put(waiter, block);
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

		private void ordered(final Item item, Set<? extends Item> items, Comparator<Item> comparator, Runnable runnable) {
			assert !items.isEmpty() && items.contains(item);
			if (items.size() == 1 && items.contains(item)) {
				threadPool.add(runnable);
				return;
			}
			runnableMap.put(item, runnable);
			final SortedSet<Item> sorted = new ConcurrentSkipListSet<>(comparator);
			sorted.addAll(items);
			barrierMap.put(item, sorted);
			finished(tag, item);
			recursiveBarrier(item, new Runnable() {
				@Override
				public void run() {
					if (sorted.first() == item) {
						for (Item _item : sorted) {
							runnableMap.get(_item).run();
						}
					}
				}
			});
		}

		private void recursiveBarrier(final Item item, final Runnable runnable) {
			final SortedSet<Item> items = barrierMap.get(item);
			barrier(tag, item, items, new Runnable() {
				@Override
				public void run() {
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
					if (beforeSize == afterSize) {
						runnable.run();
					} else {
						recursiveBarrier(item, runnable);
					}
				}
			});
		}
	}
}
