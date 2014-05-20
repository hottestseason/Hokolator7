package com.hottestseason.hokolator;

import static org.junit.Assert.assertEquals;

import javax.vecmath.Point2d;

import org.jgrapht.EdgeFactory;
import org.junit.Test;

import com.hottestseason.hokolator.Map.Intersection;
import com.hottestseason.hokolator.Map.Street;
import com.hottestseason.hokolator.graph.GeometricGraph;

public class PedestrianTest {
	@Test
	public void testUpdate() throws InterruptedException {
		final Map map = new Map();
		map.generateGrid(1, 1.6, 1, 3, new EdgeFactory<Map.Intersection, Map.Street>() {
			@Override
			public Street createEdge(Intersection sourceVertex,	Intersection targetVertex) {
				return map.new Street(map.getStreets().size() + 1, 1);
			}
		}, new GeometricGraph.VertexFactory<Map.Intersection>() {
			@Override
			public Intersection createVertex(Point2d point) {
				return map.new Intersection(map.getIntersections().size() + 1, point);
			}
		});
		Map.Street startStreet = map.getStreet(1, 2);
		Map.Intersection goal = map.getIntersection(3);
		startStreet.width = 2;

		PedestriansSimulator simulator = new PedestriansSimulator(map);
		Pedestrian pedestrian1 = new Pedestrian(simulator, 1, goal, 1);
		Pedestrian pedestrian2 = new Pedestrian(simulator, 2, goal, 2);
		simulator.pedestrians.add(pedestrian1);
		simulator.pedestrians.add(pedestrian2);
		pedestrian1.moveTo(new Place(startStreet, 0));
		pedestrian2.moveTo(new Place(startStreet, 0));

		assertEquals(1, pedestrian1.getPlace().street.getSource().id);
		assertEquals(1, pedestrian2.getPlace().street.getSource().id);
		AgentsScheduler.update(map.getStreets(), 1.0);
		AgentsScheduler.update(simulator.pedestrians, 1.0);
		assertEquals(1, pedestrian1.getPlace().street.getSource().id);
		assertEquals(2, pedestrian2.getPlace().street.getSource().id);
	}
}
