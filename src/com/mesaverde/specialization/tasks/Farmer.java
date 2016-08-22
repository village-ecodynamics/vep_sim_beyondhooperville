package com.mesaverde.specialization.tasks;

import com.mesaverde.specialization.Constraints;
import com.mesaverde.specialization.Season;
import com.mesaverde.specialization.SpecializedAgent;
import com.mesaverde.specialization.resources.Maize;
import com.mesaverde.specialization.resources.Resource;
import com.mesaverde.village.Cell;
import com.mesaverde.village.Village;

/** NOTE: Farmers are still limited by MAX_PLOTS, which is currently 10 
 *  NOTE: Don't need to do an addResource, because harvesting in the fall automatically adds it to the agents total maize */
public class Farmer extends Task {
	public static final int CLOSE_CELL = 0;
	public static final int FAR_CELL = 1;
	private double cSpringCost, cSummerCost, cFallCost, fSpringCost, fSummerCost, fFallCost;

	public Farmer(SpecializedAgent agent) {
		super(agent);
	}

	/** calculates the expected cost of a farm for the spring depending on plot position
	 * 
	 * @param cell_position - close cell or far cell.
	 * @return
	 */
	private double calculateSpringCost(int cell_position) {
		double close = 14 * 8 * Village.WORK_CAL_MAN; // hoeing
		close += 17 * 8 * Village.WORK_CAL_MAN;  // men planting

		if (cell_position == CLOSE_CELL) {
			close += agent.calcTravelCal(0, 2, 30);
			cSpringCost = close;
			return close;
		} 

		// far cell
		double far = agent.calcTravelCal(1, 2, 30);  // 30 monitoring trips for kids
		far += agent.calcTravelCal(1, 0, 14); // travel cost for planting far away

		fSpringCost = far + close;
		return (far + close);
	}

	/** calculates the expected cost of a farm for the summer depending on plot position
	 * 
	 * @param cell_position - close cell or far cell.
	 * @return
	 */
	private double calculateSummerCost(int cell_position) {
		double close = 45 * 4 * Village.WORK_CAL_WOM; // women weeding

		if (cell_position == CLOSE_CELL) {
			cSummerCost = close;
			return close;
		}

		// far cell
		double far = agent.calcTravelCal(1, 2, 90);  // 90 agent.monitoring trips for kids
		far += agent.calcTravelCal(1, 1, 45);  // 45 weeding trips for women

		fSummerCost = far + close;
		return far + close;
	}

	/** calculates the expected cost of a farm for the summer depending on plot position
	 * 
	 * @param cell_position - close cell or far cell.
	 * @param maize_potential - the maize potential for the cell we're checking
	 * @return
	 */
	private double calculateFallCost(int cell_position, int maize_potential) {
		double close = 15 * 4 * Village.WORK_CAL_WOM;  // 15 weeding trips for women
		double kidTravel = agent.calcTravelCal(1, 1, 45); // 45 agent.monitoring trips for kids
		close += kidTravel;
		double far = 0;



		// far cell
		if (cell_position == FAR_CELL) {
			far = kidTravel;  // double the agent.monitoring trips if far
			far += agent.calcTravelCal(1, 1, 15);  // 15 weeding trips for women travel
		}

		// now for the harvesting		
		double harvest_adjustment = agent.getSwarm().getHarvestAdjust();

		// MaizePotential is in kg per ha, so multiplying by 4 yields
		// kg/cell &
		// dividing by Village.PLOTS yields kg/1-ac plot (when
		// Village.PLOTS=10)
		int cell_harvest = maize_potential * 4 / Village.PLOTS;
		cell_harvest /= Village.FALLOW_FACTOR * harvest_adjustment;
		double harvest_cals = (cell_harvest / 25 * Village.WORK_CAL_MAN * 2);

		if (cell_position == CLOSE_CELL) {
			cFallCost = close + harvest_cals;
			return close + harvest_cals;
		}



		// if we are FAR
		fFallCost = far + close + harvest_cals;
		return far + close + harvest_cals;
	}

