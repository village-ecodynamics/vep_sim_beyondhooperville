package com.mesaverde.domestication;

import com.mesaverde.village.Agent;
import com.mesaverde.village.Village;

/**
 * The base economic strategy for turkey domestication.<p>
 * 
 * This class calculates the number of turkey needed by a household, and forces the 
 * household to track the number of turkeys they keep.<p>
 * 
 * The {@link #execute() execute} method controls these actions.<p>
 * 
 * Domestication itself is toggled in the {@link com.mesaverde.village.Village Village} class.<p>
 * 
 * @author R. Kyle Bocinsky <bocinsky@wsu.edu>
 * @version $Date: 2010/11/22 $
 * @since 1.0
 */

public class StrictEconomicModel extends DomesticationStrategy {
	public StrictEconomicModel(Agent agent) {
		super(agent);
	}

	/** 
	 * A method for controlling turkey domestication. This methos is fed {@code hh_protein_need} 
	 * as a parameter, calculates the number of turkeys needed to fulfull that need, and 
	 * compares that with the maximum number of turkeys allows so as not to deplete maize stores 
	 * beyond a critical threshold (the amount of maize utilized in the past year). The agent 
	 * will choose to domesticate the lesser of these two turkey amounts.<p>
	 * 
	 * @see com.mesaverde.domestication.DomesticationStrategy#execute(long)
	 * @return {@code turkeyCal} calories spent on turkey keeping.
	 */
	@Override
	public int execute(long hh_protein_need) {

		boolean domestication = agent.getMySwarm().isDomestication();
		double turkey_maize_per = agent.getMySwarm().getTurkey_maize_per();
		
		/**
		 * Ensure that this is the strategy that ought to be taken.
		 */
		if (DomesticationParameters.DOMESTICATION_STRATEGY != DomesticationParameters.STRICT_ECONOMIC_MODEL || hh_protein_need <= 0 || !domestication) {
			return 0;
		}
//		System.out.printf("Domestication Strategy is the Strict Economic Model \n");

		int numTurkey = 0;
		int turkeyNeeded = 0;
		int maxTurkey = 0;
		int turkeyCal = 0;
		
		/**
		 * Reset annual number of turkey kept by the household.
		 */
		agent.setTurkey_kept(0);
		
		/**
		 * Calculate household turkey need.
		 */
		turkeyNeeded = (int) (hh_protein_need/DomesticationParameters.PROTEIN_PER_TURKEY);
//		System.out.printf("turkeyNeeded: %d\n", turkeyNeeded);
		
		/**
		 * Calculate maximum turkey able to be kept by household.
		 */
		maxTurkey = (int) (((agent.getMaizeStorage() * Village.MAIZE_KG_CAL) - (agent.getEXP_cal_need() * Village.MAIZE_PER)) / (DomesticationParameters.BASE_CAL_TURKEY * turkey_maize_per));
//		System.out.printf("maxTurkey: %d\n", maxTurkey);
		
		/**
		 * Choose lower of the two values.
		 */
		if (maxTurkey > 0) {
			if (turkeyNeeded < maxTurkey) {
				numTurkey = turkeyNeeded;
			} else {
				numTurkey = maxTurkey;
			}
		}

		/**
		 * Report the number of calories spent domesticating.
		 */
		turkeyCal += (int)(numTurkey * DomesticationParameters.BASE_CAL_TURKEY * turkey_maize_per);
		
		C_domestication += (int)(numTurkey * DomesticationParameters.BASE_CAL_TURKEY * turkey_maize_per);
		
		/**
		 * Increment the number of turkeys kept, so we know.
		 */
		agent.setTurkey_kept(agent.getTurkey_kept() + numTurkey);

		/**
		 * Increment protein storage by turkeys kept. Agents are charged a years-worth of turkey feeding.
		 */
		agent.setCurrentProteinStorage(agent.getCurrentProteinStorage() + (numTurkey * DomesticationParameters.PROTEIN_PER_TURKEY));

		return turkeyCal;
	}	
}