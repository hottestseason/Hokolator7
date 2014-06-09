package com.hottestseason.hokolator.simulator;

import com.hottestseason.hokolator.Map;
import com.hottestseason.hokolator.Pedestrian;
import com.hottestseason.hokolator.concurrent.Scheduler;

public class BarrierAllPedestriansSimulator extends PedestriansSimulator {
	public BarrierAllPedestriansSimulator(Map map) {
		super(map);
	}

	@Override
	public void update() throws InterruptedException {
		Scheduler.update(pedestrians);
		for (Pedestrian pedestrian : Pedestrian.sort(pedestrians, Pedestrian.linkLeftTimeComparator())) {
			if (!pedestrian.isFinished()) pedestrian.moveToNextPlace();
		}
	}
}
