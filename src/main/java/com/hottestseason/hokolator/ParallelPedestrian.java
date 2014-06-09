package com.hottestseason.hokolator;

import com.hottestseason.hokolator.Map.Intersection;
import com.hottestseason.hokolator.simulator.PedestriansSimulator;

public class ParallelPedestrian extends Pedestrian {
	public ParallelPedestrian(PedestriansSimulator simulator, int id, Intersection goal, double speed) {
		super(simulator, id, goal, speed);
	}

	@Override
	public void run() {
		if (!goaled) {
			iteration.increment();
			speed = calcSpeed();
			nextPlace = calcNextPlace(simulator.timeunit);
			moveTo(nextPlace);
		}
	}
}
