package com.hottestseason.hokolator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.vecmath.Point2d;

import org.jgrapht.EdgeFactory;

import com.hottestseason.hokolator.Map.Intersection;
import com.hottestseason.hokolator.Map.Street;
import com.hottestseason.hokolator.graph.GeometricGraph;

public class PedestriansSimulator {
	public final Map map;
	public final Set<Pedestrian> pedestrians = new HashSet<>();
	public final double timeLimit;

	public static void main(String[] args) throws InterruptedException, FileNotFoundException, IOException {
		final Properties properties = new Properties();
		properties.load(new FileInputStream(args[0]));
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
		PedestriansSimulator simulator = new PedestriansSimulator(map, timeLimit);
		for (int i = 0; i < numOfPedestrians; i++) {
			Map.Intersection goal = i % 2 == 0 ? goal1 : goal2;
			Pedestrian pedestrian = new Pedestrian(simulator, i, goal, Utils.random.nextDouble() * (maxSpeed - minSpeed) + minSpeed);
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

	public PedestriansSimulator(Map map, double timeLimit) {
		this.map = map;
		this.timeLimit = timeLimit;
	}

	public PedestriansSimulator(Map map) {
		this(map, Double.MAX_VALUE);
	}

	public void run() throws InterruptedException {
		double time = 0;
		while (time <= timeLimit) {
			time += 1.0;
			AgentsScheduler.update(pedestrians, 1.0);
			int numOfFinishedPedestrians = 0;
			for (Pedestrian pedestrian : pedestrians) {
				if (pedestrian.isAtGoal()) numOfFinishedPedestrians++;
			}
			System.out.println(time + ": " + numOfFinishedPedestrians);
			if (numOfFinishedPedestrians == pedestrians.size()) break;
		}
		List<Pedestrian> sortedPedestrians = new ArrayList<>(pedestrians);
		Collections.sort(sortedPedestrians, new Comparator<Pedestrian>() {
			@Override
			public int compare(Pedestrian p1, Pedestrian p2) {
				return Integer.compare(p1.id, p2.id);
			}
		});;
		for (Pedestrian pedestrian : sortedPedestrians) {
			System.out.println(pedestrian.id + ": " + pedestrian.getTime());
		}
	}
}
