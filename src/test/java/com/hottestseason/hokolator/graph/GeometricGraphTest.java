package com.hottestseason.hokolator.graph;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2d;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.junit.Test;

public class GeometricGraphTest {
	@Test
	public void testGenerateGrid1() throws Exception {
		final List<Point2d> vertexes = new ArrayList<>();
		GeometricGraph<Point2d, DefaultEdge> graph = new GeometricGraph<>(DefaultEdge.class);
		graph.generateGrid(1, 1, 1, 2, new GeometricGraph.VertexFactory<Point2d>() {
			@Override
			public Point2d createVertex(Point2d point) {
				vertexes.add(point);
				return point;
			}
		});
		assertEquals(2, vertexes.size());
		assertEquals(2, graph.edgeSet().size());
		List<DefaultEdge> edges = DijkstraShortestPath.findPathBetween(graph, vertexes.get(0), vertexes.get(1));
		assertEquals(1, edges.size());
		assertEquals(new Point2d(0.0, 0.0), graph.getEdgeSource(edges.get(0)));
		assertEquals(new Point2d(1.0, 0.0), graph.getEdgeTarget(edges.get(0)));
	}

	@Test
	public void testGenerateGrid2() throws Exception {
		final List<Point2d> vertexes = new ArrayList<>();
		GeometricGraph<Point2d, DefaultEdge> graph = new GeometricGraph<>(DefaultEdge.class);
		graph.generateGrid(1, 1, 2, 3, new GeometricGraph.VertexFactory<Point2d>() {
			@Override
			public Point2d createVertex(Point2d point) {
				vertexes.add(point);
				return point;
			}
		});
		assertEquals(6, vertexes.size());
		assertEquals(14, graph.edgeSet().size());
		List<DefaultEdge> edges = DijkstraShortestPath.findPathBetween(graph, vertexes.get(0), vertexes.get(5));
		assertEquals(3, edges.size());
	}
}
