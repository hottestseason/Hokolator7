package com.hottestseason.hokolator.simulator;

import org.junit.Test;

public class BarrierAllPedestriansSimulatorTest extends PedestriansSimulatorTest {
	@Test
	public void testUpdate() throws InterruptedException {
		testUpdate("barrierAll");
	}
}