	private double getTotalPlotCost(int cell_position, int maize_potential) {
		return calculateSpringCost(cell_position) +
		calculateSummerCost(cell_position) +
		calculateFallCost(cell_position, maize_potential);	
	}


	/** TODO: this needs to be reworked later so that everything doesn't rely on the farmer class to get executed */
	@Override	
	public int performTask(Season season, Constraints cons, boolean onlyNeeds) {

		int work_cal = 0;
		if (season == Season.SPRING) {
			reset(); // reset our stats at the start of the year only
			if (onlyNeeds)
				return procureSpringNeeds();	
			else
				return procureSpringConstraints(cons);
		}

		/* If we were able to plant in the spring, that's because we assumed we could make it all the way through
		 * So now we can just use the behaviour already defined in Agent (even though this makes more sense to bring
		 * those methods in here).
		 */
		if (season == Season.SUMMER) {
			int cals = agent.Ag_Cost;
			agent.summerWork();

			if (!onlyNeeds)
				cons.increaseCalories(
						- (agent.Ag_Cost - cals));
		}
		else if (season == Season.FALL){
			int cals = agent.Ag_Cost;
			agent.fallWork();

			if (!onlyNeeds)
				cons.increaseCalories(-(agent.Ag_Cost - cals));	

			this.unitCount = agent.act_yield;
			this.totalCost = agent.Ag_Cost;
		}
		// do nothing in winter

		// TODO: again assuming time is related to men, should revisit later
		cons.setAvailableTime(cons.getAvailableCalories() / Village.WORK_CAL_MAN);
		return work_cal;
	}

	/** Searches our neighborhood for plots in which we can plant.  This version is different
	 * from the one in Agent.
	 * For one, it takes an integer array parameter in which it stores the found location.
	 * Second, it returns false if it doesn't find a location */
	public boolean searchNeighborhoodDX(int a, int b, int radius, int caller, int[] location)
	{
		int k, max, dx, dy;
		boolean found = false;

		k = max = 0;
		for (dy = -radius; dy < radius; dy++) {
			for (dx = -radius; dx < radius; dx++) {
				k = agent.evalCellX(dx, dy, max);
				if (k != 0) {
					max = k;
					a = dx;
					b = dy;
					found = true;
				}
			}
		}

		location[0] = a;
		location[1] = b;

		return found;
	}


