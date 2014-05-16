package com.hottestseason.hokolator.graph;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2d;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

public class GeometricGraph<V extends Point2d, E> extends DefaultDirectedWeightedGraph<V, E> {
	private static final long serialVersionUID = 3704341349469880704L;

	public GeometricGraph(Class<? extends E> edgeClass) {
		super(edgeClass);
	}

	public GeometricGraph(EdgeFactory<V, E> edgeFactory) {
		super(edgeFactory);
	}

	@Override
	public double getEdgeWeight(E edge) {
		return getEdgeSource(edge).distance(getEdgeTarget(edge));
	}

	public void generateGrid(double height, double width, int row, int column, VertexFactory<V> vertexFactory) {
		List<V> vertices = new ArrayList<>();
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < column; j++) {
				V vertex = vertexFactory.createVertex(new Point2d(width * j, height * i));
				addVertex(vertex);
				vertices.add(vertex);
				if (i > 0) addEdgeBidirectory(vertex, vertices.get(column * (i - 1) + j));
				if (j > 0) addEdgeBidirectory(vertex, vertices.get(column * i + j - 1));
			}
		}
	}

	private void addEdgeBidirectory(V vertex, V anotherVertex) {
		addEdge(vertex, anotherVertex, getEdgeFactory().createEdge(vertex, anotherVertex));
		addEdge(anotherVertex, vertex, getEdgeFactory().createEdge(anotherVertex, vertex));
	}

	public interface VertexFactory<V> {
		V createVertex(Point2d point);
	}
}
