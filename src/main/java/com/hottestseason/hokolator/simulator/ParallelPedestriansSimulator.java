package com.hottestseason.hokolator.simulator;

import com.hottestseason.hokolator.Map;
import com.hottestseason.hokolator.concurrent.Scheduler;

public class ParallelPedestriansSimulator extends PedestriansSimulator {
	public ParallelPedestriansSimulator(Map map) {
		super(map);
	}

	@Override
	public void update() throws InterruptedException {
		Scheduler.update(pedestrians);
	}
}
