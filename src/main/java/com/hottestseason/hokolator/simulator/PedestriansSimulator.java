package com.hottestseason.hokolator.simulator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.vecmath.Point2d;

import org.jgrapht.EdgeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hottestseason.hokolator.BarrierAllPedestrian;
import com.hottestseason.hokolator.Map;
import com.hottestseason.hokolator.Map.Intersection;
import com.hottestseason.hokolator.Map.Street;
import com.hottestseason.hokolator.ParallelPedestrian;
import com.hottestseason.hokolator.Pedestrian;
import com.hottestseason.hokolator.Place;
import com.hottestseason.hokolator.graph.GeometricGraph;
import com.hottestseason.hokolator.util.Counter;
import com.hottestseason.hokolator.util.Utils;

abstract public class PedestriansSimulator {
	public final Logger logger = LoggerFactory.getLogger(getClass());
	public final Map map;
	public final Set<Pedestrian> pedestrians = new HashSet<>();
	public final double timeunit = 1.0;
	public final Counter finishedCounter = new Counter();
	public double timeLimit = Double.MAX_VALUE;

	public static void main(String[] args) throws InterruptedException, FileNotFoundException, IOException {
		final Properties properties = new Properties();
		properties.load(ClassLoader.getSystemResourceAsStream("simulator.properties"));
		final String type = properties.getProperty("type");
		final int row = Integer.valueOf(properties.getProperty("row"));
		final int column = Integer.valueOf(properties.getProperty("column"));
		final int linkLength = Integer.valueOf(properties.getProperty("linkLength"));
		final int linkWidth = Integer.valueOf(properties.getProperty("linkWidth"));
		final int numOfPedestrians = Integer.valueOf(properties.getProperty("numOfPedestrians"));
		final double minSpeed = Double.valueOf(properties.getProperty("minSpeed"));
		final double maxSpeed = Double.valueOf(properties.getProperty("maxSpeed"));
		final double timeLimit = properties.containsKey("timeLimit") ? Double.valueOf(properties.getProperty("timeLimit")) : Double.MAX_VALUE;

		final Map map = new Map();
		map.generateGrid(linkLength, linkLength, row, column, new EdgeFactory<Map.Intersection, Map.Street>() {
			@Override
			public Street createEdge(Intersection sourceVertex,	Intersection targetVertex) {
				return map.new Street(map.getStreets().size() + 1, linkWidth);
			}
		}, new GeometricGraph.VertexFactory<Map.Intersection>() {
			@Override
			public Intersection createVertex(Point2d point) {
				return map.new Intersection(map.getIntersections().size() + 1, point);
			}
		});
		Map.Intersection goal1 = map.getIntersection(1);
		Map.Intersection goal2 = map.getIntersection(map.getIntersections().size());

		PedestriansSimulator simulator = create(type, map);
		simulator.timeLimit = timeLimit;

		for (int i = 0; i < numOfPedestrians; i++) {
			Map.Intersection goal = i % 2 == 0 ? goal1 : goal2;
			Pedestrian pedestrian = createPedestrian(type, simulator, i, goal, Utils.random.nextDouble() * (maxSpeed - minSpeed) + minSpeed);
			simulator.pedestrians.add(pedestrian);
			while (true) {
				Map.Street street = map.getStreet(Utils.random.nextInt(map.getStreets().size()));
				if (pedestrian.moveTo(new Place(street, street.getLength() * Utils.random.nextDouble()))) {
					break;
				}
			}
		}
		long start = System.currentTimeMillis();
		simulator.run();
		System.out.println(System.currentTimeMillis() - start);
	}

	public static PedestriansSimulator create(String type, Map map) {
		switch (type) {
		case "parallel":
			return new ParallelPedestriansSimulator(map);
		case "barrierAll":
			return new BarrierAllPedestriansSimulator(map);
		default:
			return null;
		}
	}

	public static Pedestrian createPedestrian(String type, PedestriansSimulator simulator, int id, Map.Intersection goal, double speed) {
		switch (type) {
		case "parallel":
			return new ParallelPedestrian(simulator, id, goal, speed);
		case "barrierAll":
			return new BarrierAllPedestrian(simulator, id, goal, speed);
		default:
			return null;
		}
	}

	public PedestriansSimulator(Map map) {
		this.map = map;
	}

	abstract public void update() throws InterruptedException;

	public void run() throws InterruptedException {
		double time = 0;
		while (time < timeLimit) {
			time += timeunit;
			update();

			logger.info("time: " + time + " ----------");
			if (logger.isDebugEnabled()) debugPedestrians();
			logger.info("unfinished: " + (pedestrians.size() - finishedCounter.get()));

			if (finishedCounter.get() == pedestrians.size()) break;
		}

		logger.info("finished ----------");
		for (Pedestrian pedestrian : Pedestrian.sort(pedestrians)) logger.info(pedestrian.id + ": " + pedestrian.getIteration());
	}

	private void debugPedestrians() {
		for (Pedestrian pedestrian : Pedestrian.sort(pedestrians)) logger.debug(pedestrian.toString());
	}
}
