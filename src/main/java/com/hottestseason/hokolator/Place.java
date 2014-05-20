package com.hottestseason.hokolator;

public class Place {
	public final Map.Street street;
	public final double position;

	public Place(Map.Street street, double position) {
		assert street.getLength() >= position: "invalid position";
		this.street = street;
		this.position = position;
	}

	@Override
	public String toString() {
		return street + " (" + position + ")";
	}
}