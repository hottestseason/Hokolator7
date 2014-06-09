package com.hottestseason.hokolator;

import java.util.Comparator;
import java.util.Set;

import com.hottestseason.hokolator.Map.Intersection;
import com.hottestseason.hokolator.concurrent.BlockingScheduler;
import com.hottestseason.hokolator.concurrent.Item;
import com.hottestseason.hokolator.simulator.PedestriansSimulator;

public class BlockingPedestrian extends Pedestrian {
	public BlockingPedestrian(PedestriansSimulator simulator, int id, Intersection goal, double speed) {
		super(simulator, id, goal, speed);
	}

	@Override
	public void run() {
		try {
			if (!goaled) {
				iteration.increment();
				speed = calcSpeed();
				nextPlace = calcNextPlace(simulator.timeunit);
				if (place.street != nextPlace.street) {
					linkLeftTime = calcLinkLeftTime();
					Set<Pedestrian> neighborPedestrians = calcNeighborPedestrians();
					finished("nextPlaceCalculated");
					barrier("nextPlaceCalculated", neighborPedestrians);
					Set<Pedestrian> conflictingPedestrians = calcConflictingPedestrians(neighborPedestrians);
					ordered("conflictingPedestrians", conflictingPedestrians, new Comparator<Item>() {
						@Override
						public int compare(Item i1, Item i2) {
							return ((Pedestrian) i1).compareUsingLinkLeftTime((Pedestrian) i2);
						}
					}, new Runnable() {
						@Override
						public void run() {
							moveTo(nextPlace);
						}
					});
				} else {
					finished("nextPlaceCalculated");
					moveTo(nextPlace);
				}
			} else {
				finished("nextPlaceCalculated");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void finished(String tag) {
		BlockingScheduler.finished(tag, this);
	}

	private void barrier(String tag, Set<? extends Item> items) throws InterruptedException {
		BlockingScheduler.barrier(tag, this, items);
	}

	private void ordered(String tag, Set<? extends Item> items, Comparator<Item> comparator, Runnable runnable) throws InterruptedException {
		BlockingScheduler.ordered(tag, this, items, comparator, runnable);
	}
}
