package com.hottestseason.hokolator.simulator;

import com.hottestseason.hokolator.Map;
import com.hottestseason.hokolator.concurrent.NonblockingScheduler;

public class NonblockingPedestriansSimulator extends PedestriansSimulator {
	public NonblockingPedestriansSimulator(Map map) {
		super(map);
	}

	@Override
	public void update() throws InterruptedException {
		NonblockingScheduler.update(pedestrians);
	}
}
