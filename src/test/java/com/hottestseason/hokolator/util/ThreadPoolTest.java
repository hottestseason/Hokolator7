package com.hottestseason.hokolator.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ThreadPoolTest {

	@Test
	public void test() throws InterruptedException {
		final ThreadPool threadPool = new ThreadPool();
		final List<Integer> results = new ArrayList<>();
		threadPool.add(new Runnable() {
			@Override
			public void run() {
				results.add(1);
				threadPool.add(new Runnable() {
					@Override
					public void run() {
						results.add(2);
						threadPool.add(new Runnable() {
							@Override
							public void run() {
								results.add(3);
							}
						});
					}
				});
			}
		});
		threadPool.add(new Runnable() {
			@Override
			public void run() {
				results.add(4);
				threadPool.add(new Runnable() {
					@Override
					public void run() {
						results.add(5);
						threadPool.add(new Runnable() {
							@Override
							public void run() {
								results.add(6);
							}
						});
					}
				});
			}
		});
		threadPool.start().await().shutdown();
		assertThat(results.indexOf(1), is(lessThan(results.indexOf(2))));
		assertThat(results.indexOf(2), is(lessThan(results.indexOf(3))));
		assertThat(results.indexOf(4), is(lessThan(results.indexOf(5))));
		assertThat(results.indexOf(5), is(lessThan(results.indexOf(6))));
	}
}