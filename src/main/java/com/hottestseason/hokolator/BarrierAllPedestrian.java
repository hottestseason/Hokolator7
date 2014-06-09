package com.hottestseason.hokolator;

import com.hottestseason.hokolator.Map.Intersection;
import com.hottestseason.hokolator.simulator.PedestriansSimulator;

public class BarrierAllPedestrian extends Pedestrian {
	public BarrierAllPedestrian(PedestriansSimulator simulator, int id, Intersection goal, double speed) {
		super(simulator, id, goal, speed);
	}

	@Override
	public void run() {
		if (!goaled) {
			iteration.increment();
			speed = calcSpeed();
			nextPlace = calcNextPlace(simulator.timeunit);
			linkLeftTime = calcLinkLeftTime();
		}
	}
}
