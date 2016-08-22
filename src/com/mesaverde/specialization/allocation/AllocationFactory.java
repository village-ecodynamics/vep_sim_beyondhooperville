package com.mesaverde.specialization.allocation;

import com.mesaverde.specialization.SpecializedAgent;

public class AllocationFactory {	

	/** This method returns the allocation strategy you wish to use. 
	 */
	public static AllocationStrategy getAllocationStrategy(SpecializedAgent agent) {				
		 return new SocialEconomicStrategy(agent);		
		// return new TrendMaxStorageStrategy(agent);
	}

}
