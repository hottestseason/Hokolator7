package com.hottestseason.hokolator.simulator;

import org.junit.Test;

public class BlockingPedestriansSimulatorTest extends PedestriansSimulatorTest {
	@Test
	public void testUpdate() throws InterruptedException {
		testUpdate("blocking");
	}
}
