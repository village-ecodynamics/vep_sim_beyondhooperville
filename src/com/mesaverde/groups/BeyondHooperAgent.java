package com.mesaverde.groups;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.mesaverde.specialization.resources.Maize;
import com.mesaverde.specialization.resources.Resource;
import com.mesaverde.village.Agent;
import com.mesaverde.village.ImaginaryCell;
import com.mesaverde.village.Village;
import com.mesaverde.groups.BeyondGroup;
import com.mesaverde.exchange.GRNExchangeNetwork;
import com.mesaverde.groups.BeyondExchange;

public class BeyondHooperAgent extends Agent implements uchicago.src.sim.gui.Drawable {   
	protected BeyondHooperAgentModelSwarm mySwarm;
	protected BeyondFrustration beyondFrustration;
	protected BeyondExchange beyondExchange;
	private BeyondGroup group;
	private OrganizationPreference preference;
	protected int trading_protein_GRN;
	protected int trading_protein_BRN;
	public GRNExchangeNetwork grnNetwork;
	public BeyondExchange brnNetwork;

	// tracking benefit payoff
	private double benefitReceived;
	private double paidContributionOrTaxes;

	private double pgGameBenefit;
	private double pgGameLoss;

	public BeyondHooperAgent() {
		super();
		this.beyondFrustration = new BeyondFrustration();
		this.setPgGameBenefit(0);
		this.setPgGameLoss(0);
	}


	/** Returns the gain or loss received by this agent from playing the public goods game */
	public double calcPublicGameResult() {
		return benefitReceived - paidContributionOrTaxes;
	}

	/** If we can't pay the full contribution then we won't pay any at all.
	 * The reason is that we'll get punished either way.
	 * @return
	 */
	public Maize payContribution() {
		double requiredContribution = this.getMySwarm().getC_cost() * this.getMySwarm().getKg_day();

		double amount = 0;

		if (this.getPreference().isUnpunishedDefector())
			return null;

		if ((amount = requiredContribution) <= getMaizeStorage()) {
			setMaizeStorage((int) (getMaizeStorage() - amount));
			paidContributionOrTaxes += amount;
			pgGameLoss += amount;
			return new Maize((int) amount, Resource.EMPTY);			
		}

		return null;
	}

	/** We'll just subtract the amount of maize that we're punished for.
	 *  We'll still get the full benefit after this (so it works out to the same thing).
	 */
	public void punishDefection() {		
		double amount = this.getMySwarm().getS_sanction() * this.getMySwarm().getKg_day();

		preference.setPunishedDefector(true);
		setMaizeStorage((int) (getMaizeStorage() - amount));
		pgGameLoss += amount;
	}

	/** Returns the benefit for a group of a given size. */
	public double calcBenefit(int group_size) {
		// System.out.println("group_benefit_growth_rate: " + mySwarm.getGroup_benefit_growth_rate());
		return this.getMySwarm().getKg_day() *  this.getMySwarm().getB_benefit() * Math.pow(Math.E, (-(this.getMySwarm().getGroup_benefit_growth_rate()) / group_size));
	}

	/** We get punished if we pay anything less than the full amount.
	 * As such, we only pay any if we can pay it all.
	 * @param group_size
	 * @return
	 */
	public Maize payTax(int group_size) {	
		double requiredTax = getLeader().getPreference().getTaxRate() * calcBenefit(group_size);

		double amount = 0;

		if (preference.isUnpunishedTaxCheat())
			return null;

		if ((amount = requiredTax) <= getMaizeStorage()) {
			setMaizeStorage((int)(getMaizeStorage() - amount));
			paidContributionOrTaxes += amount;
			pgGameLoss += amount;
			return new Maize((int) amount, Resource.EMPTY);
		}

		return null;
	}

