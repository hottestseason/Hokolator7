package com.hottestseason.hokolator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Pedestrian extends Agent {
	public final PedestriansSimulator simulator;
	public final int id;
	public final Map.Intersection goal;
	private double time;
	private double speed;
	private Place place;
	private Place nextPlace;
	private double linkLeftTime;

	public static List<Pedestrian> sort(Collection<Pedestrian> pedestrians, Comparator<Pedestrian> comparator) {
		List<Pedestrian> sortedPedestrians = new ArrayList<>(pedestrians);
		Collections.sort(sortedPedestrians, comparator);
		return sortedPedestrians;
	}

	public static List<Pedestrian> sort(Collection<Pedestrian> pedestrians) {
		return sort(pedestrians, new Comparator<Pedestrian>() {
			@Override
			public int compare(Pedestrian p1, Pedestrian p2) {
				return Integer.compare(p1.id, p2.id);
			}
		});
	}

	public Pedestrian(PedestriansSimulator simulator, int id, Map.Intersection goal, double speed) {
		this.simulator = simulator;
		this.id = id;
		this.goal = goal;
		this.speed = speed;
	}

	@Override
	public String toString() {
		return "id: " + id + ", place: " + place + ", speed: " + speed;
	}

	@Override
	public void update(double time) throws InterruptedException {
		speed = calcSpeed();
		nextPlace = calcNextPlace(time);
		if (place.street != nextPlace.street) {
			linkLeftTime = calcLinkLeftTime();
			final Set<Pedestrian> neighborPedestrians = calcNeighborPedestrians();
			finished("nextPlaceCalculated");
			barrier("nextPlaceCalculated", neighborPedestrians, new Runnable() {
				@Override
				public void run() {
					Set<Pedestrian> conflictingPedestrians = calcConflictingPedestrians(neighborPedestrians);
					ordered("conflictingPedestrians", conflictingPedestrians, new Comparator<Agent>() {
						@Override
						public int compare(Agent a1, Agent a2) {
							return ((Pedestrian) a1).compareUsingLinkLeftTime((Pedestrian) a2);
						}
					}, new Runnable() {
						@Override
						public void run() {
							moveTo(nextPlace);
						}
					});
				}
			});
		} else {
			finished("nextPlaceCalculated");
			moveTo(nextPlace);
		}
		this.time += time;
	}

	public double getTime() {
		return time;
	}

	public Place getPlace() {
		return place;
	}

	public double getSpeed() {
		return speed;
	}

	public double calcSpeed() {
		return speed;
	}

	public Place calcNextPlace(double time) {
		double nextPosition = speed * time + place.position;
		if (nextPosition < place.street.getLength()) {
			return new Place(place.street, nextPosition);
		} else {
			List<Map.Street> path = place.street.getTarget().findShortestPathTo(goal);
			if (!path.isEmpty()) {
				return new Place(path.get(0), Math.min(nextPosition - place.street.getLength(), path.get(0).getLength()));
			} else {
				return new Place(place.street, place.street.getLength());
			}
		}
	}

	public boolean isAtGoal() {
		return place.street.getTarget() == goal && place.position == place.street.getLength();
	}

	public boolean moveTo(Place nextPlace) {
		if (place == null) {
			if (nextPlace.street.accept(this)) {
				place = nextPlace;
				return true;
			} else {
				return false;
			}
		} if (nextPlace == null) {
			return place.street.remove(this);
		} else {
			if (place.street == nextPlace.street) {
				place = nextPlace;
				return true;
			} else {
				Map.Intersection connectedNode = place.street.getIntersectionWith(nextPlace.street);
				assert connectedNode != null && connectedNode == place.street.getTarget() : "invalid destination";
				if (nextPlace.street.accept(this)) {
					place = nextPlace;
					return true;
				} else {
					place = new Place(place.street, place.street.getLength());
					return false;
				}
			}
		}
	}

	public boolean moveToNextPlace() {
		return moveTo(nextPlace);
	}

	public int compareUsingLinkLeftTime(Pedestrian pedestrian) {
		int result = Double.compare(linkLeftTime, pedestrian.linkLeftTime);
		if (result == 0) result = Integer.compare(id, pedestrian.id);
		return result;
	}

	private boolean willEnterOrExit(Map.Street street) {
		return (place.street != nextPlace.street) && (place.street == street || nextPlace.street == street);
	}

	private double calcLinkLeftTime() {
		return (place.street.getLength() - place.position) / speed;
	}

	private Set<Pedestrian> calcNeighborPedestrians() {
		Set<Pedestrian> neighborPedestrians = new HashSet<>(place.street.neighborPedestrians);
		neighborPedestrians.addAll(nextPlace.street.neighborPedestrians);
		return neighborPedestrians;
	}

	private Set<Pedestrian> calcConflictingPedestrians(Set<Pedestrian> neighborPedestrians) {
		Set<Pedestrian>  conflictingPedestrians = new HashSet<>();
		for (Pedestrian pedestrian : neighborPedestrians) {
			if (pedestrian.willEnterOrExit(place.street) || pedestrian.willEnterOrExit(nextPlace.street)) {
				conflictingPedestrians.add(pedestrian);
			}
		}
		return conflictingPedestrians;
	}
}
