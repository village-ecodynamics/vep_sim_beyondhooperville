package com.mesaverde.specialization.allocation;

import java.util.ArrayList;
import java.util.HashMap;

import com.mesaverde.specialization.SpecializedAgent;
import com.mesaverde.specialization.resources.Resource;
import com.mesaverde.specialization.tasks.Task;

/** Based on the StorageBasedStrategy, but also includes social pressure */
public class SocialEconomicStrategy extends TrendMaxStorageStrategy {
	private static final double CHANGE_RATE = TrendMaxStorageStrategy.MAX_DECLINE;		    
	private HashMap<Task, Double> currentValues;	
	
	public SocialEconomicStrategy(SpecializedAgent agent) {
		super(agent);
	}

	@Override
    /* TODO: uncomment the updatewights and the increase weights, maybe */
	protected void updateWeights() {
		currentValues = (HashMap<Task, Double>) weights.clone();
        if (SpecializedAgent.ENABLE_SPECIALIZATION &&
                !SpecializedAgent.APPLY_DEMAND_PRESSURE) {
            super.updateWeights();
        }
		
		if (SpecializedAgent.USE_SOCIAL_INFLUENCE_FOR_SPECIALIZATION) {
			for (Task c : tasks) {	
				// we are the only ones that increase the weights
				//if (weights.get(c) > currentValues.get(c))
				//	weights.put(c, currentValues.get(c));
				socialUpdate(c);
			}
		}
	}


	private Task getHisTask(SpecializedAgent neighbour, Task c) {
		ArrayList<Task> hisTasks = neighbour.getAllocationStrategy().tasks;
		
		for (Task t : hisTasks) {
			if (t.getClass() == c.getClass())
				return t;
		}
		
		return null;
	}

	/** Applies social pressure to update weight for task. */
	private void socialUpdate(Task c) {
		Double pressure = agent.getSocialPressureFor(c);
		
		if (pressure != null && pressure != 0) {
			Double originalWeight = currentValues.get(c);						

			if (originalWeight == null || originalWeight.isNaN() || originalWeight == 0)
				originalWeight = DEFAULT_WEIGHT;
									
			Resource r = agent.getResource(c.getResourceType());			
			
			/* If we have too little of the resource, then ignore downward pressure */
			if (r.getAmount() < (r.getSatisfactoryAmount() * (1-r.getDecayRate())) && pressure < 0) {
				return;
			}

            /* Now ignore upward pressure if we have too much (let's say more than 5 years worth) */
            if (r.getAmount() >= r.getMaxStorage() * 2.5 && pressure > 0)
                return;
			
			int unitCount = c.getUnitCount();
			
			// This is rather ad-hoc.  Dealing with 0 is a very difficult special case.
			// How does one really improve from 0?
			if (unitCount == 0) {
				if (pressure > 0) {
					unitCount = (int) (pressure / 2);  // assume we had produced 50% of what the pressure indicates
				} else {
					// otherwise, assume we had produced 10 times pressure
					// this will cause a reduction by 10%
					// which leaves 90% of 0 really, but that's hard to qualify that way
					// since the 0 may have been an anomaly
					unitCount = (int) (pressure * -10); 
				}
			}				
			
			double changeNeeded = pressure / unitCount;
			
			// never let it decrease by more than CHANGE_RATE			
			//changeNeeded = Math.max(changeNeeded, CHANGE_RATE * -1);
			
			Double currentWeight = weights.get(c);  // weight after economic update
			
			if (currentWeight == null || currentWeight == 0 || currentWeight.isNaN())
				currentWeight = DEFAULT_WEIGHT;
			
			double change = originalWeight * changeNeeded;
			//change = Math.max(change, currentWeight * -CHANGE_RATE);
            change = currentWeight + change;
            change = (change / originalWeight) - 1;

            // now limit the total change within our ranges
            change = Math.max(change, -MAX_DECLINE);
            change = Math.min(change, MAX_INCREASE);

            // trying to see if we can introduce a factor so that we produce
            // less the more we have over            
            double additionalChange = 0;
            if (change == -MAX_DECLINE && (changeNeeded <= -1)  && r.getMaxStorage() < r.getAmount()) {
                additionalChange = - 0.25 + 0.25 * r.getMaxStorage() / r.getAmount();
            }

            if (change == MAX_INCREASE && (changeNeeded >= MAX_INCREASE + 1) && r.getMaxStorage() > r.getAmount()) {
                additionalChange = + 1 - r.getAmount() / r.getMaxStorage();
            }            
            change += additionalChange;

			Double newVal = originalWeight * (1 + change);

			weights.put(c, newVal);				
		}									
	}
}
