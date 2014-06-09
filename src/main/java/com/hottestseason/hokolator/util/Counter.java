package com.hottestseason.hokolator.util;

public class Counter {
	private int value = 0;

	public synchronized int get() {
		return value;
	}

	public synchronized void set(int v) {
		value = v;
	}

	public synchronized void add(int v) {
		set(value + v);
	}

	public synchronized void increment() {
		add(1);
	}

	public synchronized void decrement() {
		add(-1);
	}
}
