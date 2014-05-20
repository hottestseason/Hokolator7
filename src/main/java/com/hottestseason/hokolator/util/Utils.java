package com.hottestseason.hokolator.util;

import java.util.List;
import java.util.Random;

public class Utils {
	public static Random random = new Random(0);

	public static <E> E getRandomlyFrom(List<E> list) {
		return list.get(random.nextInt(list.size()));
	}
}