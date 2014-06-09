package com.hottestseason.hokolator.simulator;

import com.hottestseason.hokolator.Map;
import com.hottestseason.hokolator.concurrent.BlockingScheduler;

public class BlockingPedestriansSimulator extends PedestriansSimulator {
	public BlockingPedestriansSimulator(Map map) {
		super(map);
	}

	@Override
	public void update() throws InterruptedException {
		BlockingScheduler.update(pedestrians);
	}
}
