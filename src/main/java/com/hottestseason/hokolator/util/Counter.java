package com.hottestseason.hokolator.util;

public class Counter {
	private int value = 0;

	public synchronized int get() {
		return value;
	}

	public synchronized void set(int v) {
		value = v;
	}

	public void add(int v) {
		set(value + v);
	}

	public void increment() {
		add(1);
	}

	public void decrement() {
		add(-1);
	}
}
