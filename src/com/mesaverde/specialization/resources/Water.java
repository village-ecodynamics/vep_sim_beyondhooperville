package com.mesaverde.specialization.resources;

import com.mesaverde.village.Village;


public class Water extends Resource {
    private double CARRY_CAPACITY = Village.CARRY_CAPACITY - 4;

	public Water(int amount, double cost) {
		super(amount, cost, 20000);		
	}

	@Override	
	public Water makeInstance(int amount, double costForIt) {
		Water w = new Water(amount, costForIt);
		w.setMaxStorage(maxStorage);

		return w;
	}

    @Override   
    public double calculateTransportCost(double amount, double distance) {       
        double trips = amount / CARRY_CAPACITY;

        /* we also have no way that we actually factor in the increased weight on the cost of transport
        // oh well. */
        double hours = trips * distance * 2 / Village.TRAVEL_SPEED;

        return Village.WORK_CAL_WOM * hours;
    }
}
