package com.mesaverde.specialization.allocation;

import java.util.ArrayList;
import java.util.HashMap;

import com.mesaverde.specialization.Constraints;
import com.mesaverde.specialization.SpecializedAgent;
import com.mesaverde.specialization.tasks.Hunter;
import com.mesaverde.specialization.tasks.Task;
import com.mesaverde.village.Village;

/** If you extend this class, make sure to handle people who did 0 of a Task in the previous year,
 * maybe they want to do more this year. */
public abstract class AllocationStrategy {	
	protected boolean firstYear = true;
	protected HashMap<Task, Double> weights = new HashMap<Task, Double>();
	protected HashMap<Task, Constraints> constraints = new HashMap<Task, Constraints>();
	protected ArrayList<Task> tasks;
	protected Constraints group = new Constraints(0, 0);  // starts at 0	
	protected SpecializedAgent agent;	

	protected AllocationStrategy(SpecializedAgent agent) {
		this.agent = agent;
	}

	/** Sets the weights of tasks for the current year.
	 */
	protected abstract void updateWeights();

	/** Sets the allocations for the current year.
	 * It does this by updating the constraints based on calsAvailable.
	 * It also normalizes all the weights so that they sum to 1. */
	public void update(double calsAvailable) {
		correctHuntingWeight();
		//normalizeWeights();
		
		updateWeights();
		normalizeWeights();

		for (Task c : tasks) {
			double cals = calsAvailable * weights.get(c);
			
			// DC: Dealing with a couple bugs in Main village			
			if (cals < 0 || cals > calsAvailable)
				cals = 0;
			
			// still just assuming time to be measured in man hours.
			// time isn't used in calculating amount of work right now, so no effect
			double time = cals / Village.WORK_CAL_MAN; 		
			Constraints cons = new Constraints(cals, time, group);			
			constraints.put(c, cons);
		}
	}

	/** Ensure that the hunting weight is reflective of the effort put into it in the previous year */
	private void correctHuntingWeight() {
		Hunter h = null;
		
		// find our hunter
		for (Task t : tasks)
			if (t instanceof Hunter)
				h = (Hunter) t;
		
		if (h != null) {
			Constraints c = constraints.get(h);
			
			if (c != null)
				h.correctWeight(c, weights);
		}
	}

	protected void normalizeWeights() {
		double weightSum = 0;
		double newTotal = 0;

		for (Task c : tasks) {
			Double d = weights.get(c);
			
			weightSum += d;
		}
		
		// now normalize them so they all total 1
		for (Task c : tasks) {
			double temp = weights.get(c) / weightSum;
			weights.put(c, temp);
			newTotal += temp;
		}		
	}

	/** This method needs to be called at the beginning of each season.
	 *  It updates the season constraints for the Task, so that an agent will not try to 
	 *  do more work than is possible in a season.
	 */
	public void updateSeason(double calsForYear) {		
		double calsSeason = calsForYear / 4;
		group.setAvailableCalories(calsSeason);
		group.setAvailableTime(calsSeason / Village.WORK_CAL_MAN);
	}


	public Constraints getConstraintsFor(Task task) {
		return constraints.get(task);
	}

	public void setTasks(ArrayList<Task> tasks) {
		this.tasks = tasks;
	}

	public void setWeights(HashMap<Task, Double> weights) {
		// this automatically means that we don't have to initialize the weights ourselves
		firstYear = false;
		this.weights = weights;
		normalizeWeights();
	}

	public HashMap<Task, Double> getWeights() {
		return weights;
	}

	public ArrayList<Task> getTasks() {		
		return tasks;
	}	
}
