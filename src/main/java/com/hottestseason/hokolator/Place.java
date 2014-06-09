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
	public boolean equals(Object object) {
		if (object instanceof Place) {
			Place place = (Place) object;
			return street.equals(place.street) && position == place.position;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return street + " (" + position + ")";
	}
}