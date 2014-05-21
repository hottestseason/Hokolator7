package com.hottestseason.hokolator;

import java.util.Comparator;
import java.util.Set;

public abstract class Agent {
	abstract void update(double time) throws InterruptedException;

	void finished(String tag) {
		AgentsScheduler.finished(tag, this);
	}

	void barrier(String tag, Set<? extends Agent> agents, Runnable runnable) {
		AgentsScheduler.barrier(tag, this, agents, runnable);
	}

	void ordered(String tag, Set<? extends Agent> agents, Comparator<Agent> comparator, Runnable runnable) {
		AgentsScheduler.ordered(tag, this, agents, comparator, runnable);
	}
}