	/** plants as many plots as we think we can manage.  Still restricted by the max we've established (9) */
	private int procureSpringConstraints(Constraints cons) {
		// first, see how much we can put in our home cell
		double cals = cons.getAvailableCalories();		
		double originalCals = cals; // so we can know how much we used total
		// keep track of how many calories the agent has used

		// unplant all that we have currently, so we're starting from scratch
		agent.unPlotAll();

		int calSoFar = agent.Ag_Cost;		
		int numHomePlotsWeWantToPlant = (int) (cals / getTotalPlotCost(CLOSE_CELL, agent.cell[4].getMaizePotential()));

		// we're still limited by the maximum allowed
		// TODO: Note: Allowing agents MAX_PLOTS amount of home plots, as well as MAX_PLOTS amount of away plots
		numHomePlotsWeWantToPlant = Math.min(numHomePlotsWeWantToPlant, Village.MAX_PLOTS);
		int plot_need = numHomePlotsWeWantToPlant;

		int notPlanted = agent.plantPl(plot_need, 4);
		agent.H_Plots = plot_need - notPlanted;
		agent.A_Plots = 0;

		double close = 17 * 8 * agent.H_Plots * Village.WORK_CAL_MAN;  // men planting and hoeing
		agent.planting += close / Village.WORK_CAL_MAN;
		int travelCal = agent.calcTravelCal(0, 2, 30) * agent.H_Plots;
		close += travelCal;  // monitoring
		agent.monitoring += travelCal / Village.WORK_CAL_KID;

		agent.Ag_Cost += close;

		// update our available calories
		cals -= (agent.Ag_Cost - calSoFar);
		calSoFar = agent.Ag_Cost;

		// now we have to plant the rest somewhere else
		boolean canPlant = true;
		int caller = 0;
		while (canPlant) {
			// find somewhere to plant
			int dx = 0, dy = 0;
			int[] res = new int[2];
			boolean spotFound = searchNeighborhoodDX(dx, dy, 1, caller, res);

			// if we don't find a location to plant in, then doesn't matter how many calories we have available
			// cause we're done.
			if (!spotFound) {
				canPlant = false;
				break;
			}
			dx = res[0];
			dy = res[1];

			dx++;
			dy++;

			if (dy * 3 + dx > 8 || dy * 3 + dx < 0) {
				System.err.printf(
						"Village.ERROR: agent %d plot ranged dx %d dy %d\n",
						agent.getTag(), dx, dy);
				System.exit(-1);
			}

			// see how many we can afford to plant here
			Cell c = agent.getCellAt(dx, dy);
            
			int numWeCanPlant = (int) (cals / getTotalPlotCost(FAR_CELL, c.getMaizePotential()));
			// DC: not limited to MAX_PLOTS plots
			//numWeCanPlant = Math.min(numWeCanPlant, Village.MAX_PLOTS - agent.A_Plots);				

			notPlanted = agent.plantPl(numWeCanPlant, (dy * 3 + dx));
			int numPlanted = numWeCanPlant - notPlanted;
			agent.A_Plots += numPlanted;

			close = 17 * 8 * numPlanted * Village.WORK_CAL_MAN;  // men planting and hoeing	
			agent.planting += close / Village.WORK_CAL_MAN;
			travelCal = agent.calcTravelCal(1, 2, 30) * numPlanted;
			close += travelCal;  // monitoring
			agent.monitoring += travelCal / Village.WORK_CAL_KID;
			close += agent.calcTravelCal(1, 0, 14) * numPlanted;  // travel

			agent.Ag_Cost += close;

			// update our available calories
			cals -= (agent.Ag_Cost - calSoFar);
			calSoFar = agent.Ag_Cost;

			if (notPlanted == 0)
				canPlant = false;

			if (notPlanted < 0) {
				System.out.println("error, we planted too many: " + (-notPlanted));
				System.exit(1);
			}
		}

		int work_cals = (int) (originalCals - cals);

		cons.setAvailableCalories(cals);
		cons.setAvailableTime(cons.getAvailableCalories() / Village.WORK_CAL_MAN);		


		return work_cals;

	}

