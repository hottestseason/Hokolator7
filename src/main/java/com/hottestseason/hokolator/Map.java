package com.hottestseason.hokolator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.vecmath.Point2d;

import org.jgrapht.EdgeFactory;
import org.jgrapht.alg.DijkstraShortestPath;

import com.hottestseason.hokolator.graph.GeometricGraph;
import com.hottestseason.hokolator.graph.GeometricGraph.VertexFactory;

public class Map {
	private GeometricGraph<Map.Intersection, Map.Street> graph;
	private final java.util.Map<Integer, Intersection> intersectionMap = new HashMap<>();
	private final java.util.Map<Integer, Street> streetMap = new HashMap<>();

	public static Street[] sortLinks(Street...links) {
		Arrays.sort(links);
		return links;
	}

	public void generateGrid(double height, double width, int row, int column, final EdgeFactory<Intersection, Street> edgeFactory, final VertexFactory<Intersection> vertexFactory) {
		graph = new GeometricGraph<>(new EdgeFactory<Intersection, Street>() {
			@Override
			public Street createEdge(Intersection sourceVertex, Intersection targetVertex) {
				Street street = edgeFactory.createEdge(sourceVertex, targetVertex);
				streetMap.put(street.id, street);
				return street;
			}
		});
		graph.generateGrid(height, width, row, column, new VertexFactory<Intersection>() {
			@Override
			public Intersection createVertex(Point2d point) {
				Intersection intersection = vertexFactory.createVertex(point);
				intersectionMap.put(intersection.id, intersection);
				return intersection;
			}
		});
	}

	public void add(Intersection intersection) {
		graph.addVertex(intersection);
	}

	public Set<Intersection> getIntersections() {
		return graph.vertexSet();
	}

	public Intersection getIntersection(int id) {
		return intersectionMap.get(id);
	}

	public Set<Street> getStreets() {
		return graph.edgeSet();
	}

	public Street getStreet(int id) {
		return streetMap.get(id);
	}

	public Street getStreet(Intersection source, Intersection target) {
		return graph.getEdge(source, target);
	}

	public Street getStreet(int sourceId, int targetId) {
		return getStreet(getIntersection(sourceId), getIntersection(targetId));
	}

	public List<Street> findShortestPathBetween(Intersection start, Intersection end) {
		return new DijkstraShortestPath<>(graph, start, end).getPathEdgeList();
	}

	public class Intersection extends Point2d {
		private static final long serialVersionUID = -9199744699646365822L;
		public final int id;

		public Intersection(int id, Point2d point) {
			super(point);
			this.id = id;
		}

		public Set<Street> getStreets() {
			return graph.edgesOf(this);
		}

		public Set<Street> getOutgoingStreets() {
			return graph.outgoingEdgesOf(this);
		}

		public Set<Street> getIncomingStreets() {
			return graph.incomingEdgesOf(this);
		}

		public List<Street> findShortestPathTo(Intersection intersection) {
			return findShortestPathBetween(this, intersection);
		}
	}

	public class Street implements Comparable<Street> {
		public final int id;
		public int width;
		private final UUID uuid = UUID.randomUUID();
		private final Set<Pedestrian> pedestrians = new HashSet<>();

		public Street(int id, int width) {
			this.id = id;
			this.width = width;
		}

		@Override
		public String toString() {
			return getSource() + " => " + getTarget();
		}

		@Override
		public int compareTo(Street street) {
			return uuid.compareTo(street.uuid);
		}

		public Intersection getSource() {
			return graph.getEdgeSource(this);
		}

		public Intersection getTarget() {
			return graph.getEdgeTarget(this);
		}

		public double getLength() {
			return getSource().distance(getTarget());
		}

		public Intersection getIntersectionWith(Street street) {
			if (getTarget() == street.getSource() || getTarget() == street.getTarget()) {
				return getTarget();
			} else if (getSource() == street.getSource() || getSource() == street.getTarget()) {
				return getSource();
			} else {
				return null;
			}
		}

		public boolean canEnter() {
			synchronized (pedestrians) {
				return pedestrians.size() < width;
			}
		}

		public boolean accept(Pedestrian pedestrian) {
			if (pedestrian.getPlace() == null) {
				synchronized (pedestrians) {
					if (canEnter()) {
						return pedestrians.add(pedestrian);
					} else {
						return false;
					}
				}
			} else {
				Street[] links = Map.sortLinks(pedestrian.getPlace().street, this);
				synchronized (links[0].pedestrians) {
					synchronized (links[1].pedestrians) {
						if (canEnter()) {
							pedestrian.getPlace().street.remove(pedestrian);
							pedestrians.add(pedestrian);
							return true;
						} else {
							return false;
						}
					}
				}
			}
		}

		public boolean remove(Pedestrian pedestrian) {
			synchronized (pedestrians) {
				return pedestrians.remove(pedestrian);
			}
		}
	}
}