	/** We'll just subtract the amount of maize that we're punished for.
	 *  We'll still get the full benefit after this (so it works out to the same thing).
	 */
	public void punishTaxEvasion() {
		double amount = this.getMySwarm().getS_tax_sanction() * this.getMySwarm().getKg_day();

		preference.setPunishedTaxCheat(true);
		setMaizeStorage((int) (getMaizeStorage() - amount));
		pgGameLoss += amount;
	}

	@Override
	public void moveHouse(boolean force) {
		this.getBeyondFrustration().setHurt(false);
		this.getBeyondFrustration().getFrustrations().clear();

		//		BeyondHooperMaster hooper2 = this.getSwarm().getHooper2();
		// Generate the sorted list of cells as in Agent.java
		ArrayList<ImaginaryCell> moveCells = searchNeighborhoodAllDX(this.x, this.y, Village.MOVE_RAD);

		ImaginaryCell currentCell = null;
		for(ImaginaryCell c : moveCells){
			if(c.getX()==this.x && c.getY()==this.y){
				currentCell = c;
			}
		}

		//		System.out.println("Searching through moveCells: " + moveCells.size());

		ImaginaryCell moveCell = null;

		// Now, apply a series of tests to see if moving is legal, and accrue frustrations.
		// The first test checks whether the proposed cell is in another group's territory.
		// The second test checks whether a straight-line path from the agents current cell to 
		// the new cell passes through another groups territory.
		// The third test checks whether the resulting group geometry will overlap with another group's territory.
		// NONE OF THESE ARE ALLOWED!
		for(ImaginaryCell c : moveCells) {

			if(force && c==currentCell){
				continue;
			}

			// The current cell always trumps all equally weighted cells.
			if(!force && currentCell != null && currentCell.getPotentialEnergy() == c.getPotentialEnergy()){
				c = currentCell;
			}

			// Check if proposed cell is in another group's territory.
			BeyondGroup g = this.getMySwarm().getHooper2().isInGroupTerritory(c.getX(),c.getY());
			if((g!=null) && g!=this.getGroup()){
				this.getBeyondFrustration().addFrustration(c.getX(),c.getY(),g);
				continue;
			}

			// Check whether a straight-line path from the agents current cell to 
			// the new cell passes through another groups territory.
			//			System.out.println(this.getX() + "," + this.getY() + ":" + c.getX() + "," + c.getY());
			g = null;
			g = this.getMySwarm().getHooper2().pathCrossesGroupTerritory(this.getX(), this.getY(), c.getX(),c.getY());
			//			System.out.println("Territory: " + territory);
			if((g!=null) && g!=this.getGroup()){
				this.getBeyondFrustration().addFrustration(c.getX(),c.getY(),g);
				//				System.out.println("Path crosses territory!");
				continue;
			}

			// Check whether the new territory will overlap with any other group's territories
			g = null;
			g = this.getMySwarm().getHooper2().newTerritoryIntersectsGroupTerritory(this, c.getX(),c.getY());
			if((g!=null) && g!=this.getGroup()){
				this.getBeyondFrustration().addFrustration(c.getX(),c.getY(),g);
				//				System.out.println("Territories intersect!");
				continue;
			}

			// If it makes it through the tests, break out!
			//			System.out.println("Made it through the tests! Moving to: "+ c);
			//			System.out.println();
			moveCell = c;
			break;
		}

		// If staying in the same place, this frustration hurts!
		if(moveCell==null || (this.getX()==moveCell.getX() && this.getY()==moveCell.getY())){
			this.getBeyondFrustration().setHurt(true);
			return;
		}

		this.unPlotAll();
		cell[4].removeSettler(this);

		this.setXY(moveCell.getX(), moveCell.getY());

		state = 1;
		//		plot_need = (int) 2 + familyUnit.getKidCount() / 2; // This call to reset the plot need makes no sense.
		last_move = 0;

		this.getGroup().updateTerritory();
		return;
	}

	@Override
	public void death() {
		super.death();
		this.group.updateTerritory();
	}

	public BeyondGroup getGroup() {
		return group;
	}