	/** Plant only as many plots as we need */
	private int procureSpringNeeds() {
		int dx;
		int dy;
		int i;
		int k = (int) agent.season_fam;
		int caller = 0;
		int work_cal = 0;
		if (agent.tot_plots > k + Village.AD_PLOTS) {
			agent.plot_need = -1;
		} else if (agent.tot_plots == k + Village.AD_PLOTS && agent.plot_need > 0) {
			agent.plot_need = 0; // can't add plots; labor limited
		} else if (agent.tot_plots + agent.plot_need > k + Village.AD_PLOTS) {
			i = k + Village.AD_PLOTS - agent.tot_plots; // maximum allowable plots -
			// current plots (=
			// expandable capacity)
			agent.plot_need = i;
		}

		if (Village.DEBUG && agent.getTag() == Village.TAG) {
			System.out.printf(
					"Village.DEBUG -spring bp: agent %d has agent.plot_need %d, agent.tot_plots %d,%d workers, & %d kg maize in storage\n",
					agent.getTag(), agent.plot_need, agent.tot_plots, k, agent.getMaizeStorage());
		}

		// plant until done, start with homestead 4 hectares (home cell)
		// passing instance var, might want function for later uses not with
		// agent.plot_need
		if (agent.plot_need > 0) {
			agent.plot_need = agent.plantPl(agent.plot_need, 4);
		} else if (agent.plot_need < 0 && agent.tot_plots == agent.farm_pl[4]) {
			agent.plot_need = agent.plantPl(agent.plot_need, 4);
		}

		if (Village.DEBUG && agent.getTag() == Village.TAG) {
			System.out.printf(
					"Village.DEBUG -spring ap home cell: agent %d agent.plot_need %d, total_plots %d\n",
					agent.getTag(), agent.plot_need, agent.tot_plots);
		}
		k = 0;
		while (agent.plot_need != 0 && k < 7) { // no room in home cell
			dx = dy = 0;
			if (agent.plot_need > 0) {
				int[] res = agent.searchNeighborhoodDX(dx, dy, 1);
				dx = res[0];
				dy = res[1];
			} else {
				int[] res = agent.unplotDX(dx, dy);
				dx = res[0];
				dy = res[1];
			}

			dx++;
			dy++;

			if (dy * 3 + dx > 8 || dy * 3 + dx < 0) {
				System.err.printf(
						"Village.ERROR: agent %d plot ranged dx %d dy %d\n",
						agent.getTag(), dx, dy);
				System.exit(-1);
			}

			agent.plot_need = agent.plantPl(agent.plot_need, (dy * 3 + dx));
			k++; // safety
		}

		if (Village.DEBUG && agent.getTag() == Village.TAG) {
			System.out
			.printf(
					"Village.DEBUG -spring ap all plots: agent %d agent.plot_need %d, agent.tot_plots %d\n",
					agent.getTag(), agent.plot_need, agent.tot_plots);
		}

		agent.A_Plots = 0;
		agent.H_Plots = 0;

		for (i = 0; i < 9; i++) {
			if (i != 4) {
				agent.A_Plots += agent.farm_pl[i];
			} else {
				agent.H_Plots += agent.farm_pl[i];
			}

			// need 17 days to plant & hoe 1-ac field. 14 days to clear and hoe
			// + 3 days to plant (Forde p. 390)
			// one of our grid cells = 40,000 sq m = 4 ha = 9.88 acres
			// so at 9 plots/cell, each plot is about 1 acre
			// 17 days * 8 Hours * Number of Plots * calories per hour
			// next costs are for all plots, home or away

			work_cal += 17 * agent.farm_pl[i] * 8 * Village.WORK_CAL_MAN; // converts
			// hours .
			// calories
			// and
			// cumulates
			agent.planting += 17 * 8 * agent.farm_pl[i]; // (in hours); initialized in
			// -stepProcureInit

			// This code adds a travel cost for farming in fields away from the
			// home cell
			// 30 days of field agent.monitoring to spring, assume kid is available to
			// do it
			// agent.calcTravelCal takes trips, calculates distance, and returns tcal
			// which is calories modulated by the "gender" (or age class) doing
			// the traveling

			if (i == 4 && agent.farm_pl[i] > 0) {
				double travelCal = agent.calcTravelCal(0, 2, (30 * agent.farm_pl[i]));
				work_cal += travelCal; // agent.calcTravelCal
				// takes the
				// int 0 and
				// turns it
				// into the
				// double
				// 0.5

				agent.monitoring += travelCal / Village.WORK_CAL_KID; // agent.calcTravelCal takes the int 0
				// and turns it into the double
				// 0.5
				// agent.monitoring initialized to 0 in -step_procure_init
				// dividing the cal returned by agent.calcTravelCal by
				// Village.WORK_CAL_KID yields hours
			}

			if (i != 4 && agent.farm_pl[i] > 0) {
				double travelCal = agent.calcTravelCal(1, 2, (30 * agent.farm_pl[i]));
				work_cal += travelCal; // agent.calcTravelCal
				// takes the
				// int 0 and
				// turns it
				// into the
				// double
				// 0.5
				agent.monitoring += travelCal / Village.WORK_CAL_KID;
				// dividing the cal returned by agent.calcTravelCal by
				// Village.WORK_CAL_MAN yields hours
			}
		}

		agent.Ag_Cost += work_cal;
		return work_cal;
	}

	@Override
	public Class<? extends Resource> getResourceType() {
		return Maize.class;
	}
}
