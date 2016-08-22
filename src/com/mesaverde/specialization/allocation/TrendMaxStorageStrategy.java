package com.mesaverde.specialization.allocation;

import java.util.HashMap;
import com.mesaverde.specialization.SpecializedAgent;
import com.mesaverde.specialization.resources.*;
import com.mesaverde.specialization.tasks.*;

public class TrendMaxStorageStrategy extends AllocationStrategy {

	public static final double MAX_DECLINE = 0.5d; // we can decline up to 50% based purely on desire to reduce
	// the most to increase by, will also allow
	// increases projected to be less than 2 * max_storage
	public static final double MAX_INCREASE = 2;
	public static final double DEFAULT_WEIGHT = 0.01;
	private HashMap<Resource, Integer> previousProductionAmount;

	public TrendMaxStorageStrategy(SpecializedAgent agent) {
		super(agent);
		previousProductionAmount = agent.getProductionRecord();
	}

	@Override
	protected void updateWeights() {
		if (!firstYear) { // we don't do anything in the first year
			// TradeManager tm = TradeManager.getInstance();						
			for (Task c : tasks) {		
				Class<? extends Resource> resourceType = c.getResourceType();
				Resource resource = agent.getResource(resourceType);									
				
				Integer lastYear = previousProductionAmount.get(resource);
				if (lastYear == null)
					lastYear = 0;
				

				Double currentWeight = weights.get(c);		

				// if we produced none last year and had a shortage, we
				// better produce more this year
				// TODO: NOTE: we ensure that a needed task is seeded if it
				// wasn't before
				if (currentWeight == 0 || currentWeight == null || currentWeight.isNaN())
					currentWeight = DEFAULT_WEIGHT;  
								
				// how much more/less we need to get to max storage
				double desiredChange = resource.getMaxStorage() - resource.getAmount();
				
				// so we had less than max
				// add back the amount decayed
				if (desiredChange > 0) {
					double decayedAmount = resource.getMaxStorage() / 2 * (1 - resource.getDecayRate());
					desiredChange -= decayedAmount;
					
					// don't switch to decrease
					desiredChange = Math.max(desiredChange, 0);
				}
				
				double requiredChange = desiredChange;
				
				double lastYearsProduction = c.getUnitCount();
				if (lastYearsProduction == 0)
					lastYearsProduction = 1;
				

                requiredChange = requiredChange / lastYearsProduction;


				requiredChange = Math.max(-MAX_DECLINE, requiredChange);			
				requiredChange = Math.min(MAX_INCREASE, requiredChange);
                               
                 // trying to see if we can introduce a factor so that we produce
                // less the more we have over                
                if (requiredChange == -MAX_DECLINE && (desiredChange / lastYearsProduction <= -1)) {
                    double additionalChange = - 0.25 + 0.25 * resource.getMaxStorage() / resource.getAmount();
                    requiredChange = requiredChange + additionalChange;
                }

                if (requiredChange == MAX_INCREASE && (desiredChange / lastYearsProduction >= MAX_INCREASE + 1)) {
                    double additionalChange = + 1 - resource.getAmount() / resource.getMaxStorage();
                    requiredChange = requiredChange + additionalChange;
                }

                
				Double newVal = currentWeight * (1 + requiredChange);               
				
				weights.put(c, newVal);            
			}
		}
	}
}