	/** Just set our group to null, does nothing else (even
	 * remove us from previous group.
	 */
	void setToEmptyGroup() {
		group = null;
	}

	public void setGroup(BeyondGroup group) {
		if(group==null && this.group!=null){
			this.group.removeMember(this);
		}else{
			this.group = group;
			this.group.addMember(this);
		}
	}


	protected void evalState() {
		// state (possible values 0, 1, 2) is evaluated at end of year after all
		// other actions
		// called by step_assess during step_procure_conclude
		// determines if more or fewer plots are needed and/or whether to move
		// internal state variables should represent what happened in year
		// note that evalState updates protein_max_store but that protein
		// balance does not enter into state calculation!
		// state is also set in -moveHouse (to 1) and plot_need reset
		// plot_need initialized in as 2 + (familyUnit.getKidCount() / 2) in -createEnd
		// and can be changed here and can be decreased in -step_procure_spring
		// prior to planting if labor is limiting

		// The goal of this function is to evaluate how well the
		// agent is performing at its current location and to make
		// adjustments based on this performance. This is currently done by
		// looking at two variables of the agent.

		// EXP_cal_need creates a 2-year target for maize storage sufficient for
		// agent base cals taking into account spoilage
		// It is used if agent is in state 1 to see if agent
		// should plant another field
		// 1 year storage = current years usage (Total_cal *Village.MAIZE_PER)
		// in KG of corn (divide by MaizeVillage._KG_CAL)
		// Village.MAIZE_PER is prop of diet assumed to come from maize,
		// currently (7/30/08) set to .7 in Village.h

		// total calories discounted by Village.MAIZE_PER, and used throughout
		// the rest of this method
		int dis_total_cal = 0;
		dis_total_cal = (int) (total_cal * Village.MAIZE_PER);

		max_store = (int) ((1 / (1 - Village.MAIZE_STORAGE_DECR)) * 2
				* (dis_total_cal / Village.MAIZE_KG_CAL)); // total_cal is the sum of cals actually used/HH/yr
		protein_max_store = (int) 1.33 * p_need * getFamilySize() * 365 / 4; // 1 season's need taking into account spoilage



		/** Written by Stefani Crabtree; here if the agent is the current leader of the group they can have augmented storage. 
		 * The constant "leader_STORAGE" is what augments it, and that is changed in Constants.java*/


		//			System.out.println("We are augmenting our storage");
		if (this == this.getGroup().getLeader()) {
			max_store = (int) (((1 / (1 - Village.MAIZE_STORAGE_DECR)) * 2
					* (dis_total_cal / Village.MAIZE_KG_CAL)) * this.getMySwarm().getLeader_storage()); // total_cal is the sum of cals actually used/HH/yr
			protein_max_store = (int) (1.33 * p_need * getFamilySize() * 365)* this.getMySwarm().getLeader_storage() / 4;
		}
		else {
			max_store = (int) ((1 / (1 - Village.MAIZE_STORAGE_DECR)) * 2
					* (dis_total_cal / Village.MAIZE_KG_CAL)); 
			protein_max_store = (int) 1.33 * p_need * getFamilySize() * 365 / 4;
		}


		//	Stefani writing here

		// update expectations of cal need, est. by this year's usage plus
		// estimate of next year
		// this is for 2 years
		int old_EXP_cal_need = 0;
		old_EXP_cal_need = EXP_cal_need;

		EXP_cal_need = dis_total_cal + old_EXP_cal_need / 2;

		if (Village.DEBUG) {
			if (tag == Village.TAG) {
				System.out
				.printf(
						"Village.DEBUG -" +
								"State: agent %d new max_store is %d kg\n",
								tag, max_store);
				System.out
				.printf(
						"Village.DEBUG -evalState: agent %d new EXP_cal_need is %d, storage contains %d Cal\n",
						tag, EXP_cal_need,
						(getMaizeStorage() * Village.MAIZE_KG_CAL));
			}
		}

		// calculate internal state
		state = 2;
		// the first state test determines if the current maize storage
		// is greater than the expected caloric needs for 1 year
		// Maize_Per taken out of equation because already accounted for in
		// EXP_cal_need
		if (getMaizeStorage() * Village.MAIZE_KG_CAL < EXP_cal_need) {
			storage1++; // static, initialized to zero at top of Agent.m
			state = 1; // due to storage shortfall
		}

		// JAC 4/05
		// The second test determines if the Avg calories produced in
		// one year is greater than the expected caloric need for 1 year
		// Maize_Per taken out of equation because already accounted for in
		// EXP_cal_need
		// AVG_cal_Prod previously updated in -step_procure_fall to average of
		// last two years' production: so now just contains 1 year

		if ((int) (AVG_cal_prod * Village.MAIZE_PER) < (int) (EXP_cal_need / 2)) // note
			// that
			// Village
			// .
			// AVG_Ccal_prod
			// is
			// not
			// discounted
			// by
			// Village
			// .
			// MAIZE_PER
			// but
			// EXP_cal_need
			// is
			// !
		{
			production1++; // static, initialized to zero at top of Agent.m		
			state = 1; // due to production not keeping pace with expected need
		}

		if (state == 1) {
			// if state = 1 then agents will plant 1 additional plot if
			// they are not labor bound and they have less than 2 years
			// of food in storage. If a plot cannot be planted then they
			// will move

			// if current storage is less than 2 years worth
			// Do not count maize that may have been donated to us this year
			if (getMaizeStorage() - maize_imported < max_store) {
				int k = 0;
				k = familyUnit.getCountAtOrAboveAge(8);

				// if there are more workers than plots planted
				int ad_plots = mySwarm.getAd_plots();
				if (tot_plots <= k + ad_plots) {
					// then plant 1 more
					plot_need++;
				} else {
					// else move
					move_trigger = 1;
					moveHouse(false);
				}
			}
		} else if (state == 0) {

			if (exchange_count >= 5) {
				move_trigger = 2;
			} else {
				move_trigger = 1; // init: move is triggered by environment.
			}
			moveHouse(false);
		}

		if (Village.DEBUG) {
			if (tag == Village.TAG) {
				System.out.printf(
						"Village.DEBUG -evalState: agent %d new state is %d\n",
						tag, state);
			}
		}

		if (getMaizeStorage() > max_store) {
			update_state();
		}

		//		// defector check
		//		if (Village.ENABLE_DEFECTING_AGENT
		//				&& DefectingAgentFlag == 0
		//				&& (DefectingAgentCount < Village.MAX_DEFECTORS || Village.DEFECTOR_PROPAGATE_FAMILY != 0)) {
		//			if (coop_state == Village.PHILANTHROPIST
		//					|| coop_state == Village.SATISFIED
		//					|| coop_state == Village.FULL) {
		//				DefectingAgentCount++;
		//				DefectingAgentFlag = 1;
		//			}
		//		}

		if (Village.DEBUG) {
			System.out.printf("Village.DEBUG -evalState end\n");
		}
		return;
	}

