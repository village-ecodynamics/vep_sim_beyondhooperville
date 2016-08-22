package com.mesaverde.village;

public class ImaginaryCell implements Comparable<ImaginaryCell>{
	int x;
	int y;

	int productivity;
	int numPlots;
	boolean waterNearby;
	int population;
	
	public int getPopulation() {
		return population;
	}

	public void setPopulation(int population) {
		this.population = population;
	}

	int hunt_cost;
	int water_cost;
	int wood_cost;
	
	int potentialEnergy;
	
	

	public ImaginaryCell(){
		
	}
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getProductivity() {
		return productivity;
	}

	public void setProductivity(int productivity) {
		this.productivity = productivity;
	}

	public int getNumPlots() {
		return numPlots;
	}

	public void setNumPlots(int numPlots) {
		this.numPlots = numPlots;
	}

	public boolean isWaterNearby() {
		return waterNearby;
	}

	public void setWaterNearby(boolean waterNearby) {
		this.waterNearby = waterNearby;
	}

	public int getHunt_cost() {
		return hunt_cost;
	}

	public void setHunt_cost(int hunt_cost) {
		this.hunt_cost = hunt_cost;
	}

	public int getWater_cost() {
		return water_cost;
	}

	public void setWater_cost(int water_cost) {
		this.water_cost = water_cost;
	}

	public int getWood_cost() {
		return wood_cost;
	}

	public void setWood_cost(int wood_cost) {
		this.wood_cost = wood_cost;
	}

	public int getPotentialEnergy() {
		return potentialEnergy;
	}

	public void setPotentialEnergy(int potentialEnergy) {
		this.potentialEnergy = potentialEnergy;
	}
	
	public int compareTo(ImaginaryCell compareCell) {
		 
		int compareQuantity = ((ImaginaryCell) compareCell).getPotentialEnergy(); 
 
		//ascending order
//		return this.potentialEnergy - compareQuantity;
 
		//descending order
		return compareQuantity - this.potentialEnergy;
	}
}
