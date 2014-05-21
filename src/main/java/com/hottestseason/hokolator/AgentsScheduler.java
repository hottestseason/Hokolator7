package com.hottestseason.hokolator;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AgentsScheduler {
	private static final int awaitTime = 60 * 60;
	private static final AgentsScheduler instance = new AgentsScheduler();

	private final Map<String, BarrierScheduler> barrierSchedulerMap = new HashMap<>();
	private final Map<String, OrderedScheduler> orderedSchedulerMap = new HashMap<>();
	private static final Queue<Runnable> newlyCreatedJobs = new ConcurrentLinkedQueue<>();

	public static void update(Set<? extends Agent> agents, final double time) throws InterruptedException {
		AgentsScheduler.clear();
		for (final Agent agent : agents) {
			newlyCreatedJobs.add(new Runnable() {
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
		while (!newlyCreatedJobs.isEmpty()) {
			executeWithThreadPool(newlyCreatedJobs);;
		}
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

	public static void clear() {
		instance.barrierSchedulerMap.clear();
		instance.orderedSchedulerMap.clear();
	}

	public static void finished(String tag, Agent agent) {
		instance.getOrRegisterWaitersScheduler(tag).finished(agent);
	}

	public static void barrier(String tag, Agent waiter, Set<? extends Agent> agents, Runnable block) {
		instance.getOrRegisterWaitersScheduler(tag).barrier(waiter, agents, block);
	}

	public static void ordered(String tag, Agent agent, Set<? extends Agent> agents, Comparator<Agent> comparator, Runnable runnable) {
		instance.getOrRegisterSequentialScheduler(tag).ordered(agent, agents, comparator, runnable);
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
		private final Map<Agent, Boolean> finishedFlags = new HashMap<>();
		private final Map<Agent, Set<Agent>> waitersMap = new HashMap<>();
		private final Map<Agent, Set<? extends Agent>> waitingsMap = new HashMap<>();
		private final Map<Agent, Runnable> blockMap = new HashMap<>();

		private synchronized void finished(Agent agent) {
			finishedFlags.put(agent, true);
			if (waitersMap.containsKey(agent)) {
				for (Agent waiter : waitersMap.get(agent)) {
					waitingsMap.get(waiter).remove(agent);
					if (waitingsMap.get(waiter).isEmpty()) {
						newlyCreatedJobs.add(blockMap.get(waiter));
					}
				}
			}
		}

		private synchronized void barrier(Agent waiter, Set<? extends Agent> others, Runnable block) {
			Set<? extends Agent> waitings = new HashSet<>(others);
			for (Agent other : others) {
				if (finishedFlags.containsKey(other)) {
					waitings.remove(other);
				} else {
					if (!waitersMap.containsKey(other)) {
						waitersMap.put(other, new HashSet<Agent>());
					}
					waitersMap.get(other).add(waiter);
				}
			}
			if (waitings.isEmpty()) {
				newlyCreatedJobs.add(block);
			} else {
				waitingsMap.put(waiter, waitings);
				blockMap.put(waiter, block);
			}
		}
	}

	class OrderedScheduler {
		private final String tag;
		private final Map<Agent, SortedSet<Agent>> barrierMap = new ConcurrentHashMap<>();
		private final Map<Agent, Runnable> runnableMap = new ConcurrentHashMap<>();

		public OrderedScheduler(String tag) {
			this.tag = tag;
		}

		private void ordered(final Agent agent, Set<? extends Agent> agents, Comparator<Agent> comparator, Runnable runnable) {
			if (agents.size() == 1 && agents.contains(agent)) {
				newlyCreatedJobs.add(runnable);
				return;
			}
			runnableMap.put(agent, runnable);
			final SortedSet<Agent> sorted = new ConcurrentSkipListSet<>(comparator);
			sorted.addAll(agents);
			barrierMap.put(agent, sorted);
			AgentsScheduler.finished(tag, agent);
			recursiveBarrier(agent, new Runnable() {
				@Override
				public void run() {
					if (sorted.first() == agent) {
						for (Agent _agent : sorted) {
							runnableMap.get(_agent).run();
						}
					}
				}
			});
		}

		private void recursiveBarrier(final Agent agent, final Runnable runnable) {
			final SortedSet<Agent> agents = barrierMap.get(agent);
			AgentsScheduler.barrier(tag, agent, agents, new Runnable() {
				@Override
				public void run() {
					int beforeSize, afterSize;
					synchronized (agents) {
						beforeSize = agents.size();
						SortedSet<Agent> _agents = new ConcurrentSkipListSet<>(agents.comparator());
						for (Agent waiting : agents) {
							_agents.addAll(barrierMap.get(waiting));
						}
						agents.addAll(_agents);
						afterSize = agents.size();
					}
					if (beforeSize == afterSize) {
						runnable.run();
					} else {
						recursiveBarrier(agent, runnable);
					}
				}
			});
		}
	}
}