	/**
	 * @return the leader
	 */
	public BeyondHooperAgent getLeader() {
		if (group != null) {
			return group.getLeader();
		}
		return null;
	}

	/** Receive benefit from Public Goods game */
	public void receiveBenefit(double benefitPayback) {
		setMaizeStorage((int) (getMaizeStorage() + benefitPayback));
		benefitReceived = benefitPayback;
		pgGameBenefit += benefitPayback;
	}


	/* scheduled after all yearly agent actions,
    // and after randomizing agentList, removing
    // agents, and diffusing deer */
	public void printagentstats(boolean heading) 
	{int run_number = mySwarm.getFileID();
	String combined = "output/agent_stats_run_" + run_number + ".csv";

	if (heading) {
		try {
			FileWriter out = new FileWriter(new File(combined));
			out.write("Year," +
					"agent," +
					"group," +
					"mother_household," +
					"father_household," +
					"x," +
					"y," +
					"family_size_begin," +
					"family_size_end," +
					"births," +
					"deaths," +
					"arriving_newlyweds," +
					"departing_newlyweds," +
					"tot_hrs," +
					"H," + 
					"ALLC," + 
					"UnpunishedDefector," + 
					"MM," + 
					"ALLT," + 
					"UnpunishedTaxCheat," + 
					"L," + 
					"leadershipTaxRate," + 
					"PgGameLoss," + 
					"PgGameBenefit," + 
					"Deer_hunted," +
					"Rabbit_hunted," +
					"Hare_hunted," +
					"Turkey_kept," +
					"Deer_distance_mean," +
					"state\n");
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	try {
		FileWriter out = new FileWriter(new File(combined),true);
		out.write(this.mySwarm.getWorldTime() + "," +
				this.getTag() + "," +
				this.getGroup().getID() + "," +
				this.getParentHHTagA() + "," +
				this.getParentHHTagB() + "," +
				this.getX() + "," +
				this.getY() + "," +
				this.getBeginSize() + "," +
				this.getFamilySize() + "," +
				this.getNumBirths() + "," +
				this.getNumDeaths() + "," +
				this.getArrivingNewlyweds() + "," +
				this.getDepartingNewlyweds() + "," +
				this.getTot_hrs() + "," +
				Boolean.toString(this.getPreference().isH()) + "," + 
				Boolean.toString(this.getPreference().isALLC()) + "," + 
				Boolean.toString(this.getPreference().isUnpunishedDefector()) + "," + 
				Boolean.toString(this.getPreference().isMM()) + "," + 
				Boolean.toString(this.getPreference().isALLT()) + "," + 
				Boolean.toString(this.getPreference().isUnpunishedTaxCheat()) + "," + 
				Boolean.toString(this.getPreference().isL()) + "," + 
				this.getPreference().getTaxRate() + "," + 
				this.getPgGameLoss() + "," +
				this.getPgGameBenefit() + "," +
				this.getDeer_hunted() + "," +
				this.getRabbit_hunted()  + "," +
				this.getHare_hunted() + "," +
				this.getTurkey_kept() + "," +
				((this.getDeer_hunted() > 0) ? (Math.max(this.getDeer_distance(), 0.5)/5)/(this.getDeer_hunted()) : -999)  + "," +
				this.getState() + "\n");
		out.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	}

	public BeyondExchange getBeyondExchange() {
		return beyondExchange;
	}

	public OrganizationPreference getPreference() {
		return preference;
	}

	public void setPreference(OrganizationPreference preference) {
		this.preference = preference;
	}

	public BeyondFrustration getBeyondFrustration() {
		return beyondFrustration;
	}

	public void setBeyondFrustration(BeyondFrustration beyondFrustration) {
		this.beyondFrustration = beyondFrustration;
	}

	public double getPgGameBenefit() {
		return pgGameBenefit;
	}

	public void setPgGameBenefit(double pgGameBenefit) {
		this.pgGameBenefit = pgGameBenefit;
	}

	public double getPgGameLoss() {
		return pgGameLoss;
	}

	public void setPgGameLoss(double pgGameLoss) {
		this.pgGameLoss = pgGameLoss;
	}

	public double getBenefitReceived() {
		return benefitReceived;
	}

	public void setBenefitReceived(double benefitReceived) {
		this.benefitReceived = benefitReceived;
	}

	public double getPaidContributionOrTaxes() {
		return paidContributionOrTaxes;
	}

	public void setPaidContributionOrTaxes(double paidContributionOrTaxes) {
		this.paidContributionOrTaxes = paidContributionOrTaxes;
	}

	public void setMySwarm(BeyondHooperAgentModelSwarm s) {
		mySwarm = s;
		return;
	}
	
	public BeyondHooperAgentModelSwarm getMySwarm() {
		return mySwarm;
	}
}
