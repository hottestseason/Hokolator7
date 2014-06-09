package com.hottestseason.hokolator;

import java.util.Comparator;
import java.util.Set;

import com.hottestseason.hokolator.Map.Intersection;
import com.hottestseason.hokolator.concurrent.Item;
import com.hottestseason.hokolator.concurrent.NonblockingScheduler;
import com.hottestseason.hokolator.simulator.PedestriansSimulator;

public class NonblockingPedestrian extends Pedestrian {
	public NonblockingPedestrian(PedestriansSimulator simulator, int id, Intersection goal, double speed) {
		super(simulator, id, goal, speed);
	}

	@Override
	public void run() {
		if (!goaled) {
			iteration.increment();
			speed = calcSpeed();
			nextPlace = calcNextPlace(simulator.timeunit);
			if (place.street != nextPlace.street) {
				linkLeftTime = calcLinkLeftTime();
				final Set<Pedestrian> neighborPedestrians = calcNeighborPedestrians();
				finished("nextPlaceCalculated");
				barrier("nextPlaceCalculated", neighborPedestrians, new Runnable() {
					@Override
					public void run() {
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
					}
				});
			} else {
				finished("nextPlaceCalculated");
				moveTo(nextPlace);
			}
		} else {
			finished("nextPlaceCalculated");
		}
	}


	private void finished(String tag) {
		NonblockingScheduler.finished(tag, this);
	}

	private void barrier(String tag, Set<? extends Item> items, Runnable runnable) {
		NonblockingScheduler.barrier(tag, this, items, runnable);
	}

	private void ordered(String tag, Set<? extends Item> items, Comparator<Item> comparator, Runnable runnable) {
		NonblockingScheduler.ordered(tag, this, items, comparator, runnable);
	}
}
