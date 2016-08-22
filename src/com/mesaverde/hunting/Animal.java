package com.mesaverde.hunting;

import com.mesaverde.village.Cell;

public abstract class Animal {
	protected double amountHunted;
	protected double amount;
	protected Cell location;
	
	public void setLocation(Cell c) {
		location = c;
	}
	
	public Cell getLocation() {
		return location;
	}	
	
	public double getAmountHunted() {
		return amountHunted;
	}

	public void setAmountHunted(int i) {
		amountHunted = i;
	}
	
	public void huntAnimals(int amount_killed) {
		amountHunted = amount_killed;
        setAmount(getAmount() - amount_killed);
	}

	public void setAmount(double i) {
		amount = i;
	}
	
	public double getAmount() {
		return amount;
	}
}
