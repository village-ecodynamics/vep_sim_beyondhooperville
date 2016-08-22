package com.mesaverde.specialization.resources;

public abstract class Resource {
	public static final int EMPTY = 0;  // for when we don't want to include a cost, like when specifying needs
	protected int amount;
	protected double cost;
	// children classes should set this amount based on their default max storage
	// SpecializedAgents can change this value for their own resources in their ResourceManager
	protected double maxStorage;
	protected double decayRate = 0d;  // the rate at which this resource decays if any 

	protected Resource(int amount, double cost, double defaultMaxStorage) {
		this.amount = amount;
		this.cost = cost;
		this.maxStorage = defaultMaxStorage;
	}

	public void decreaseAmount(int amountToDecrease) {	
		if (amount != 0) {
			cost *= (1 - amountToDecrease / amount); // decrease the costs by the same ratio			
		}
		
		this.amount -= amountToDecrease;
	}

	/** Amount of this resource */
	public int getAmount() {
		return amount;
	}

	public double getCost() {
		return cost;
	}

	public double getMaxStorage() {
		return maxStorage;
	}

	/** The level at which we believe we have enough resources stored */
	public double getSatisfactoryAmount() {
		return maxStorage * 0.5;
	}

	/** The level at which we believe that we need more resources. CRITICAL state. */ 
	public int getShortageThreshold() {		
		return (int) (maxStorage * 0.25);
	}

	/** Returns the amount above which an agent will try to trade away this resource.
	 * This is equivalent to the PHILANTHROPIC setting in Agent.  Currently it returns the value of max storage.
	 * You should override it for your resource if you want a different setting. */
	public double getTradeThresholdAmount() {
		return maxStorage;
	}
	
	/** Cost per unit of this resource */
	public double getUnitCost() {
		return getCost() / getAmount();
	}

	public void increaseAmount(double d, double costToGetIt) {
		this.amount += d;
		this.cost += costToGetIt;		
	}

	/** Calculate the cost of transporting the resource */
	// A great and realistic idea that's not really implemented anywhere in the simulation
	//public abstract double calcTransportCost(int amount, double distance);

	public void increaseCost(double amount) {
		cost += amount;		
	}

	/** Create a copy of this resource with the amount of units and costForIt */
	public abstract Resource makeInstance(int amount, double costForIt);

	public void setMaxStorage(double maxStore) {		
		this.maxStorage = maxStore;
	}

	public double getDecayRate() {
		return decayRate;
	}

    public abstract double calculateTransportCost(double amount, double distance);
}
