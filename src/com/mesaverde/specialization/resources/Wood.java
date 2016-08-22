package com.mesaverde.specialization.resources;

import com.mesaverde.village.Village;

public class Wood extends Resource {
    private double CARRY_CAPACITY = Village.CARRY_CAPACITY;

	public Wood(int amount, double cost) {
		super(amount, cost, 10000);		
	}

	@Override
	public Wood makeInstance(int amount, double costForIt) {
		Wood w = new Wood(amount, costForIt);
		w.setMaxStorage(maxStorage);

		return w;
	}

     @Override  
    public double calculateTransportCost(double amount, double distance) {      
        double trips = amount / CARRY_CAPACITY;

        /* we also have no way that we actually factor in the increased weight on the cost of transport
        . oh well */
        double hours = trips * distance * 2 / Village.TRAVEL_SPEED;

        return Village.WORK_CAL_WOM * hours;
    }
}
