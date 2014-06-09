package com.hottestseason.hokolator.concurrent;

import java.util.Set;

import com.hottestseason.hokolator.util.ThreadPool;

public class Scheduler {
	public static void update(Set<? extends Item> items) throws InterruptedException {
		new ThreadPool(items).start().await().shutdown();
	}
}
