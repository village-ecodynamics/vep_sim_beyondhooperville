package com.mesaverde.specialization.resources;

import com.mesaverde.village.Village;

public class Maize extends Resource {
    public static final double CARRY_CAPACITY = Village.CARRY_CAPACITY;
    
	public Maize(int amount, double cost) {
		super(amount, cost, 700);
		decayRate = Village.MAIZE_STORAGE_DECR;
	}

	@Override
	public Maize makeInstance(int amount, double costForIt) {	
		Maize m = new Maize(amount, costForIt);
		m.setMaxStorage(maxStorage);

		return m;
	}

    @Override
    /* TODO: Have this verified by Kohler */
    public double calculateTransportCost(double amount, double distance) {       
        double trips = amount / CARRY_CAPACITY;

        // we also have no way that we actually factor in the increased weight on the cost of transport
        // oh well.
        double hours = trips * distance * 2 / Village.TRAVEL_SPEED;

        return Village.WORK_CAL_MAN * hours;
    }
}
