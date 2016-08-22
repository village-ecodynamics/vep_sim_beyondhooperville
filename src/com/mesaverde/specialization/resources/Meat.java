package com.mesaverde.specialization.resources;

import com.mesaverde.village.Village;


public class Meat extends Resource {
	public static final double DECAY_RATE = 0.25d;
    private static final double CARRY_CAPACITY = Village.CARRY_CAPACITY;
	
	/** Maximum storage for meat is dependent upon the family size, so we'll have it default to 50 KG, but
	 * agents are expected to set this for themselves */
	public Meat(int amount, double cost) {
		super(amount, cost, 50000);		
		decayRate = DECAY_RATE;
	}

	@Override
	public Meat makeInstance(int amount, double costForIt) {
		Meat m = new Meat(amount, costForIt);
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
