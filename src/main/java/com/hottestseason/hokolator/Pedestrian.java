package com.hottestseason.hokolator;

import java.util.List;

public class Pedestrian extends Agent {
	public final PedestriansSimulator simulator;
	public final int id;
	public final Map.Intersection goal;
	private double time;
	private double speed;
	private Place place;
	private Place nextPlace;

	public Pedestrian(PedestriansSimulator simulator, int id, Map.Intersection goal, double speed) {
		this.simulator = simulator;
		this.id = id;
		this.goal = goal;
		this.speed = speed;
	}

	@Override
	public void update(double time) {
		if (!isAtGoal()) {
			speed = calcSpeed();
			nextPlace = calcNextPlace(time);
			moveTo(nextPlace);
			this.time += time;
		}
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
}
