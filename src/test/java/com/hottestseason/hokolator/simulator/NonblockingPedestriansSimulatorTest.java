package com.hottestseason.hokolator.simulator;

import org.junit.Test;

public class NonblockingPedestriansSimulatorTest extends PedestriansSimulatorTest {
	@Test
	public void testUpdate() throws InterruptedException {
		testUpdate("nonblocking");
	}
}
