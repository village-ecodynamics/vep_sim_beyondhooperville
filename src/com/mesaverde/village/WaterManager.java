package com.mesaverde.village;

import java.util.ArrayList;
import com.mesaverde.model.AgentModelSwarm;

public class WaterManager {
	private ArrayList<Cell> waterCells = new ArrayList<Cell>();
	private AgentModelSwarm swarm;
    public  static int WATER_CARRY_CAPACITY = Village.CARRY_CAPACITY - 4;
	
	public WaterManager(AgentModelSwarm swarm) {
		this.swarm = swarm;		
	}
	
	/** Calculates the distance between 2 cells.  Works with the Torus world concept */
	public static double distance(Cell cell1, Cell cell2) {
		double diffX = Math.abs(cell1.getX() - cell2.getX());
		double halfWorldX = Village.WORLD_X_SIZE / 2;
		if (diffX > halfWorldX)
			diffX = halfWorldX * 2 - diffX;

		double diffY = Math.abs(cell1.getY() - cell2.getY());
		double halfWorldY = Village.WORLD_Y_SIZE / 2;
		if (diffY > halfWorldY)
			diffY = halfWorldY * 2 - diffY;

		double distance = Math.sqrt(Math.pow(diffX, 2)
				+ Math.pow(diffY, 2));

		return distance;
	}

	public void update() {
		waterCells.clear();
		ArrayList<Cell> cellList = swarm.getCellList();
		for (Cell c : cellList) {
			if (c.getWaterType() >= Village.H2O_TYPE) {
				waterCells.add(c);
			}		
		}
	}

    /** Returns the list of water cells */
    public ArrayList<Cell> getWaterCells() {
        return waterCells;
    }
}
