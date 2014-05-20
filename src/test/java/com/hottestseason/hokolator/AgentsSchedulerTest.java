package com.hottestseason.hokolator;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class AgentsSchedulerTest {
	@Test
	public void testOrdered() throws InterruptedException {
		final List<String> result = new ArrayList<>();
		final List<Agent> ordered = new ArrayList<>();
		final Comparator<Agent> comparator = new Comparator<Agent>() {
			@Override
			public int compare(Agent a1, Agent a2) {
				return ordered.indexOf(a1) - ordered.indexOf(a2);
			}
		};
		final Set<Agent> agents = new HashSet<>();
		final Set<Agent> abc = new HashSet<>();
		final Set<Agent> bcd = new HashSet<>();
		final Set<Agent> cde = new HashSet<>();
		final Set<Agent> ad = new HashSet<>();
		Agent a = new Agent() { @Override public void update(double time) throws InterruptedException { AgentsScheduler.ordered("ordered", this, abc, comparator, new Runnable() {
			@Override
			public void run() {
				result.add("a");
			}
		}); } };
		Agent b = new Agent() { @Override public void update(double time) throws InterruptedException { AgentsScheduler.ordered("ordered", this, bcd, comparator, new Runnable() {
			@Override
			public void run() {
				result.add("b");
			}
		}); } };
		Agent c = new Agent() { @Override public void update(double time) throws InterruptedException { AgentsScheduler.ordered("ordered", this, cde, comparator, new Runnable() {
			@Override
			public void run() {
				result.add("c");
			}
		}); } };
		Agent d = new Agent() { @Override public void update(double time) throws InterruptedException { AgentsScheduler.ordered("ordered", this, ad, comparator, new Runnable() {
			@Override
			public void run() {
				result.add("d");
			}
		}); } };
		Agent e = new Agent() { @Override public void update(double time) throws InterruptedException { AgentsScheduler.ordered("ordered", this, cde, comparator, new Runnable() {
			@Override
			public void run() {
				result.add("e");
			}
		}); } };
		ordered.add(b); ordered.add(c); ordered.add(e); ordered.add(d); ordered.add(a);
		abc.add(a); abc.add(b); abc.add(c);
		bcd.add(b); bcd.add(c); bcd.add(d);
		cde.add(c); cde.add(d); cde.add(e);
		ad.add(a); ad.add(d);
		agents.add(a); agents.add(b); agents.add(c); agents.add(d); agents.add(e);
		AgentsScheduler.update(agents, 0);
		assertEquals(Arrays.asList("b", "c", "e", "d", "a"), result);
	}
}
