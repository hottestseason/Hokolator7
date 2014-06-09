package com.hottestseason.hokolator.simulator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;

import javax.vecmath.Point2d;

import org.jgrapht.EdgeFactory;

import com.hottestseason.hokolator.Map;
import com.hottestseason.hokolator.Map.Intersection;
import com.hottestseason.hokolator.Map.Street;
import com.hottestseason.hokolator.Pedestrian;
import com.hottestseason.hokolator.Place;
import com.hottestseason.hokolator.graph.GeometricGraph;

public class PedestriansSimulatorTest {
	private PedestriansSimulator simulator;
	private Map map;
	private Pedestrian p1;
	private Pedestrian p2;

	// 1 -> 2 -> 3 -> 4
	// 1: 1 (0), 2: 1 (0)
	public void prepare(String type) {
		map = new Map();
		map.generateGrid(1, 1.5, 1, 4, new EdgeFactory<Map.Intersection, Map.Street>() {
			@Override
			public Street createEdge(Intersection sourceVertex, Intersection targetVertex) {
				return map.new Street(map.getStreets().size() + 1, 1);
			}
		}, new GeometricGraph.VertexFactory<Map.Intersection>() {
			@Override
			public Intersection createVertex(Point2d point) {
				return map.new Intersection(map.getIntersections().size() + 1, point);
			}
		});
		simulator = PedestriansSimulator.create(type, map);
		Map.Street startStreet = map.getStreet(1, 2);
		Map.Intersection goal = map.getIntersection(4);
		startStreet.width = 2;

		p1 = PedestriansSimulator.createPedestrian(type, simulator, 1, goal, 1);
		p2 = PedestriansSimulator.createPedestrian(type, simulator, 2, goal, 1.6);
		simulator.pedestrians.addAll(Arrays.asList(p1, p2));
		p1.moveTo(new Place(startStreet, 0));
		p2.moveTo(new Place(startStreet, 0));

		assertThat(p1.getPlace(), is(new Place(map.getStreet(1, 2), 0)));
		assertThat(p2.getPlace(), is(new Place(map.getStreet(1, 2), 0)));
	}

	public void testUpdate(String type) throws InterruptedException {
		prepare(type);

		simulator.update();

		// 1: 1 (1.0), 2: 2 (0.1)
		assertThat(p1.getPlace().street, is(map.getStreet(1, 2)));
		assertThat(p1.getPlace().position, is(closeTo(1.0, 0.01)));
		assertThat(p2.getPlace().street, is(map.getStreet(2, 3)));
		assertThat(p2.getPlace().position, is(closeTo(0.1, 0.01)));

		simulator.update();

		// 1: 1 (1.5), 2: 3 (0.2)
		assertThat(p1.getPlace().street, is(map.getStreet(1, 2)));
		assertThat(p1.getPlace().position, is(closeTo(1.5, 0.01)));
		assertThat(p2.getPlace().street, is(map.getStreet(3, 4)));
		assertThat(p2.getPlace().position, is(closeTo(0.2, 0.01)));

		simulator.update();

		// 1: 2 (1.0), 2: finished
		assertThat(p1.getPlace().street, is(map.getStreet(2, 3)));
		assertThat(p1.getPlace().position, is(closeTo(1.0, 0.01)));
		assertThat(p2.isFinished(), is(true));
	}
}