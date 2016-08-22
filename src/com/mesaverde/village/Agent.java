package com.mesaverde.village;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import com.mesaverde.hunting.*;
import com.mesaverde.domestication.*;
import com.mesaverde.exchange.*;

import uchicago.src.sim.gui.SimGraphics;

import com.mesaverde.model.AgentModelSwarm;
import com.mesaverde.model.ObserverAgentModel;

import java.util.HashSet;

public class Agent {
	// defines household behavior in world
	protected int time;
	protected int tag; // agent id tag
	protected int x;
	protected int y;
	protected int worldX; // how big world is for X
	protected int worldY; // and Y
	protected VillageSpace world; // pointer to agent's world 1.2
	protected AgentModelSwarm mySwarm;
	protected Cell[] cell = new Cell[9]; // cells near agent position
	protected static int counter = 0;
	// fecundity control rule: max of 6 children per female and
	// min two years between each child
	int child_per_female; // number of children born per female
	int last_year_child_born; // when the last time a child was born
	protected Color myColor; // display colors
	// internal household attributes
	protected int agent_time; // global time
	protected int form_date; // date of formation
	protected int family_tag; // call it clan
	protected double season_fam; // number in family during summer
	protected int last_move; // number of years since last move
	// these variable used for tracking # of hours spent at these activities
	protected double planting;
	protected double weeding, harvesting;
	protected double monitoring;
	protected double plotwork, C_fuel, C_water;
	protected double hrs;
	protected double tot_hrs, nonhunt_hrs, field_hrs;
	protected double deer_time;
	protected double deer_return_rate;
	protected double deer_distance;
	protected double hare_time;
	protected double hare_return_rate;
	protected double rabbit_time;
	protected double rabbit_return_rate;
	protected double hunting_return_rate;
	// these variables track when births and deaths occur
	protected int numBirths; // number of births per agent, per year
	protected int numDeaths; // number of deaths per agent, per year
	// and these variables track when individuals marry into or leave the household
	protected int arrivingNewlyweds; // number of births per agent, per year
	protected int departingNewlyweds; // number of deaths per agent, per year
	protected int beginSize; // size at start of year
	// food and planting info
	protected int maize_storage; // food stock
	protected int[] farm_pl = new int[9]; // # plots for farming
	protected int tot_plots; // how many planted
	protected int plot_need; // plots desired left
	protected int max_store; // max limit storage
	// protein resources
	protected int deer_protein_storage; // Deer, hare, rabbit
	protected int hare_protein_storage;
	protected int rabbit_protein_storage;
	protected int current_protein_storage; // total of deer/hare/rabbit
	protected int protein_tot_cells; // how many hunted
	protected int protein_max_store; // max limit storage
	protected double expected_protein_yield_per_cell;
	protected int num_cells_hunted;
	protected double total_protein_yield_all_cells;
	protected int hunting_radius;
	protected int p_need;
	protected int protein_deficient; // switch that determines if agent is lacking
	// protein
	protected int years_deficient;
	protected int deer_hunted;
	protected int hare_hunted;
	protected int rabbit_hunted;
	protected int turkey_kept;
	protected int i_turkey_kept;

	protected int hunting_protein;
	protected int domestication_protein;
	protected int trading_protein_GRN;
	protected int trading_protein_BRN;
	protected int total_protein;

	protected double hunting_protein_proportion;
	protected double domestication_protein_proportion;
	protected double trading_protein_GRN_proportion;
	protected double trading_protein_BRN_proportion;

	public double getHunting_protein_costs_proportion() {
		return hunting_protein_costs_proportion;
	}

	public void setHunting_protein_costs_proportion(
			double hunting_protein_costs_proportion) {
		this.hunting_protein_costs_proportion = hunting_protein_costs_proportion;
	}

	public double getDomestication_protein_costs_proportion() {
		return domestication_protein_costs_proportion;
	}

	public void setDomestication_protein_costs_proportion(
			double domestication_protein_costs_proportion) {
		this.domestication_protein_costs_proportion = domestication_protein_costs_proportion;
	}

	protected double hunting_protein_costs_proportion;
	protected double domestication_protein_costs_proportion;

	protected int H_Plots;
	protected int A_Plots;
	// expectations of farming productivity and Cal needs
	protected int state;
	protected int EXP_cal_need;
	protected int AVG_cal_prod;
	protected int EXP_yield;
	protected int act_yield; // not used?
	protected int[][] past_yield = new int[9][3]; // record of local yields
	protected int actual_cal; // spent in each season
	protected int total_cal; // yearly figure, accumulates actual_cal from each

	// season
	protected int Cal_Produced; // number of calories produced each year
	protected int Water_Cost; // number of calories spent each year for water
	protected int Fuel_Cost; // number of calories spent each year for fuel
	protected int Food_Cost; // number of calories spent each year eating food
	protected int Ag_Cost; // number of calories spent each year for agriculture
	protected int Protein_Cost;
	protected int FW_need; // firewood need of household
	protected int FWsearchradius;
	protected int fwsr; // firewood search radius for use in functions
	protected int wtsr; // water for functions
	protected int W_need; // water need of household
	protected int Wsearchradius;
	protected int Hsearchradius;
	protected int FWHappy;
	protected int FWout;
	protected int OutsideFlag; // Flag indicating if the agent left the region
	// (0=no, 1=yes)
	protected int OutsideCellX; // location where the agent left
	protected int OutsideCellY;
	protected int move_trigger; // Why is the agent moving?
	// 1=Environmental Trigger
	// The agent is looking for a better cell while not ruling out the fact that
	// the current cell
	// could still sustain productivity, just trying to be greedy. (default
	// initial behavior)
	// 2=Social Trigger
	// The agent's cell could not sustain it, it borrowed before, and gave up
	// due to harsh environment.
	// it decides to rely on the social network to move closer to its selected
	// successful kin
	protected int exchange_count; // keep track of past exchanges, if the agent
	// exchanged in the past then its move_trigger
	// could now be
	// based on social network preference over environment
	protected int maize_coop_count; // keep track of seasonal coop allowance
	protected int protein_coop_count;
	protected int totalMaizeExchanged;
	protected int successful_exchange;
	protected int exchange_requests; // counts not kg
	protected int maize_requests; // in KG
	protected int maize_exchanged; // in KG
	protected int maize_wasted; // Amount of maize exchanged and the agent dies
	// anyway at the end of the year
	protected int maize_given;
	protected int donater;

	// tracking protein exchange

	protected int protein_given;
	protected int pro_donater;

	protected int maize_imported;
	protected int protein_imported;
	protected int remarriage_count; // track number of times an agent got
	// remarried, allow
	// Village.MAX_REMARRIAGE_COUNT only then
	// remove it.
	protected int remarriage_attempts; // track number of times and agent attempted

	// needed for updates
	protected int ParentHHTagA; // network links
	protected int ParentHHTagB;
	protected int[] ChildHHTag = new int[Village.MAX_CHILD_LINKS];
	protected int ChildHHCount;
	protected int[] RelativeHHTag = new int[Village.MAX_RELATIVE_LINKS];
	protected int RelativeHHCount;
	protected int nextpick; // used for round-robin in philanthropic model
	protected int coop_state; // 1=critical; 2=hungry; 3=satisfied; 4=phil; 5=full
	protected int protein_coop_state; // 1=critical; 2=hungry; 3=satisfied; 4=phil;
	// 5=full
	protected int last_ration; // last amount of food eaten by family, used to
	// define hungry
	protected int last_protein_ration; // last amount of food eaten by family, used
	// to define hungry
	protected double[] WhoToAskProb = new double[2 + Village.MAX_CHILD_LINKS
	                                          + Village.MAX_RELATIVE_LINKS]; // ParentA/B, a child, or a relative
	protected int[] MemoryInteraction_ID = new int[Village.MAX_MEM_INTERACTION]; // used
	// to
	// store
	// the
	// recent
	// successful
	// exchange
	// partners
	// (
	// by
	// tag
	// id
	// )
	protected int[] MemoryInteraction_Index = new int[Village.MAX_MEM_INTERACTION]; // the
	// kin
	// index
	// in
	// the
	// strategy
	protected int[] MemoryInteraction_X = new int[Village.MAX_MEM_INTERACTION];
	protected int[] MemoryInteraction_Y = new int[Village.MAX_MEM_INTERACTION];
	protected int MemoryInteractionCount; // store the actual number of mem cells
	// used

	/* Economic Network Properties */
	protected int DefectingAgentFlag; // 0=normal, 1=always defects

	protected int hunt_test_penalty;
	protected HuntingStrategy huntingStrategy;
	protected DomesticationStrategy domesticationStrategy;
	protected GRNExchangeNetwork grnNetwork;
	protected BRNExchangeNetwork brnNetwork;
	protected DonationExchangeNetwork donationNetwork;
	protected FamilyUnit familyUnit;  // keeps track of individuals within the household

	// birth and death rates per age group
	public static double[] birth_per = new double[] { .098, .264, .264, .214,
		.148, .062, .012 };
	public static double[] death_per = new double[] { .2330, .1400, .1000,
		.0735, .1305, .1336, .1367, .1400, .1432, .1466, .1500, .1536,
		.2513, .3270, 1.0 };
	public static int tot_pop = 0;
	public static int pop_id = 1;
	public static double oldagentpop = 0;
	public static double agentpop = 154;

	// used for agent defection in cooperation
	public static int DefectingAgentCount = 0;
	public static int DefectingAgentTurnover = 0;
	public static int storage1 = 0;
	public static int production1 = 0;
	public static int fdeaths = 0;
	public static int totalage = 0;
	public static int fkids = 0;
	public static int totalkids = 0;
	public static int birthcalls = 0;
	public static int parentp = 0;
	public static int kidp = 0;
	public static int birthp = 0;
	public static int successfulbirths = 0;
	public static int m2 = 0;
	public static int sm2 = 0;
	public static long g_countof_move_radius = 0;
	public static double g_average_move_radius = 0;
	public static int term_error = 0;

	/* used for agent tags, DC: This should start at 1 to ensure no agent
	 * starts with a tag of 0.
	 */
	private static int TAGS = 1;

	public static void resetStatics() {
		birth_per = new double[]{.098, .264, .264, .214,
				.148, .062, .012};
		death_per = new double[]{.2330, .1400, .1000,
				.0735, .1305, .1336, .1367, .1400, .1432, .1466, .1500, .1536,
				.2513, .3270, 1.0};
		tot_pop = 0;
		pop_id = 1;
		oldagentpop = 0;
		agentpop = 154;

		// used for agent defection in cooperation
		DefectingAgentCount = 0;
		DefectingAgentTurnover = 0;
		storage1 = 0;
		production1 = 0;
		fdeaths = 0;
		totalage = 0;
		fkids = 0;
		totalkids = 0;
		birthcalls = 0;
		parentp = 0;
		kidp = 0;
		birthp = 0;
		successfulbirths = 0;
		m2 = 0;
		sm2 = 0;
		g_countof_move_radius = 0;
		g_average_move_radius = 0;
		term_error = 0;
		TAGS = 1;
	}

	public Agent() {
		int i;

		tag = getNextTag();

		// set up exchange networks
		setupNetworks();

		domesticationStrategy = new StrictEconomicModel(this);

		deer_protein_storage = 0;
		hare_protein_storage = 0;
		rabbit_protein_storage = 0;
		setCurrentProteinStorage(0);
		protein_max_store = 0;

		// initialize years deficient for protein penalty
		years_deficient = 0;

		expected_protein_yield_per_cell = 0;
		num_cells_hunted = 0;
		total_protein_yield_all_cells = 0;

		// add the final touches and check for agent creation
		setAgent_time(0);
		last_move = 0;

		child_per_female = 0;
		last_year_child_born = 0; // just to start

		// track remarriage constraints
		remarriage_count = 0;
		remarriage_attempts = 0;

		g_countof_move_radius = 0;
		g_average_move_radius = 0;

		coop_state = Village.SATISFIED;
		protein_coop_state = Village.SATISFIED;
		MemoryInteractionCount = 0; // no one to remember yet

		nextpick = 1;

		familyUnit = new FamilyUnit(this);

		for (i = 0; i < 2 + Village.MAX_CHILD_LINKS
				+ Village.MAX_RELATIVE_LINKS; i++) {
			WhoToAskProb[i] = 0.1;
		}

		// the default drawable color
		setColor(Color.red);
	}

	/**
	 * @param work_cal
	 */
	public void actualCal(int work_cal) {
		// Increases actual_cal to match other net caloric needs for agents.
		// Village.MAIZE_PER gets reapplied in eatMaize and at the end of each
		// year in evalState.
		int real_cal = 0;
		real_cal = (int) (work_cal / Village.MAIZE_PER);
		actual_cal += real_cal;
	}

	protected Agent addHousehold(Individual wife, Individual husband) {				
		Agent newHousehold;

		newHousehold = this.getMySwarm().addAgent(this.x, this.y, wife, husband);

		// create a link from parent to child

		if (ChildHHCount < Village.MAX_CHILD_LINKS) {
			ChildHHTag[ChildHHCount] = newHousehold.getTag();
			ChildHHCount++;
		}

		// link child tags and relative tags here call an update, verify to make
		// sure they are not dead, rank as needed.
		return newHousehold;
	}

	void birth(int m_age) // mother's age
	{
		birthcalls++;
		if (child_per_female > 1) {
			m2++;
		}

		double tmp = 1.0;
		int ind;

		// make sure both husband and wife are present before having babies
		if (!familyUnit.hasHusband() || !familyUnit.hasWife()) {
			parentp++;
			return;
		}

		// birth control
		if (child_per_female > 20) {
			kidp++;
			return;
		}

		// checks state, alters percentage from that
		float state_good = this.getMySwarm().getState_good();
		float state_bad = -state_good;

		if (state<1)
			tmp += state_bad;
		if (state>1) 
			tmp += state_good;

		// protein penalty

		int p_penalty = this.getMySwarm().getProteinPenalty();
		if (p_penalty == 1) {
			if (protein_deficient != 0 && state > 1) // initialized to zero in
				// -step_procure_protein but reset to 1
				// there if protein inadequate
			{
				tmp = 1.0;
			}
		}

		if (14 < m_age && m_age < 50 && familyUnit.getKidCount() < 8) {
			ind = m_age / 5 - 3;
			// increase/decrease birth percentage based on state
			if (Village.uniformDblRand(0.0, 1.0) < birth_per[ind] * tmp) // defined
				// as
				// static
				// double
				// at
				// top
				// of
				// Agent
				// .
				// m
			{
				// playing
				int ind2 = Village.uniformIntRand(0, 1);

				familyUnit.addNewChild(0, ind2); // enable gender 0=fem, 1=male
				numBirths++;
				last_year_child_born = this.getMySwarm().getWorldTime();
				child_per_female++;
				successfulbirths++;
				if (child_per_female > 2) {
					sm2++;
				}
			}
		}
	}

	// annual fuelwood need consumed in summer and then only if
	// Village.BURN_WOOD == 1
	protected int burnWood() {
		int need, dx, dy, dw, bw_cals;
		float trips, cellwood;
		Cell c;
		trips = 0;
		bw_cals = 0;
		dx = dy = dw = 0;
		need = FW_need * getFamilySize(); // FW_need set to Village.WOOD_NEED in
		// -createEnd
		FWsearchradius = 0;
		FWHappy = 0;
		FWout = 0;
		while (need > 0) {
			int wood_type = 0;
			dx = dy = 0;
			int[] res = searchNeighborhoodFireDX(dx, dy, dw, 0); // dw acts as a
			// switch
			// between
			// standing
			// crop and
			// dead wood

			dx = res[0];
			dy = res[1];
			dw = res[2];

			if (FWsearchradius < 100) {
				// if dw = 1 then wood taken from standing crop, if
				// dw = 0 wood taken from deadwood
				c = (Cell) world.getObjectAt(Village.wrapX(dx, x, worldX), Village.wrapY(dy, y, worldY));
				if (dw == 1) {
					wood_type = Village.SC_COLLECT;
					cellwood = c.getFWPotential(0);
				} else {
					wood_type = Village.DW_COLLECT;
					cellwood = c.getDWPotential(0);
				}

				if (cellwood >= need) {
					cellwood = cellwood - need;
					trips = need / Village.CARRY_CAPACITY;
					bw_cals += collectWood(wood_type, trips);
					need = 0;
					if (dw == 1) {
						c.setFWPotential(cellwood, 0);
					} else {
						c.setDWPotential(cellwood, 0);
					}
				} else {
					need -= (int) cellwood;
					trips = cellwood / Village.CARRY_CAPACITY;
					bw_cals += collectWood(wood_type, trips);
					if (dw == 1) {
						c.setFWPotential(0, 0);
					} else {
						c.setDWPotential(0, 0);
					}
				}

			} else {
				wood_type = Village.SC_COLLECT;
				trips = need / Village.CARRY_CAPACITY;
				bw_cals += collectWood(wood_type, trips);
				need = 0;
			}
		}

		if (FWsearchradius > 24) {
			FWHappy = 100;
		}
		if (FWsearchradius > 49) {
			FWout = 100;
		}
		fwsr = FWsearchradius;
		return bw_cals;
	}

	protected int calcExpCalUse() {
		// expected family Cal need for 2 years, but with no work
		int cals = 0;

		// seasonal estimates, so 2 years is 8 seasons + other stuff
		cals += 9 * (familyUnit.getWifesAge() > 1 ? Village.BASE_CAL_WOM : 0); // dead moms don't
		// eat
		cals += 9 * (familyUnit.getHusbandsAge() > 1 ? Village.BASE_CAL_MAN : 0); // nor do dead
		// dads
		cals += 9 * familyUnit.getKidCount() * Village.BASE_CAL_KID;
		return cals;
	}

	public int calcTravelCal(int radius, int gend, double trips) {
		double distance;
		int t_cals = 0;
		double TT_cals = 0;
		int hrcost = 0;

		int travel_speed = Village.TRAVEL_SPEED; // in cells/hour

		distance = Math.max(radius, 0.5); // in cells after all, they still have
		// to get it even if they are on top
		// of it
		if (gend == 0) {
			hrcost = Village.WORK_CAL_MAN;
		}
		if (gend == 1) {
			hrcost = Village.WORK_CAL_WOM;
		}
		if (gend == 2) {
			hrcost = Village.WORK_CAL_KID;
		}
		if (gend == 4) {
			hrcost = 1500;
		}

		TT_cals = (double) (2 * distance / travel_speed * trips * hrcost);
		t_cals = (int) TT_cals;
		//traveling += 2 * distance / travel_speed * trips;

		return t_cals;
	}

	/**
	 * 
	 */
	public void calculateEatCal() {
		int eat_cal = 0;
		eat_cal = familyUnit.getKidCount() * Village.BASE_CAL_KID;
		if (familyUnit.getWifesAge() > 0) {
			eat_cal += Village.BASE_CAL_WOM;
		}
		if (familyUnit.getHusbandsAge() > 0) {
			eat_cal += Village.BASE_CAL_MAN;
		}
		Food_Cost += eat_cal;
		actual_cal = eat_cal;
	}

	/**
	 * 
	 */
	public void calculateNumberOfWorkers() {
		// this code is used for determining the average number of workers
		// a family has over the course of one year
		int amountOfWorkers = familyUnit.getCountAtOrAboveAge(8); // winter
		season_fam += amountOfWorkers;
	}

	/**
	 * 
	 * @param wood_type
	 * @param trips
	 * @return
	 */
	protected int collectWood(int wood_type, float trips) {
		int cals = 0;
		cals += calcTravelCal(FWsearchradius, 1, trips);
		C_fuel += (double) cals / Village.WORK_CAL_WOM;
		cals += trips * wood_type * Village.WORK_CAL_WOM;
		C_fuel += trips * wood_type;
		return cals;
	}

	public void createEnd() {
		int i;
		int trade = this.getMySwarm().getEconomy();

		hunting_radius = this.getMySwarm().getHuntingRadius();
		p_need = this.getMySwarm().getProteinNeed();

		// initial planting habits and cal needs
		state = 1;

		setHuntingStrategy();

		setMaizeStorage(Village.MAIZE_ENDOWMENT * getFamilySize());
		setCurrentProteinStorage((int) (Village.PROTEIN_ENDOWMENT * p_need * getFamilySize() * 365));


		setMaizeMaxStore(Village.MAIZE_INITIAL_MAX * getFamilySize());
		setProteinMaxStore((int) (Village.PROTEIN_ENDOWMENT * p_need * getFamilySize() * 365));

		// update state for coop
		update_state();

		EXP_cal_need = calcExpCalUse(); // returns 2+ yrs base cal needed given
		// current family size, with no work,
		// not discounted by Village.MAIZE_PER

		FW_need = Village.WOOD_NEED;

		// initial water need for households
		W_need = Village.WATER_NEED;

		EXP_yield = 250; // derived from Van West
		// to better reflect lower average production in VEP I dataplanes

		AVG_cal_prod = (int) (EXP_cal_need * Village.MAIZE_PER); // now we
		// discount
		// it by
		// Village
		// .MAIZE_PER
		// after creation, AVG_cal_prod is calculated in -Fall, and used
		// in -evalState
		plot_need = 2 + familyUnit.getKidCount() / 2;
		tot_plots = 0;

		for (i = 0; i < 9; i++) {
			farm_pl[i] = 0;
		}
		// farm_pl[i] stores n of plots used by each household in 9-cell
		// neighborhood

		// set past expectations of yield to overall avg expectation from data,
		// adjusted downwards to help agents who initially land outside known
		// world
		for (i = 0; i < 9; i++) {
			past_yield[i][0] = past_yield[i][1] = past_yield[i][2] = 250;
		}

		if (world == null) {
			System.err.printf("Village.ERROR: agent %d no world initialized\n",
					tag);
			System.exit(-1);
		}

		// time is always 0 here because it hasn't run through step_procure_init yet
		if (this.getMySwarm().getWorldTime() == 0) // agent_time == 0
		{
			ParentHHTagA = -1; // no link to parents initially (social links are
			// empty)
			ParentHHTagB = -1; // A is for parent household of index 0, B for
			// index 1

			for (i = 0; i < Village.MAX_CHILD_LINKS; i++) {
				ChildHHTag[i] = -1; // no links to children's households yet
			}

			ChildHHCount = 0;

			for (i = 0; i < Village.MAX_RELATIVE_LINKS; i++) {
				RelativeHHTag[i] = -1; // no links to relatives' households yet
			}

			RelativeHHCount = 0;
		}

		move_trigger = 1; // init: move is triggered by environment.
		exchange_count = 0; // init: did not exchange yet

		// outer Region (enable agent to exit region)
		OutsideFlag = 0; // Flag indicating if the agent left the region (0=no,
		// 1=yes)
		OutsideCellX = 0; // location where the agent left
		OutsideCellY = 0;

		// Balanced Reciprocity

		if (trade >= 4) // if balanced reciprocity enabled (economics)
		{
			brnNetwork.initialize();
		}

		if (Village.ENABLE_DEFECTING_AGENT) {
			DefectingAgentFlag = 0;
		}
		return;
	}

	protected void death() {
		if (state != -1) {
			// defector check
			//			if (Village.ENABLE_DEFECTING_AGENT && DefectingAgentFlag != 0) {
			//				DefectingAgentCount--;
			//				DefectingAgentTurnover++;
			//				DefectingAgentFlag = 0;
			//			}

			state = -1;
			this.unPlotAll();
			cell[4].removeSettler(this);
			cell[4].addDeadAgent();
			numDeaths += getFamilySize();
			tot_pop = tot_pop - getFamilySize();

			for (Individual ind : familyUnit.getAllIndividuals()) {
				if (ind.eligibleRecord != null) {
					Eligible.removeFromEligibleList(ind.eligibleRecord,
							mySwarm);
				}
			}			
		} else {
			System.err.println("Village.ERROR: agent " + this + " died twice");
			//System.exit(-1);
		}

		// clear exchange networks
		if (brnNetwork != null)
			brnNetwork.clearInformation();

		return;
	}

	protected void decreaseCurrentProteinStorage(int donateProtein) {
		current_protein_storage -= donateProtein;
	}

	public void draw(SimGraphics r) { // 1.2
		r.drawFastRect(myColor);
	}

	// This function has been updated to work similarly to the burn wood
	// function it now takes and subtracts water from cells
	// annual water needs entirely consumed in summer, and then only if
	// Village.DRINK_WATER == 1
	protected int drinkWater() {
		int need, dx, dy, W_cals, w_carry_capacity;
		Cell c;
		double trips;
		int w_type;
		float l_max;

		trips = 0;
		W_cals = 0;
		dx = dy = 0;
		need = W_need * getFamilySize();

		if (this.getMySwarm().isDomestication() && this.getMySwarm().isTurkey_water()) {
			//			System.out.printf("turkey_kept: %d\n", turkey_kept);
			need += (int)(this.turkey_kept * DomesticationParameters.TURKEY_WATER_NEED * 365);
		}

		w_carry_capacity = Village.CARRY_CAPACITY - 4; // water carrying
		// capacity = total
		// carrying capacity -
		// vessel weight
		// (4kg)(Lightfoot,
		// 1994)

		while (need > 0) {
			int[] res = searchNeighborhoodwaterDX(dx, dy);
			dx = res[0];
			dy = res[1];

			c = (Cell) world.getObjectAt(Village.wrapX(dx, x, worldX), Village
					.wrapY(dy, y, worldY));
			w_type = c.getWaterType();
			l_max = c.getWater();

			// if the water type found is a spring(4) then water taken from it
			// must be reported back to the cell
			if (w_type == 4) {
				if (l_max >= need) {
					l_max = l_max - need;
					trips = need / w_carry_capacity;
					double travel_cost = calcTravelCal(Wsearchradius, 1, trips);
					W_cals += travel_cost;

					C_water += travel_cost / Village.WORK_CAL_WOM;
					wtsr = Wsearchradius;

					// setWaterUse tracks total water taken from the cell while
					// set water sets the amount of water left in the cell
					c.setWaterUse(need);
					c.setWater(l_max);
					need = 0;
				} else {
					need = (int) (need - l_max);
					trips = l_max / w_carry_capacity;
					double travel_cost = calcTravelCal(Wsearchradius, 1, trips);
					W_cals += travel_cost;

					C_water += travel_cost / Village.WORK_CAL_WOM;
					c.setWaterUse((int) l_max);
					c.setWater(0);
				}
			} else {
				trips = need / w_carry_capacity;

				double travel_cost = calcTravelCal(Wsearchradius, 1, trips);
				W_cals += travel_cost;

				C_water += travel_cost / Village.WORK_CAL_WOM;
				wtsr = Wsearchradius;

				c.setWaterUse(need);
				need = 0;
			}
		}
		return W_cals;
	}

	// **** household utilities ****
	protected void eatMaize(int cals_spent) {
		int trade = this.getMySwarm().getEconomy();
		maize_coop_count = 0;
		// eat food, corrected for percent of Cals from maize
		setMaizeStorage(getMaizeStorage() - ((int) (Village.MAIZE_PER * cals_spent / (double) Village.MAIZE_KG_CAL)));

		// store the ration amount the family ate
		last_ration = (int) (Village.MAIZE_PER * cals_spent / (double) Village.MAIZE_KG_CAL);

		// update state for coop
		update_state();

		// Request food
		if (trade == 1 || trade == 3 || trade >= 4) {
			// call in kin
			if ((Village.TRIGGER_REQUEST_MAIZE() & getCoopState()) != 0) {
				int init_maize_storage;
				if (trade >= 3 && maize_coop_count < Village.COOP_ATTEMPTS) {
					int received = 0;
					init_maize_storage = getMaizeStorage();

					received = grnNetwork.requestMaize((int) (Village.MAIZE_PER
							* cals_spent / (double) Village.MAIZE_KG_CAL)
							- getMaizeStorage()); // cooprequest returns local
					// variable maize_retrieved
					setMaizeStorage(getMaizeStorage() + received);
					maize_imported += received;
					update_state();
				}
				if (trade >= 4 && maize_coop_count < Village.COOP_ATTEMPTS) {
					int received = 0;
					init_maize_storage = getMaizeStorage();

					received = brnNetwork.requestMaize((int) (Village.MAIZE_PER
							* cals_spent / (double) Village.MAIZE_KG_CAL)
							- init_maize_storage); // RequestTrade returns local
					// variable maize_retrieved
					setMaizeStorage(init_maize_storage + received);
					maize_imported += received;
					update_state();
				}
			}
		}

		if (getMaizeStorage() < 5) {
			// DC: we're also going to recall debts when we need it to survive
			if (brnNetwork != null && last_ration > getMaizeStorage()) {
				brnNetwork.update();
				brnNetwork.callInDebts();
			}

			if (Village.DEBUG) {
				System.out.printf("agent %d , maize storage = %d\n", tag,
						getMaizeStorage());
			}
		}
		if (getMaizeStorage() < 0) {
			setMaizeStorage(-1);
			update_state();
			death();

			if (Village.DEBUG) {
				System.out
				.printf(
						"Village.DEBUG -eatMaize: agent %d starved, spent %d, last harv %d kg (%d Cal), state %d\n",
						tag, cals_spent, act_yield, act_yield
						* Village.MAIZE_KG_CAL, state);
			}
		}

		return;
	}

	/** ensure that protein and maize storage do not exceed the maximum 
	 * DC: This is not currently enforced. */
	public void enforceResourceMaximums() {
		/*
		setMaizeStorage(Math.min(getMaizeStorage(), getMaizeMaxStore()));
		setCurrentProteinStorage(Math.min(getCurrentProteinStorage(), getProteinMaxStore()));
		 */
	}

	//    /** DC: The original evalCellX has multiple function exits, so I'm just
	//     * going to nest it and then change the return value in here.
	//     * @param dx
	//     * @param dy
	//     * @param max
	//     * @param caller
	//     * @return
	//     */
	//    public int evalCellX(int dx, int dy, int max, int caller)
	//    {
	//               
	//        int val = evalCell(dx, dy, max, caller);
	//
	//        if (this.getMySwarm().getTrade_boosted_move()) {
	//        	
	//            /* Now we're going to use ideas about how to boost the value of
	//             * these cells, such as presence of trading partners.
	//             */
	//
	//            Cell c = (Cell) world.getObjectAt(Village.wrapX(dx, x, worldX), Village.wrapY(dy, y, worldY));
	//
	//            // for now we're going to keep this recalculating
	//            val = boostByProximityOfWealthyPartnersAndKin(val, c);
	//        }
	//
	//        return val;
	//    }


	public boolean waterNearby(int x, int y){
		for (int i = x - Village.H2O_RAD; i < x + Village.H2O_RAD; i++) {
			for (int j = y - Village.H2O_RAD; j < y + Village.H2O_RAD; j++) {
				if (((Cell) world.getObjectAt(Village.wrapX(i, x, worldX), Village.wrapY(j, y, worldY))).getWaterType() >= Village.H2O_TYPE) {

					return true;

				}
			}
		}
		return false;
	}

	//	// -evalCell provides some notion of weighting crowded land
	//	// also checks to see about local water availability if
	//	// Village.NEEDS_H2O (defined in Village.java) is 1 and this cell is
	//	// being evaluated for a residential move (in which case caller=1)
	//	// max (accumulated in -searchNeighborhood) stores the highest
	//	// value found for (possibly degraded) MaizePotential
	//	public int evalCell(int dx, int dy, int max, int caller) 
	//	{
	//		int pot, full, water, i, j, h2o;
	//		Cell c;
	//		if (caller == 0 || !Village.NEEDS_H2O){
	//			h2o = 0; // don't worry about water
	//		} else {
	//			h2o = 1; // worry about water
	//		}
	//		
	//		c = (Cell) world.getObjectAt(Village.wrapX(dx, x, worldX), Village.wrapY(dy, y, worldY));
	//		pot = c.getMaizePotential();
	//		full = c.getFarmPl(); // someday we'll add a check for n of houses too
	//		water = c.getWaterType();
	//		
	//		if (full >= Village.PLOTS - 2) {
	//			return 0; // don't waste our time
	//
	//		}
	//		if (h2o == 0) { // don't care about water
	//
	//
	//				return pot; // looks good (pot>=max) & who cares about water?
	//
	//		
	//		} else { // now h2o == 1 (we worry about water)
	//
	//			if (water >= Village.H2O_TYPE) {
	//
	//				return pot; // production looks good & waters right here too!
	//							// Take it
	//
	//			} else { // better shop around, bad pot or bad water here
	//
	//				for (i = dx - Village.H2O_RAD; i < dx + Village.H2O_RAD; i++) {
	//					for (j = dy - Village.H2O_RAD; j < dy + Village.H2O_RAD; j++) {
	//						if (((Cell) world.getObjectAt(Village.wrapX(i, x,
	//								worldX), Village.wrapY(j, y, worldY)))
	//								.getWaterType() >= Village.H2O_TYPE) {
	//
	//							return pot; // looks good and waters close enough!
	//
	//						}
	//					}
	//				}
	//			}
	//			return 0; // try somewhere else
	//
	//		}
	//	}

	// evaluates protein potential in cells
	public int evalDProteinCellX(int dx, int dy, int img) {
		int pot;
		Cell c;

		c = (Cell) world.getObjectAt(Village.wrapX(dx, x, worldX), Village
				.wrapY(dy, y, worldY));
		pot = (int) c.getDProteinPotential(img);
		return pot;

	}

	// Updated: JAC 12/04
	public int evalDWinCellX(int dx, int dy, int img) {
		int pot;
		Cell c;

		c = (Cell) world.getObjectAt(Village.wrapX(dx, x, worldX), Village
				.wrapY(dy, y, worldY));
		pot = (int) c.getDWPotential(img);
		return pot;

	}

	// -evalFWinCell looks for firewood in a cell and if available returns the
	// amount
	// Updated: JAC 12/04
	public int evalFWinCellX(int dx, int dy, int img) {
		int pot;
		Cell c;

		c = (Cell) world.getObjectAt(Village.wrapX(dx, x, worldX), Village
				.wrapY(dy, y, worldY));
		pot = (int) c.getFWPotential(img);
		return pot;

	}

	public int evalLProteinCellX(int dx, int dy, int img) {
		int pot;
		Cell c;

		c = (Cell) world.getObjectAt(Village.wrapX(dx, x, worldX), Village
				.wrapY(dy, y, worldY));
		pot = (int) c.getLProteinPotential(img);
		return pot;

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
				int ad_plots = this.getMySwarm().getAd_plots();
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

	// This function has changed so only cells with water amounts are sent to
	// the search routine
	int evalWaterinCellX(int dx, int dy) {
		int pot;
		float amount;
		Cell c;

		c = (Cell) world.getObjectAt(Village.wrapX(dx, x, worldX), Village
				.wrapY(dy, y, worldY));
		amount = c.getWater();
		if (amount > 0) {
			pot = c.getWaterType();
			return pot;
		} else {
			return 0;
		}

	}

	// tracking births and deaths

	public int exportMaize(int maize_requested) {
		int maize_needed = Math.min(maize_requested,
				Village.MAX_EXCHANGE_AMOUNT);
		int maize_toexport = 0;

		if (maize_needed <= 0) {
			return 0;
		}

		// If we have enough, more than we need, then give some (assume self has
		// higher priority for the food)
		if (getMaizeStorage() > 0) {
			if (getMaizeStorage() >= maize_needed) {
				setMaizeStorage(getMaizeStorage() - maize_needed);

				update_state();
				maize_toexport = maize_needed;
			} else // partial export
			{
				maize_toexport = getMaizeStorage();
				setMaizeStorage(0);

				update_state();
				maize_needed -= getMaizeStorage();
			}

		}

		return maize_toexport;
	}

	public int exportProtein(int protein_requested) {
		if (Village.COOP_DEBUG) {
			System.out.printf(
					"Agent %d has %d g protein requested and has %d g\n", tag,
					protein_requested, getCurrentProteinStorage());
		}
		int protein_needed = Math.min(protein_requested,
				Village.MAX_PROTEIN_EXCHANGE_AMOUNT);

		int protein_toexport = 0;

		if (protein_needed <= 0
				|| (getProteinCoopState() & (Village.HUNGRY | Village.CRITICAL)) != 0) {
			return 0;
		}

		// If we have enough, more than we need, then give some (assume self has
		// higher priority for the food)
		if (getCurrentProteinStorage() > 0) {
			if (getCurrentProteinStorage() >= protein_needed) {
				setCurrentProteinStorage(getCurrentProteinStorage() - protein_needed);

				update_state();
				protein_toexport = protein_needed;
			} else // partial export
			{
				protein_toexport = getCurrentProteinStorage();
				setCurrentProteinStorage(0);

				update_state();
				protein_needed -= getCurrentProteinStorage();
			}

		}

		return protein_toexport;
	}

	public int getAplots() {
		return A_Plots;
	}

	// tracking births and deaths
	public int getNumBirths() {
		return numBirths;
	}

	public int getBRN_PExchange() {
		return brnNetwork.getProteinExchanged();
	}

	public int getBRN_PNumExchange() {
		return brnNetwork.getProteinExchangeRequests();
	}

	public int getBRN_PPaidBack() { // protein
		return brnNetwork.getProteinPaidBack();
	}

	public int getBRN_PPayback() { // maize
		return brnNetwork.getProteinPaybacks();
	}

	public int getBRN_PRequest() {
		return brnNetwork.getProteinRequests();
	}

	public int getBRN_PSucExchange() { // agents have value of 1 in case of
		// successful exchnage
		return brnNetwork.getProteinSuccessfullyExchanged();
	}

	public int getBRN_PWasted() {
		return brnNetwork.getProteinWasted();
	}

	public int getBRNExchange() {
		return brnNetwork.getMaizeExchanged();
	}

	public int getBRNMPaidBack() { // maize
		return brnNetwork.getMaizePaidBack();
	}

	public int getBRNMPayback() { // maize
		return brnNetwork.getMaizePaybacks();
	}

	public int getBRNNumExchange() { // maize
		return brnNetwork.getExchangeRequests();
	}

	public int getBRNRequest() {
		return brnNetwork.getMaizeRequests();
	}

	public int getBRNSucExchange() { // maize
		return brnNetwork.getSuccessfulExchanges();
	}

	public int getBRNWasted() {
		return brnNetwork.getMaizeWasted();
	}

	public Cell getCell() {
		return this.cell[4];
	}

	/* gets the Cell at location (dx, dy). As this is a TorusWorld, the agent will wrap */
	public Cell getCellAt(int i) {
		return this.cell[i];
	}
	
	/* gets the Cell at location (dx, dy). As this is a TorusWorld, the agent will wrap */
	public Cell getCellAt(int dx, int dy) {
		return (Cell) getWorld().getObjectAt(Village.wrapX(dx, getX(), getWorldX()), Village
				.wrapY(dy, getY(), getWorldY()));
	}

	// return the coop_state
	public int getCoopState() {
		return coop_state;
	}

	public int getCurrentProteinStorage() {
		return current_protein_storage;
	}

	public int getNumDeaths() {
		return numDeaths;
	}

	public double getDeerRR() {
		return deer_return_rate;
	}

	public int getDonater() { // maize
		return donater;
	}

	// **** get agent states ****
	public int getFamilySize() {
		return familyUnit.getFamilySize();
	}

	public int getBeginSize() {
		return beginSize;
	}

	public void setBeginSize(int beginSize) {
		this.beginSize = beginSize;
	}

	public int getFamilyTag() {
		return family_tag;
	}

	public FamilyUnit getFamilyUnit() {
		return familyUnit;
	}

	public int getArrivingNewlyweds() {
		return arrivingNewlyweds;
	}

	public void setArrivingNewlyweds(int arrivingNewlyweds) {
		this.arrivingNewlyweds = arrivingNewlyweds;
	}

	public int getDepartingNewlyweds() {
		return departingNewlyweds;
	}

	public void setDepartingNewlyweds(int departingNewlyweds) {
		this.departingNewlyweds = departingNewlyweds;
	}

	public int getFarmCal() {
		return Ag_Cost;
	}

	public int getFarmPlAt(int i) {
		return farm_pl[i];
	}

	public int getFoodCal() {
		return Food_Cost;
	}

	public int getFormationDate() {
		return form_date;
	}

	public int getFuelCal() {
		return Fuel_Cost;
	}

	public int getFWhappy() {
		return FWHappy;
	}

	public int getFWout() {
		return FWout;
	}

	public int getGatheredCal() {
		return Cal_Produced;
	}

	public int getGRN_PExchange() {
		return grnNetwork.getProteinExchanged();
	}

	public int getGRN_PNumExchange() {
		return grnNetwork.getProteinExchangeRequests();
	}

	// JAC 5/5 tracking protein trade networks
	public int getGRN_PRequest() {
		return grnNetwork.getProteinRequests();
	}

	public int getGRN_PSucExchange() {
		return grnNetwork.getProteinSuccessfullyExchanged();
	}

	public int getGRN_PWasted() {
		return grnNetwork.getProteinWasted();
	}

	public double getHareRR() {

		return hare_return_rate;

	}

	public double getHunting_return_rate() {
		return hunting_return_rate;
	}

	public void setHunting_return_rate(double hunting_return_rate) {
		this.hunting_return_rate = hunting_return_rate;
	}

	public int getHunting_protein() {
		return hunting_protein;
	}

	public void setHunting_protein(int hunting_protein) {
		this.hunting_protein = hunting_protein;
	}

	public int getDomestication_protein() {
		return domestication_protein;
	}

	public void setDomestication_protein(int domestication_protein) {
		this.domestication_protein = domestication_protein;
	}

	public int getTrading_protein_GRN() {
		return trading_protein_GRN;
	}

	public void setTrading_protein_GRN(int trading_protein_GRN) {
		this.trading_protein_GRN = trading_protein_GRN;
	}

	public int getTrading_protein_BRN() {
		return trading_protein_BRN;
	}

	public void setTrading_protein_BRN(int trading_protein_BRN) {
		this.trading_protein_BRN = trading_protein_BRN;
	}

	public int getTotal_protein() {
		return total_protein;
	}

	public void setTotal_protein(int total_protein) {
		this.total_protein = total_protein;
	}

	public double getHunting_protein_proportion() {
		return hunting_protein_proportion;
	}

	public void setHunting_protein_proportion(double hunting_protein_proportion) {
		this.hunting_protein_proportion = hunting_protein_proportion;
	}

	public double getDomestication_protein_proportion() {
		return domestication_protein_proportion;
	}

	public void setDomestication_protein_proportion(
			double domestication_protein_proportion) {
		this.domestication_protein_proportion = domestication_protein_proportion;
	}

	public double getTrading_protein_GRN_proportion() {
		return trading_protein_GRN_proportion;
	}

	public void setTrading_protein_GRN_proportion(
			double trading_protein_GRN_proportion) {
		this.trading_protein_GRN_proportion = trading_protein_GRN_proportion;
	}

	public double getTrading_protein_BRN_proportion() {
		return trading_protein_BRN_proportion;
	}

	public void setTrading_protein_BRN_proportion(
			double trading_protein_BRN_proportion) {
		this.trading_protein_BRN_proportion = trading_protein_BRN_proportion;
	}

	public int getHplots() {
		return H_Plots;
	}

	public double getHrs() {
		return hrs;
	}

	public int getHuntCal() {
		return Protein_Cost;
	}

	public int getI_turkey_kept() {
		return i_turkey_kept;
	}

	public void setI_turkey_kept(int i_turkey_kept) {
		this.i_turkey_kept = i_turkey_kept;
	}

	public int getMaizeMaxStore() {
		return max_store;
	}

	public int getMaizeStorage() {
		return maize_storage;
	}

	public int getMaxStore() {
		return max_store;
	}

	public int getMExchange() {
		return maize_exchanged;
	}

	public int getMGiven() {
		return maize_given;
	}

	public int getMothersAge() {
		return familyUnit.getWifesAge();
	}

	public int getMRequest() {
		return maize_requests;
	}

	public int getMWasted() {
		return maize_wasted;
	}

	public int getNumExchange() { // maize
		return exchange_requests;
	}

	public int getNumKids() {
		return familyUnit.getKidCount();
	}

	public int getNumPlots() {
		return tot_plots;
	}

	public int getPGiven() {
		return protein_given;
	}

	public int getProDonater() {
		return pro_donater;
	}

	// return the protein coop_state
	public int getProteinCoopState() {
		return protein_coop_state;
	}

	// A couple new methods to allow us to graph protein storage
	public int getProteinMaxStore() {
		return protein_max_store;
	}

	public int getProteinStorage() {
		return getCurrentProteinStorage();
	}

	public double getRabbitRR() {

		return rabbit_return_rate;
	}

	public int getRdistance() { // fuelwood distance
		return fwsr / 5;
	}

	public int getSpentCal() {
		int Spent;
		Spent = Food_Cost + Ag_Cost + Water_Cost + Protein_Cost + Fuel_Cost;
		return Spent;
	}

	public int getState() {
		return state;
	}

	public int getSucExchange() { // maize
		return successful_exchange;
	}

	public int getTag() {
		return tag;
	}

	public int getWaterCal() {
		return Water_Cost;
	}

	/** Counts how many individuals in the family are over the age of 7 */
	public int getWorkerSize() {
		return familyUnit.getCountAtOrAboveAge(8);
	}

	public VillageSpace getWorld() {
		return world;
	}

	public int getWorldTime() {
		return this.getMySwarm().getWorldTime();
	}

	public int getWorldX() {
		return worldX;
	}

	public int getWorldY() {
		return worldY;
	}

	public int getWRdistance() {
		return wtsr / 5;
	}

	public int getX() {
		return x;
	}


	public int getY() {
		return y;
	}

	public int getYield() {
		return cell[4].getMaize_prod();
	}

	/**
	 * @param harvest
	 * @param work_cal
	 * @param harvest_adjustment
	 * @return
	 */
	public int harvestPlots() {
		int i;
		double harvest = 0;
		double cell_harvest = 0;
		int harvest_work = 0;

		double harvest_adjustment = this.getMySwarm().getHarvestAdjust();
		for (i = 0; i < 9; i++) {

			// Check harvest for double dipping:
			cell_harvest = farm_pl[i] * cell[i].getMaize_prod() * 4
					/ Village.MAX_PLOTS;
			// MaizePotential is in kg per ha, so multiplying by 4 yields
			// kg/cell &
			// dividing by Village.PLOTS yields kg/1-ac plot (when
			// Village.PLOTS=10)

			cell_harvest /= Village.FALLOW_FACTOR * harvest_adjustment;
			// set Village.FALLOW_FACTOR & Village.ADJUST_FACTOR with
			// defines in village.h

			// harvest costs

			harvest_work += (int) (cell_harvest / 25 * Village.WORK_CAL_MAN * 2);
			harvesting += cell_harvest / 25 * 2;
			// Each 25 kg maize harvested costs 2 hours work, needs
			// authentication

			// This code adds a travel cost for farming in fields away from
			// the home cell
			if (i != 4 && farm_pl[i] > 0) {
				// radius is 1 gender is work calories for male, and the
				// number of trips is based on parameter set in Village.h
				double travelCal = calcTravelCal(1, 0,
						(cell_harvest / Village.CARRY_CAPACITY));
				harvest_work += travelCal;
				harvesting += travelCal	/ Village.WORK_CAL_MAN;
			}

			harvest += cell_harvest;
		}

		act_yield = (int) harvest; // so I can report this elsewhere
		return harvest_work;
	}

	public int importFood(int maize_donated) {
		int maize_gotten;

		if (maize_donated <= 0) {
			return 0;
		}

		maize_gotten = Math.min(maize_donated, Village.MAX_EXCHANGE_AMOUNT);
		setMaizeStorage(getMaizeStorage() + maize_gotten);
		maize_imported += maize_gotten;

		update_state();
		if (Village.COOP_DEBUG) {
			System.out.printf("Agent %d received %d of donated maize.", tag,
					maize_donated);
		}
		return maize_gotten;
	}

	// Allow for protein importation
	public int importFoodProtein(int protein_donated) {
		int protein_gotten;

		if (protein_donated <= 0) {
			return 0;
		}

		protein_gotten = Math.min(protein_donated,
				Village.MAX_PROTEIN_EXCHANGE_AMOUNT);

		setCurrentProteinStorage(getCurrentProteinStorage() + protein_gotten);
		protein_imported += protein_gotten;

		update_state();
		if (Village.COOP_DEBUG) {
			System.out.printf("Agent %d received %d of donated protein.", tag,
					protein_donated);
		}
		return protein_gotten;
	}

	public void increaseC_water(double amount) {
		C_water += amount;		
	}

	/**
	 * @param ad_plots
	 */
	public void laborCheck(int ad_plots) {
		int k;
		int i;
		// plot_need should be equal to what needs to be planted/shed this
		// period
		// as determined in -evalState
		// but here we check to see if there is enough labor
		// only >7 yr olds contribute meaningful labor
		k = (int) season_fam; // they are the same at this point
		if (tot_plots > k + ad_plots) {
			plot_need = -1;
		} else if (tot_plots == k + ad_plots && plot_need > 0) {
			plot_need = 0; // can't add plots; labor limited
		} else if (tot_plots + plot_need > k + ad_plots) {
			i = k + ad_plots - tot_plots; // maximum allowable plots -
			// current plots (=
			// expandable capacity)
			plot_need = i;
		}
	}

	/**
	 * @return
	 */
	public int monitorPlots(int weeding_days, int monitoring_days) {
		int i;
		int work_cal = 0;
		for (i = 0; i < 9; i++) {
			work_cal += weeding_days * farm_pl[i] * 4 * Village.WORK_CAL_WOM;
			weeding += weeding_days * 4 * farm_pl[i];
			// This code adds a travel cost for farming in fields away from the
			// home cell
			// kids have to go out to the fields every day and see how they are
			// doing, maybe scare stuff away
			if (i == 4 && farm_pl[i] > 0) {
				double travelCal = calcTravelCal(0, 2, (monitoring_days * farm_pl[i]));
				work_cal += travelCal;
				monitoring += travelCal / Village.WORK_CAL_KID; 
				// dividing the cal returned by calcTravelCal by
				// Village.WORK_CAL_KID yields hours
			}

			if (i != 4 && farm_pl[i] > 0) {
				double travelCal = calcTravelCal(1, 1, (weeding_days * farm_pl[i]));
				work_cal += travelCal;
				weeding += travelCal / Village.WORK_CAL_WOM;

				travelCal = calcTravelCal(1, 2, (monitoring_days * farm_pl[i]));
				work_cal += travelCal;

				monitoring += travelCal / Village.WORK_CAL_KID;
				// dividing the cal returned by calcTravelCal by
				// Village.WORK_CAL_KID yields hours
			}
		}
		return work_cal;
	}

	int mortality(int age, int gend) {
		// check to see if person dies randomly (within age group)
		// this is the modifed version which depends on the state of the
		// household
		double tmp = 1.0;
		int test;

		float state_good = this.getMySwarm().getState_good();
		float state_bad = -state_good;
		if (state<1)
			tmp -= state_bad;
		if (state>1) 
			tmp -= state_good;
		// protein penalty

		int p_penalty = this.getMySwarm().getProteinPenalty();
		if (p_penalty == 1) {
			if (protein_deficient != 0 && state > 1) {
				tmp = 1.0;
			}
		}

		if (age < 1) {
			test = Village.uniformDblRand(0.0, 1.0) < 0.2330 * tmp ? 1 : 0;
			if (test == 1) {
				numDeaths++;
			}
			return test;
		}

		// fail safe: needed if agents survive past life table
		if (age > 75) {
			numDeaths++;
			return 1;
		}

		// evaluate mortality at 3,8,13,...

		if (age % 5 == 3) {
			int ind = age / 5;
			double danger = Village.uniformDblRand(0.0, 1.0);

			if (danger < death_per[ind] * tmp) {
				numDeaths++;
				return 1;
			}
		}
		return 0;
	}

	// **** movement and search ****
	public void moveHouse(boolean force) {
		// catch statements to ensure all variables are valid

		if (x > Village.WORLD_X_SIZE) {
			System.out
			.printf(
					"Village.ERROR: -moveHouse x variable invalid agent = %d, x = %d\n",
					tag, x);
		}

		if (y > Village.WORLD_Y_SIZE) {
			System.out
			.printf(
					"Village.ERROR: -moveHouse y variable invalid agent = %d, y = %d\n",
					tag, y);
		}

		ArrayList<ImaginaryCell> moveCells = searchNeighborhoodAllDX(this.x, this.y, Village.MOVE_RAD);

		// Don't move if there are no cells to move to!
		if(moveCells.size()==0){
			return;
		}

		// Get the top ranked move-cell
		ImaginaryCell bestCell = moveCells.get(0);

		// Find the movecell that is the current cell

		ImaginaryCell currentCell = null;
		for(ImaginaryCell c : moveCells){
			if(c.getX()==this.x && c.getY()==this.y){
				currentCell = c;
			}
		}

		// If current cell has the same potential energy as the best cell, 
		// don't change anything and return.
		if(force && bestCell==currentCell){
			bestCell = moveCells.get(1);
		}else if(currentCell != null && currentCell.getPotentialEnergy() == bestCell.getPotentialEnergy()){
			return;
		}

		this.unPlotAll();
		cell[4].removeSettler(this);

		//		System.out.println("Agent: " + this + "   OLD CELLS:");
		//		for (Cell c : this.cell)
		//			System.out.println("cell x: " + c.x + ", cell y: " + c.y);

		this.setXY(bestCell.getX(), bestCell.getY());

		//		System.out.println("Agent: " + this + "   NEW CELLS:");
		//		for (Cell c : this.cell)
		//			System.out.println("cell x: " + c.x + ", cell y: " + c.y);
		//		System.out.println("\n");

		if (Village.DEBUG) {
			if (tag == Village.TAG) {
				System.out
				.printf(
						"Village.DEBUG -moveHouse: agent %d with state %d moving to (%d,%d).  Agent was formed %d.\n",
						tag, state, x, y, form_date);
			}
		}
		state = 1;
		//		plot_need = (int) 2 + familyUnit.getKidCount() / 2; // This call to reset the plot need makes no sense.
		last_move = 0;

		return;
	}

	/**
	 * The searchNeighborhoodAll function is a function designed using the
	 * greedy approach however it incorporated multiple variables into the
	 * search routine. While other functions only look at the productivity of
	 * the land this function searches for the place with the best available
	 * land, distance to water, distance to firewood, and lowest hunting costs
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<ImaginaryCell> searchNeighborhoodAllDX(int x, int y, int radius){

		//		System.out.println(x + "," + y);

		ArrayList<ImaginaryCell> moveCells = new ArrayList<ImaginaryCell>((int)Math.pow(((radius*2)+1),2));

		//		System.out.println((int)Math.pow(((Village.MOVE_RAD*2)+1),2));

		int dx, dy;
		// Set x and y locations of all cells in move radius for easy calculations down the road.
		for (dy = y - radius; dy <= y + radius; dy++) {
			for (dx = x - radius; dx <= x + radius; dx++) {

				if(dx>=0 & dx<Village.WORLD_X_SIZE & dy>=0 & dy<Village.WORLD_Y_SIZE){
					ImaginaryCell newcell = new ImaginaryCell();
					newcell.setX(dx);
					newcell.setY(dy);
					moveCells.add(newcell);
					//					System.out.println(newcell);
					//					System.out.println(moveCells.size());
				}

			}
		}

		//		System.out.println("Final size: " + moveCells.size());

		/*
		 * The first search gets the productivity of all of the cells within the search radius.
		 */
		double harvest_adjustment = (double) this.getMySwarm().getHarvestAdjust();
		int tempPlots = tot_plots;

		ArrayList<ImaginaryCell> moveCellsClone = (ArrayList<ImaginaryCell>) moveCells.clone();
		for(ImaginaryCell c : moveCellsClone) {
			Cell tempcell = (Cell) world.getObjectAt(c.getX(), c.getY());

			if (this.getMySwarm().getTrade_boosted_move() && this.getMySwarm().getEconomy()>0) {
				// for now we're going to keep this recalculating
				c.setProductivity(boostByProximityOfWealthyPartnersAndKin(tempcell.getMaize_prod(), tempcell));
			}else{
				// Get the maize potential productivity of the cell, after degradation
				c.setProductivity(tempcell.getMaize_prod());
			}

			// Get the 
			c.setNumPlots(tempcell.getFarmPl());

			if (tempPlots < 1) {
				tempPlots = 1;
			}

			int potProd = 0;
			potProd = c.getProductivity() * 4 / Village.MAX_PLOTS * tempPlots;
			potProd /= (double) Village.FALLOW_FACTOR * (double) harvest_adjustment;

			int hrvst_cost = (int) (potProd / 25 * 240);
			potProd = potProd * Village.MAIZE_KG_CAL;
			potProd = potProd - (hrvst_cost + Village.AVG_FARM_COST) * tempPlots;



			if(Village.DOMESTICATION){
				this.setI_turkey_kept(0);
			}


			if(Village.DRINK_WATER){
				c.setWater_cost(searchdrinkWateratX(c.getX(), c.getY()));
				potProd -= c.getWater_cost();
			}
			
			if(Village.DRINK_WATER && Village.NEED_H2O){
				c.setWaterNearby(waterNearby(c.getX(), c.getY()));
			}

			if(Village.HUNTING && this.getMySwarm().getP_need() > 0){
				c.setHunt_cost(huntingStrategy.searchHuntatX(c.getX(), c.getY()));
				potProd -= c.getHunt_cost();
			}

			if(Village.BURN_WOOD){
				c.setWood_cost(searchburnWoodatX(c.getX(), c.getY()));
				potProd -= c.getWood_cost();
			}

			c.setPotentialEnergy(potProd);

			//			c.setPopulation(tempcell.getPopulation());
		}

		if(Village.HUNTING && this.getMySwarm().getNeed_meat()==1){
			moveCellsClone = (ArrayList<ImaginaryCell>) moveCells.clone();
			for(ImaginaryCell c : moveCellsClone) {
				if(c.getHunt_cost()==0){
					moveCells.remove(c);
				}
			}
		}

		if(Village.DRINK_WATER && Village.NEED_H2O){
			moveCellsClone = (ArrayList<ImaginaryCell>) moveCells.clone();
			for(ImaginaryCell c : moveCellsClone) {
				if(!c.isWaterNearby()){
					moveCells.remove(c);
				}
			}
		}


		moveCellsClone = (ArrayList<ImaginaryCell>) moveCells.clone();
		for(ImaginaryCell c : moveCellsClone) {
			if(c.getX() == this.x && c.getY()==this.y) continue;
			if(c.getNumPlots() >= Village.MAX_PLOTS) moveCells.remove(c);
		}

		Collections.shuffle(moveCells); // Ensures the direction of movement is random.
		Collections.sort(moveCells); // Ranks cells. Cells with same potential energy will stay in a random order.

		return moveCells;
	}


	// set our own internal coordinates, also set cell neighborhood
	public void setXY(int inX, int inY) {
		this.x = inX;
		this.y = inY;
		// place agent into the world by messaging cell to keep track of
		this.setCells(); // init cell cache, method defined below to have radius 1
		cell[4].addSettler(this);
		return;
	}

	// **** agent internal state functions ****
	// handy guide showing where k points to:
	// X or j:
	// 				-1 0 1
	// Y or i: 	  -1 0 1 2
	// 			   0 3 4 5 // 4 is self
	//             1 6 7 8
	public void setCells() { // sets cell neighborhood

		int i, j, k;
		k = 0; // array cell[9] defined as instance var of agent in agent.h

		if (cell[4] != null) {
			if (cell[4].getSettlerSet().contains(this)) {
				// cell[4].getSettlerSet().remove(this);
				cell[4].removeSettler(this);
			}
		}

		for (i = -1; i < 2; i++) {
			for (j = -1; j < 2; j++, k++) {
				cell[k] = (Cell) world.getObjectAt(Village.wrapX(j, this.x, worldX),
						Village.wrapY(i, this.y, worldY));
			}
		}
	}

	/**
	 * @param work_cal
	 * @return
	 */
	public int planting(int work_cal, int days) {
		int i;
		A_Plots = 0;
		H_Plots = 0;

		for (i = 0; i < 9; i++) {
			if (i != 4) {
				A_Plots += farm_pl[i];
			} else {
				H_Plots += farm_pl[i];
			}

			// need 17 days to plant & hoe 1-ac field. 14 days to clear and hoe
			// + 3 days to plant (Forde p. 390)
			// one of our grid cells = 40,000 sq m = 4 ha = 9.88 acres
			// so at 9 plots/cell, each plot is about 1 acre
			// 17 days * 8 Hours * Number of Plots * calories per hour
			// next costs are for all plots, home or away

			work_cal += days * farm_pl[i] * 8 * Village.WORK_CAL_MAN; // converts
			// hours .
			// calories
			// and
			// cumulates
			planting += days * 8 * farm_pl[i]; // (in hours); initialized in
			// -stepProcureInit

		}
		return work_cal;
	}

	public int plantPl(int plots, int num) {
		int plots_to_plant = plots;
		int cell_index = num;

		if (Village.ENABLE_MULTITHREADING) {
			cell[cell_index].getFarmLock().lock();
		}

		int plots_in_cell = cell[cell_index].getFarmPl();
		int work_cal = 0;
		if (plots_to_plant > 0) {
			if (plots_to_plant + plots_in_cell > Village.MAX_PLOTS) {
				plots_to_plant = Village.MAX_PLOTS - plots_in_cell;
			}

			// cost
			// 14 (8 hour) days to clear and hoe 1 acre plot
			// this is double the cost that is taken from Forde, 1931
			work_cal += 14 * plots_to_plant * 8 * Village.WORK_CAL_MAN;
			plotwork += 14 * plots_to_plant * 8;
			// This code adds a travel cost for farming in fields away from the
			// home cell
			if (cell_index != 4) {
				double travelCal = calcTravelCal(1, 0, (14 * plots_to_plant));
				work_cal += travelCal;
				plotwork += travelCal / Village.WORK_CAL_MAN;
			}
			Ag_Cost += work_cal;

			// Increases actual_cal to match other net caloric needs for agents.
			// MAIZE_PER gets reapplied in eatMaize and at the end of each year
			// in evalState.
			int real_cal = (int) (work_cal * 1 / Village.MAIZE_PER);
			actual_cal += real_cal;

			cell[cell_index].changeFarmPl(plots_to_plant);

			farm_pl[cell_index] += plots_to_plant;
			tot_plots += plots_to_plant;
			plots -= plots_to_plant; // return plots left
		} else if (plots_to_plant < 0) // remove plots
		{
			int plots_to_remove = plots_to_plant * -1;

			if (plots_to_remove > farm_pl[cell_index]) {
				plots_to_remove = farm_pl[cell_index];
			}

			cell[cell_index].changeFarmPl(plots_to_remove * -1);
			farm_pl[cell_index] -= plots_to_remove;
			tot_plots -= plots_to_remove;
			plots += plots_to_remove;
		}

		if (Village.ENABLE_MULTITHREADING) {
			cell[cell_index].getFarmLock().unlock();
		}


		return plots;
	}

	/**
	 * @param caller
	 * @param work_cal
	 * @return
	 */
	public int plantPlots(int caller) {
		int dx;
		int dy;
		int k;
		int work_cal = 0;
		// plant until done, start with homestead 4 hectares (home cell)
		// passing instance var, might want function for later uses not with
		// plot_need
		if (plot_need > 0) {
			plot_need = plantPl(plot_need, 4);
		} else if (plot_need < 0 && tot_plots == farm_pl[4]) {
			plot_need = plantPl(plot_need, 4);
		}

		k = 0;
		while (plot_need != 0 && k < 7) { // no room in home cell
			dx = dy = 0;
			if (plot_need > 0) {
				//				int[] res = searchNeighborhoodDX(dx, dy, 1, caller);
				ArrayList<ImaginaryCell> potentialPlots = searchPlotProd(this.getX(),this.getY(),1);

				if(potentialPlots.isEmpty()){
					//					System.out.println("Agent " + this + " can't spot a spot to plot a plot! MOVING!!!");
					this.moveHouse(true);
					return work_cal;
				}
				//				System.out.println(potentialPlots);

				//				for(ImaginaryCell c : potentialPlots){
				//					System.out.println(c.getPotentialEnergy());
				//				}

				dx = potentialPlots.get(0).getX() - this.getX();
				dy = potentialPlots.get(0).getY() - this.getY();
			} else {
				int[] res = unplotDX(dx, dy);
				dx = res[0];
				dy = res[1];
			}

			dx++;
			dy++;

			if (dy * 3 + dx > 8 || dy * 3 + dx < 0) {
				System.err.printf(
						"Village.ERROR: agent %d plot ranged dx %d dy %d\n",
						tag, dx, dy);
				System.exit(-1);
			}

			plot_need = plantPl(plot_need, (dy * 3 + dx));
			k++; // safety
		}

		work_cal += planting(work_cal, 17);
		return work_cal;
	}

	/* scheduled after all yearly agent actions,
        // and after randomizing agentList, removing
        // agents, and diffusing deer */
	public void printagentstats(boolean heading) 
	{int run_number = this.getMySwarm().getFileID();
		String combined = "output/agent_stats_run_" + run_number + ".csv";

		if (heading) {
			try {
				FileWriter out = new FileWriter(new File(combined));
				out.write("Year," +
						"agent," +
						"x," +
						"y," +
						"family_size_begin," +
						"family_size_end," +
						"births," +
						"deaths," +
						"arriving_newlyweds," +
						"departing_newlyweds," +
						"state\n");
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		try {
			FileWriter out = new FileWriter(new File(combined),true);
			out.write(this.getMySwarm().getWorldTime() + "," +
			this.getTag() + "," +
			this.getX() + "," +
			this.getY() + "," +
			this.getBeginSize() + "," +
			this.getFamilySize() + "," +
			this.getNumBirths() + "," +
			this.getNumDeaths() + "," +
			this.getArrivingNewlyweds() + "," +
			this.getDepartingNewlyweds() + "," +
			this.getState() + "\n");
	out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/** Called when agents work more than 14 hours per day. */
	protected void printTooMuchWorkError() {
		System.out.printf(
				"Houston we have a problem: agent = %d, Family = %d, hrs worked per worker = %f\n",
				tag, (int) season_fam, hrs);
		System.out.printf("number of plots = %d\n", tot_plots);
		System.out
		.printf("**************breakdown of hours*****************\n");
		System.out
		.printf("Plant\tWeed\tHarvest\tPlot\tFuel\tWater\tHunting\n");
		System.out.printf("%d\t%d\t%d\t%d\t%d\t%d\t%d\n\n\n",
				(int) planting, (int) weeding, (int) harvesting,
				(int) plotwork, (int) C_fuel, (int) C_water,
				huntingStrategy.getC_hunt());
	}

	int promoteChild() {
		int res = familyUnit.promoteChild();

		if (res == 1) {
			child_per_female = 0;
			last_year_child_born = 0;
		}

		return res;
	}

	void removePerson(Individual ind) {
//		ind.die();
//		ind = null;
		familyUnit.removePerson(ind);
	}

	public int RequestPayBack(int amount) {
		//System.out.printf("tag = %d, food = %d\n", tag, amount);
		return exportMaize(amount);
	}

	public int RequestPayBackProtein(int amount) {
		//System.out.printf("tag = %d, food protein = %d\n", tag, amount);
		return exportProtein(amount);
	}

	void saveDefector() {
		if (Village.SAVE_LINKS) {
			String str;
			int worldtime = this.getMySwarm().getWorldTime();

			str = String.format("%d %d %d %d\n", tag, worldtime,
					DefectingAgentCount, DefectingAgentTurnover);

			this.getMySwarm().log(str, "defector", false);
		}
	}

	void saveDonate(int A, int B) {
		if (Village.SAVE_LINKS) {
			String str;

			str = String.format("%d %d %d %d %d\n", tag, x, y, A, B); // A=
			// initially
			// amout
			// it
			// had,
			// B=amt
			// after
			// the
			// request

			this.getMySwarm().log(str, "donate", true);
			// [mySwarm log:str toFile:"donate" usingYear: YES];
		}
		return;
	}

	/*
	 * void test() { int i = 0, j = 0, caller = 0;
	 * System.out.printf("Village.BEFORE: agent %d pos (%d,%d)\n", tag, x, y);
	 * int[] res = this.searchNeighborhoodDX(j, i, 2, caller); j = res[0]; i =
	 * res[1]; System.out.printf("Village.AFTER:  agent %d dxL %d dy %d\n", tag,
	 * j, i); return; }
	 */

	void saveFood() {
		if (Village.SAVE_LINKS) {
			String str;

			str = String.format("%d %d %d %d %d %d %d %d %d %d %d %d %d", tag,
					x, y, getMaizeStorage(), tot_plots, plot_need, max_store,
					EXP_cal_need, AVG_cal_prod, EXP_yield, act_yield,
					actual_cal, total_cal);

			this.getMySwarm().log(str, "food", true);
		}
	}

	void saveRequest(int A, int B) {
		if (Village.SAVE_LINKS) {
			String str;

			str = String.format("%d %d %d %d %d\n", tag, x, y, A, B); // A=
			// initially
			// amout
			// it
			// had,
			// B=amt
			// after
			// the
			// request

			this.getMySwarm().log(str, "request", true);
		}
	}

	public Agent searchAgentList(int ptag) {
		return this.getMySwarm().searchAgentList(ptag);
	}

	int searchburnWoodatX(int dx, int dy) {
		int need, dw, bw_cals, initx, inity, m;
		int imx[] = new int[1000];
		int imy[] = new int[1000];
		int q;
		float trips, cellwood;
		initx = dx;
		inity = dy;
		Cell c;
		trips = 0;
		bw_cals = 0;
		dw = 0;
		q = 0;
		need = FW_need * getFamilySize();
		while (need > 0 && q < 1000) {  // it has to stop somewhere
			dx = initx;
			dy = inity;
			int[] res = searchNeighborhoodFireDX(dx, dy, dw, 1); // dw acts as a
			// switch
			// between
			// standing
			// crop and
			// dead wood
			dx = res[0];
			dy = res[1];
			dw = res[2];

			if (dw == 1) // if dw = 1 than wood taken from standing crop, if dw
				// = 0 wood taken fron deadwood
			{
				c = (Cell) world.getObjectAt(Village.wrapX(dx, x, worldX),
						Village.wrapY(dy, y, worldY));
				cellwood = c.getFWPotential(1);

				if (cellwood >= need) {
					cellwood = cellwood - need;
					trips = need / Village.CARRY_CAPACITY;
					bw_cals += calcTravelCal(FWsearchradius, 1, trips);

					// calculates collection costs
					bw_cals += trips * Village.SC_COLLECT
							* Village.WORK_CAL_WOM;

					need = 0;
					c.setFWPotential(cellwood, 1);
				} else {
					need = need - (int) cellwood;
					trips = cellwood / Village.CARRY_CAPACITY;
					bw_cals += calcTravelCal(FWsearchradius, 1, trips);

					// calculates collection costs
					bw_cals += trips * Village.SC_COLLECT
							* Village.WORK_CAL_WOM;

					c.setFWPotential(0, 1);
				}
			} else {
				c = (Cell) world.getObjectAt(Village.wrapX(dx, x, worldX),
						Village.wrapY(dy, y, worldY));
				cellwood = c.getDWPotential(1);
				if (cellwood >= need) {
					cellwood = cellwood - need;
					trips = need / Village.CARRY_CAPACITY;

					bw_cals += calcTravelCal(FWsearchradius, 1, trips);

					// calculates collection costs
					bw_cals += trips * Village.DW_COLLECT
							* Village.WORK_CAL_WOM;

					need = 0;
					c.setDWPotential(cellwood, 1);
				} else {
					need = (int) (need - cellwood);
					trips = cellwood / Village.CARRY_CAPACITY;
					bw_cals += calcTravelCal(FWsearchradius, 1, trips);

					// calculates collection costs
					bw_cals += trips * Village.DW_COLLECT
							* Village.WORK_CAL_WOM;

					c.setDWPotential(0, 1);
				}
			}

			if (FWsearchradius > 99) {
				trips = need / Village.CARRY_CAPACITY;
				bw_cals += calcTravelCal(FWsearchradius, 1, trips);

				// calculates collection costs
				bw_cals += trips * Village.SC_COLLECT * Village.WORK_CAL_WOM;

				need = 0;
			}

			imx[q] = dx;
			imy[q] = dy;
			q++;
		}
		// this code resets the imaginary wood numbers to their real numbers for
		// future searches

		for (m = 0; m < q; m++) {

			c = (Cell) world.getObjectAt(Village.wrapX(imx[m], x, worldX),
					Village.wrapY(imy[m], y, worldY));
			c.resetImgWood();
		}
		if (q > 1000) {
			System.out
			.printf("\n\n *** Village.ERROR Burn wood Search out of bounds.***\n\n");
			System.exit(0);
		}

		return bw_cals;
	}

	int searchdrinkWateratX(int dx, int dy) {
		int need, W_cals, w_carry_capacity;
		Cell c;
		double trips;
		int w_type;
		float l_max;

		trips = 0;
		W_cals = 0;
		need = W_need * getFamilySize();

		if (this.getMySwarm().isDomestication() && this.getMySwarm().isTurkey_water()){
			//			System.out.printf("i_turkey_kept: %d\n", this.i_turkey_kept);
			need += (int)(this.i_turkey_kept * DomesticationParameters.TURKEY_WATER_NEED * 365);
		}

		w_carry_capacity = Village.CARRY_CAPACITY - 4; // water carrying
		// capacity = total
		// carrying capacity -
		// vessel weight
		// (4kg)(Lightfoot,
		// 1994)

		while (need > 0) {
			int[] res = searchNeighborhoodwaterDX(dx, dy);
			dx = res[0];
			dy = res[1];

			c = (Cell) world.getObjectAt(Village.wrapX(dx, x, worldX), Village
					.wrapY(dy, y, worldY));
			w_type = c.getWaterType();
			l_max = c.getWater();

			if (w_type == 4) {
				if (l_max >= need) {
					l_max = l_max - need;
					trips = need / w_carry_capacity;
					W_cals += calcTravelCal(Wsearchradius, 1, trips);
					need = 0;
				} else {
					need = (int) (need - l_max);
					trips = l_max / w_carry_capacity;
					W_cals += calcTravelCal(Wsearchradius, 1, trips);

				}
			} else {
				trips = need / w_carry_capacity;
				W_cals += calcTravelCal(Wsearchradius, 1, trips);
				need = 0;
			}
		}

		return W_cals;
	}


	/**
	 * The searchNeighborhoodAll function is a function designed using the
	 * greedy approach however it incorporated multiple variables into the
	 * search routine. While other functions only look at the productivity of
	 * the land this function searches for the place with the best available
	 * land, distance to water, distance to firewood, and lowest hunting costs
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<ImaginaryCell> searchPlotProd(int x, int y, int radius) {

		//		System.out.println("Searching for plots around " + x + "," + y);

		ArrayList<ImaginaryCell> moveCells = new ArrayList<ImaginaryCell>((int)Math.pow(((radius*2)+1),2));

		int dx, dy;
		// Set x and y locations of all cells in move radius for easy calculations down the road.
		for (dy = y - radius; dy <= y + radius; dy++) {
			for (dx = x - radius; dx <= x + radius; dx++) {
				if(dx>=0 & dx<Village.WORLD_X_SIZE & dy>=0 & dy<Village.WORLD_Y_SIZE){
					ImaginaryCell newcell = new ImaginaryCell();
					newcell.setX(dx);
					newcell.setY(dy);
					moveCells.add(newcell);
				}
			}
		}

		ArrayList<ImaginaryCell> moveCellsClone = (ArrayList<ImaginaryCell>) moveCells.clone();
		for(ImaginaryCell c : moveCellsClone) {
			Cell tempcell = (Cell) world.getObjectAt(c.getX(), c.getY());

			c.setProductivity(tempcell.getMaize_prod());
			c.setNumPlots(tempcell.getFarmPl());
			c.setPotentialEnergy(c.getProductivity());
		}

		moveCellsClone = (ArrayList<ImaginaryCell>) moveCells.clone();
		for(ImaginaryCell c : moveCellsClone) {
			if(c.getNumPlots() >= Village.MAX_PLOTS){
				moveCells.remove(c);
			}
		}

		Collections.shuffle(moveCells);
		Collections.sort(moveCells);

		return moveCells;
	}


	//	/** DC: Again using a pair instead of pointers for a and b */
	//	public int[] searchNeighborhoodDX(int a, int b, int radius, int caller) // : (int
	//																		// *) a
	//																		// DY:
	//																		// (int
	//																		// *) b
	//																		// Rad:
	//																		// (int)
	//																		// radius
	//																		// From:
	//																		// (int)
	//																		// caller
	//	{
	//		int k, max, dx, dy;
	//
	//		if (Village.MOVE_STRATEGY != 0) {
	//
	//			k = max = 0;
	//
	//			// Get the target coordinates of the top agent and define the
	//			// quadrant to move into
	//
	//			for (dy = -radius; dy < radius; dy++) {
	//				for (dx = -radius; dx < radius; dx++) {
	//					k = evalCellX(dx, dy, max, caller);
	//					if (k != 0) {
	//						max = k;
	//						a = dx;
	//						b = dy;
	//					}
	//				}
	//			}
	//
	//		} else {
	//
	//			k = max = 0;
	//			for (dy = -radius; dy < radius; dy++) {
	//				for (dx = -radius; dx < radius; dx++) {
	//					k = evalCellX(dx, dy, max, caller);
	//					if (k != 0) {
	//						max = k;
	//						a = dx;
	//						b = dy;
	//					}
	//				}
	//			}
	//
	//		}
	//
	//		return new int[] { a, b };
	//	}

	// searches neighborhood for closest cell with resources needed by agent
	// currently used to find firewood on landscape 3/11/04 J. Cowan
	// Updated 12/04
	/** DC: returns an array of int containing the updated valus for a, b, and c */
	int[] searchNeighborhoodFireDX(int a, int b, int c, int img) // : (int *) a																// img
	{
		int k, r, dx, dy, ddx, ddy;
		dx = a;
		dy = b;
		ddx = dx;
		ddy = dy;
		k = 0;
		r = 0;
		int firsttime;
		int DWfirsttime;

		/*
		 * if (tag == 3) System.out.printf("%d,%d,%d,%d,%d,%d\n", a, b, dx, dy,
		 * ddx, ddy);
		 */
		firsttime = 1;
		DWfirsttime = 1;

		while (r <= 100) {
			if (DWfirsttime != 0) {
				k = evalDWinCellX(ddx, ddy, img);
				if (k > 0) // 1. kill 2. random relocation
				{
					a = ddx;
					b = ddy;
					c = 0;
					FWsearchradius = r;
					return new int[] { a, b, c };
				}
				DWfirsttime = 0;
			} else {
				for (ddy = dy - r; ddy < dy + r + 1; ddy++) {
					if (ddy == dy - r || ddy == dy + r) {
						for (ddx = dx - r; ddx < dx + r + 1; ddx++) {
							k = evalDWinCellX(ddx, ddy, img);
							if (k > 0) // 1. kill 2. random relocation
							{
								a = ddx;
								b = ddy;
								c = 0;
								FWsearchradius = r;
								return new int[] { a, b, c };
							}
						}
					} else {
						ddx = dx - r;
						k = evalDWinCellX(ddx, ddy, img);
						if (k > 0) // 1. kill 2. random relocation
						{
							a = ddx;
							b = ddy;
							c = 0;
							FWsearchradius = r;
							return new int[] { a, b, c };
						}
						ddx = dx + r;
						k = evalDWinCellX(ddx, ddy, img);
						if (k > 0) // 1. kill 2. random relocation
						{
							a = ddx;
							b = ddy;
							c = 0;
							FWsearchradius = r;
							return new int[] { a, b, c };
						}
					}
				}
			}

			if (r - Village.FW_SEARCH_DISTANCE >= 0) {
				if (firsttime != 0) {
					k = evalFWinCellX(ddx, ddy, img);
					if (k > 0) // 1. kill 2. random relocation
					{
						a = ddx;
						b = ddy;
						c = 1;
						FWsearchradius = r;
						return new int[] { a, b, c };
					}
					firsttime = 0;
				} else {
					for (ddy = dy - (r - Village.FW_SEARCH_DISTANCE); ddy < dy
							+ r - Village.FW_SEARCH_DISTANCE + 1; ddy++) {
						if (ddy == dy - (r - Village.FW_SEARCH_DISTANCE)
								|| ddy == dy + r - Village.FW_SEARCH_DISTANCE) {
							for (ddx = dx - (r - Village.FW_SEARCH_DISTANCE); ddx < dx
									+ r - Village.FW_SEARCH_DISTANCE + 1; ddx++) {
								k = evalFWinCellX(ddx, ddy, img);
								if (k > 0) // 1. kill 2. random relocation
								{
									a = ddx;
									b = ddy;
									c = 1;
									FWsearchradius = r;
									return new int[] { a, b, c };
								}
							}
						} else {
							ddx = dx - (r - Village.FW_SEARCH_DISTANCE);
							k = evalFWinCellX(ddx, ddy, img);
							if (k > 0) // 1. kill 2. random relocation
							{
								a = ddx;
								b = ddy;
								c = 1;
								FWsearchradius = r;
								return new int[] { a, b, c };
							}
							ddx = dx + r - Village.FW_SEARCH_DISTANCE;
							k = evalFWinCellX(ddx, ddy, img);
							if (k > 0) // 1. kill 2. random relocation
							{
								a = ddx;
								b = ddy;
								c = 1;
								FWsearchradius = r;
								return new int[] { a, b, c };
							}
						}
					}
				}
			}
			r++;
		}

		a = ddx;
		b = ddy;
		FWsearchradius = 100;

		return new int[] { a, b, c };
	}

	//	/*
	//	 * Searches the neighborhood for the best evaluated location to move into
	//	 * uses a greedy approach along with an incremental radius from
	//	 * Village.MIN_MOVE_RAD to Village.MAX_MOVE_RAD
	//	 */
	//	/** DC: returns an int array containing the updated values for a and b */
	//	int[] searchNeighborhoodGreedyDX(int a, int b, int radius, int caller) // :
	//																			// (
	//																			// int
	//																			// *
	//																			// )
	//																			// a
	//																			// DY
	//																			// :
	//																			// (
	//																			// int
	//																			// *
	//																			// )
	//																			// b
	//																			// Rad
	//																			// :
	//																			// (
	//																			// int
	//																			// )
	//																			// radius
	//																			// From
	//																			// :
	//																			// (
	//																			// int
	//																			// )
	//																			// caller
	//	{
	//		int k, max, dx, dy;
	//		int korigin, r;
	//		int firsttime;
	//		k = max = 0;
	//
	//		korigin = evalCellX(0, 0, 0, caller);
	//		r = Village.MIN_MOVE_RAD;
	//
	//		firsttime = 1;
	//		while (r <= Village.MAX_MOVE_RAD && k <= korigin) {
	//			if (firsttime != 0) {
	//				for (dy = -r; dy < r; dy++) {
	//					for (dx = -r; dx < r; dx++) {
	//						k = evalCellX(dx, dy, max, caller);
	//						if (k != 0) // 1. kill 2. random relocation
	//						{
	//							max = k;
	//							a = dx;
	//							b = dy;
	//						}
	//					}
	//				}
	//				r++;
	//				firsttime = 0;
	//			} else {
	//				for (dy = -r; dy < r; dy++) {
	//					if (dy == -r || dy == r - 1) {
	//						for (dx = -r; dx < r; dx++) {
	//							k = evalCellX(dx, dy, max, caller);
	//							if (k != 0) // 1. kill 2. random relocation
	//							{
	//								max = k;
	//								a = dx;
	//								b = dy;
	//							}
	//						}
	//					} else {
	//						dx = -r;
	//						k = evalCellX(dx, dy, max, caller);
	//						if (k != 0) // 1. kill 2. random relocation
	//						{
	//							max = k;
	//							a = dx;
	//							b = dy;
	//						}
	//						dx = r;
	//						k = evalCellX(dx, dy, max, caller);
	//						if (k != 0) // 1. kill 2. random relocation
	//						{
	//							max = k;
	//							a = dx;
	//							b = dy;
	//						}
	//					}
	//				}
	//				r++;
	//				firsttime = 0;
	//			}
	//		}
	//		g_countof_move_radius++;
	//		g_average_move_radius = (g_average_move_radius
	//				* (g_countof_move_radius - 1) + r)
	//				/ g_countof_move_radius;
	//		// System.out.printf("agent = %d, C[%d][%d]=%d\n", tag, x,y,korigin);
	//
	//		return new int[] { a, b };
	//	}

	/** searches neighborhood for closest cell with water resources needed by
	 agent */
	public int[] searchNeighborhoodwaterDX(int a, int b) {
		Cell closest = null;
		Cell location = getCellAt(a, b);
		double closestDistance = Double.POSITIVE_INFINITY;		

		ArrayList<Cell> waterCells = getMySwarm().getWaterManager()
				.getWaterCells();

		for (Cell c : waterCells) {
			double dist = 0;

			if (c.getWaterType() >= Village.H2O_TYPE
					&& ((dist = WaterManager.distance(location, c)) < closestDistance)
					&& c.getWater() > 0) {
				closest = c;
				closestDistance = dist;
			}
		}

		if (closest != null)
			Wsearchradius = (int) WaterManager.distance(closest, getCell());

		return new int[] { closest.getX(), closest.getY() };
	}

	// 5/04/04 J. Cowan
	/** searches neighborhood for closest cell with water resources needed by
	 agent */
	int[] searchNeighborhoodwaterDXOld(int a, int b) {
		int k, r, dx, dy, ddx, ddy;
		dx = a;
		dy = b;
		ddx = dx;
		ddy = dy;
		k = 0;
		r = 0;
		int firstime;

		firstime = 1;

		while (r <= 100) {
			if (firstime == 1) {
				k = evalWaterinCellX(ddx, ddy);
				if (k >= Village.H2O_TYPE) {
					a = ddx;
					b = ddy;
					Wsearchradius = r;

					return new int[] { a, b };
				}
				r++;
				firstime = 0;
			} else {
				for (ddy = dy - r; ddy < dy + r + 1; ddy++) {
					if (ddy == dy - r || ddy == dy + r) {
						for (ddx = dx - r; ddx < dx + r + 1; ddx++) {
							k = evalWaterinCellX(ddx, ddy);
							if (k >= Village.H2O_TYPE) // 1. kill 2. random
								// relocation
							{
								a = ddx;
								b = ddy;
								Wsearchradius = r;
								return new int[] { a, b };
							}
						}
					} else {
						ddx = dx - r;
						k = evalWaterinCellX(ddx, ddy);
						if (k >= Village.H2O_TYPE) // 1. kill 2. random
							// relocation
						{
							a = ddx;
							b = ddy;
							Wsearchradius = r;
							return new int[] { a, b };
						}
						ddx = dx + r;
						k = evalWaterinCellX(ddx, ddy);
						if (k >= Village.H2O_TYPE) // 1. kill 2. random
							// relocation
						{
							a = ddx;
							b = ddy;
							Wsearchradius = r;
							return new int[] { a, b };
						}

					}
				}
				r++;
			}
		}

		a = ddx;
		b = ddy;
		Wsearchradius = 100;

		return new int[] { a, b };
	}

	//	/** DC: returns an int array, which contains the updated values for a and b */
	//	int[] searchSocialNeighborhoodDX(int a, int b, int radius, int caller) // :
	//																			// (
	//																			// int
	//																			// *
	//																			// )
	//																			// a
	//																			// DY
	//																			// :
	//																			// (
	//																			// int
	//																			// *
	//																			// )
	//																			// b
	//																			// Rad
	//																			// :
	//																			// (
	//																			// int
	//																			// )
	//																			// radius
	//																			// From
	//																			// :
	//																			// (
	//																			// int
	//																			// )
	//																			// caller
	//	{
	//		int k, max, dx, dy;
	//		int sum, i, whotoask, pick, whotoaskID;
	//		Agent coopAgent;
	//
	//		k = max = 0;
	//
	//		// find a best kin, get x, get y
	//		// then use that kin's neighborhood to search
	//
	//		for (sum = 0, i = 0; i < Village.MAX_RELATIVE_LINKS
	//				+ Village.MAX_CHILD_LINKS + 2; i++) {
	//			sum += WhoToAskProb[i];
	//		}
	//		pick = (int) Village.uniformDblRand(0.0, sum);
	//
	//		sum = (int) WhoToAskProb[0];
	//		whotoask = 0;
	//		while (pick > sum) {
	//			whotoask++;
	//			sum += WhoToAskProb[whotoask];
	//		}
	//
	//		if (whotoask == -1) {
	//			whotoaskID = -1;
	//		} else if (whotoask == 0) // ParentA
	//		{
	//			whotoaskID = ParentHHTagA;
	//		} else if (whotoask == 1) // ParentB
	//		{
	//			whotoaskID = ParentHHTagB;
	//		} else if (whotoask > 1 && whotoask < 2 + ChildHHCount) // Child
	//		{
	//			whotoaskID = ChildHHTag[whotoask - 2];
	//		} else // ask a relative
	//		{
	//			whotoaskID = RelativeHHTag[whotoask - 2 - ChildHHCount];
	//		}
	//
	//		if (whotoaskID != -1) {
	//			coopAgent = searchAgentList(whotoaskID);
	//			dx = coopAgent.getX();
	//			dy = coopAgent.getY();
	//		}
	//
	//		for (dy = -radius; dy < radius; dy++) {
	//			for (dx = -radius; dx < radius; dx++) {
	//				k = evalCellX(dx, dy, max, caller);
	//				if (k != 0) {
	//					max = k;
	//					a = dx;
	//					b = dy;
	//				}
	//			}
	//		}
	//
	//		return new int[] { a, b };
	//	}

	public void setC_fuel(double c_fuel2) {
		C_fuel = c_fuel2;			
	}


	public void setColor(Color c) {
		myColor = c;
	}

	// Extra bits of display code: setting our color, drawing on a window.
	// This code works, but it'd be better if there were a generic object
	// that knew how to draw agents on grids.
	public void setColor(int c) {
		myColor = ObserverAgentModel.getColorMap().getColor(c);
	}

	public void setCurrentProteinStorage(double d) {
		setCurrentProteinStorage((int) d);	
	}

	public void setCurrentProteinStorage(int current_protein_storage) {
		this.current_protein_storage = current_protein_storage;
	}

	public void setDeathCounter() // scheduled in AgentModelSwarm as first
	// action in each year for each agent
	{
		if (Village.DEBUG) {
			System.out.printf("begin Death Counter\n");
		}

		storage1 = 0;
		production1 = 0;

		birthcalls = 0;
		parentp = 0;
		kidp = 0;
		birthp = 0;
		successfulbirths = 0;
		m2 = 0;
		sm2 = 0;
		protein_deficient = 0;

		if (agentpop != 0) {
			oldagentpop = agentpop;
		}

		agentpop = 0;
		return;
	}

	public void setFamilyTag(int t) {
		family_tag = t;
		return;
	}

	public void setFarmPl(int plot, int i) {
		farm_pl[i] = plot;
		return;
	}

	public void setFormationDate(int date) {
		form_date = date;
		return;
	}

	protected void setMaizeMaxStore(int max_store) {		
		this.max_store = max_store;
	}

	public void setMaizeStorage(int food) {
		maize_storage = food;

		update_state();
		return;
	}

	public void setMySwarm(AgentModelSwarm s) {
		mySwarm = s;
		return;
	}

//	public void setParentHHLinksWife(int wifehh, int husbandhh) {
//		ParentHHTagA = wifehh;
//		ParentHHTagB = husbandhh;
//		return;
//	}


	public void setProteinMaxStore(int protein_max_store) {
		this.protein_max_store = protein_max_store;
	}

	public double getDeer_time() {
		return deer_time;
	}

	public void setDeer_time(double deer_time) {
		this.deer_time = deer_time;
	}

	public double getDeer_return_rate() {
		return deer_return_rate;
	}

	public void setDeer_return_rate(double deer_return_rate) {
		this.deer_return_rate = deer_return_rate;
	}

	public double getDeer_distance() {
		return deer_distance;
	}

	public void setDeer_distance(double deer_distance) {
		this.deer_distance = deer_distance;
	}

	public double getHare_time() {
		return hare_time;
	}

	public void setHare_time(double hare_time) {
		this.hare_time = hare_time;
	}

	public double getHare_return_rate() {
		return hare_return_rate;
	}

	public void setHare_return_rate(double hare_return_rate) {
		this.hare_return_rate = hare_return_rate;
	}

	public double getRabbit_time() {
		return rabbit_time;
	}

	public void setRabbit_time(double rabbit_time) {
		this.rabbit_time = rabbit_time;
	}

	public double getRabbit_return_rate() {
		return rabbit_return_rate;
	}

	public void setRabbit_return_rate(double rabbit_return_rate) {
		this.rabbit_return_rate = rabbit_return_rate;
	}

	public int getDeer_protein_storage() {
		return deer_protein_storage;
	}

	public void setDeer_protein_storage(int deer_protein_storage) {
		this.deer_protein_storage = deer_protein_storage;
	}

	public int getHare_protein_storage() {
		return hare_protein_storage;
	}

	public void setHare_protein_storage(int hare_protein_storage) {
		this.hare_protein_storage = hare_protein_storage;
	}

	public int getRabbit_protein_storage() {
		return rabbit_protein_storage;
	}

	public void setRabbit_protein_storage(int rabbit_protein_storage) {
		this.rabbit_protein_storage = rabbit_protein_storage;
	}

	public int getDeer_hunted() {
		return deer_hunted;
	}

	public void setDeer_hunted(int deer_hunted) {
		this.deer_hunted = deer_hunted;
	}

	public int getHare_hunted() {
		return hare_hunted;
	}

	public void setHare_hunted(int hare_hunted) {
		this.hare_hunted = hare_hunted;
	}

	public int getRabbit_hunted() {
		return rabbit_hunted;
	}

	public void setRabbit_hunted(int rabbit_hunted) {
		this.rabbit_hunted = rabbit_hunted;
	}

	public double getNonhunt_hrs() {
		return nonhunt_hrs;
	}

	public void setNonhunt_hrs(double nonhunt_hrs) {
		this.nonhunt_hrs = nonhunt_hrs;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getChild_per_female() {
		return child_per_female;
	}

	public void setChild_per_female(int child_per_female) {
		this.child_per_female = child_per_female;
	}

	public int getLast_year_child_born() {
		return last_year_child_born;
	}

	public void setLast_year_child_born(int last_year_child_born) {
		this.last_year_child_born = last_year_child_born;
	}

	public Color getMyColor() {
		return myColor;
	}

	public void setMyColor(Color myColor) {
		this.myColor = myColor;
	}

	public int getForm_date() {
		return form_date;
	}

	public void setForm_date(int form_date) {
		this.form_date = form_date;
	}

	public int getFamily_tag() {
		return family_tag;
	}

	public void setFamily_tag(int family_tag) {
		this.family_tag = family_tag;
	}

	public double getSeason_fam() {
		return season_fam;
	}

	public void setSeason_fam(double season_fam) {
		this.season_fam = season_fam;
	}

	public int getLast_move() {
		return last_move;
	}

	public void setLast_move(int last_move) {
		this.last_move = last_move;
	}

	public double getPlanting() {
		return planting;
	}

	public void setPlanting(double planting) {
		this.planting = planting;
	}

	public double getWeeding() {
		return weeding;
	}

	public void setWeeding(double weeding) {
		this.weeding = weeding;
	}

	public double getHarvesting() {
		return harvesting;
	}

	public void setHarvesting(double harvesting) {
		this.harvesting = harvesting;
	}

	public double getMonitoring() {
		return monitoring;
	}

	public void setMonitoring(double monitoring) {
		this.monitoring = monitoring;
	}

	public double getPlotwork() {
		return plotwork;
	}

	public void setPlotwork(double plotwork) {
		this.plotwork = plotwork;
	}

	public double getC_water() {
		return C_water;
	}

	public void setC_water(double c_water) {
		C_water = c_water;
	}

	public double getTot_hrs() {
		return tot_hrs;
	}

	public void setTot_hrs(double tot_hrs) {
		this.tot_hrs = tot_hrs;
	}

	public double getField_hrs() {
		return field_hrs;
	}

	public void setField_hrs(double field_hrs) {
		this.field_hrs = field_hrs;
	}

	public int getMaize_storage() {
		return maize_storage;
	}

	public void setMaize_storage(int maize_storage) {
		this.maize_storage = maize_storage;
	}

	public int[] getFarm_pl() {
		return farm_pl;
	}

	public void setFarm_pl(int[] farm_pl) {
		this.farm_pl = farm_pl;
	}

	public int getTot_plots() {
		return tot_plots;
	}

	public void setTot_plots(int tot_plots) {
		this.tot_plots = tot_plots;
	}

	public int getPlot_need() {
		return plot_need;
	}

	public void setPlot_need(int plot_need) {
		this.plot_need = plot_need;
	}

	public int getMax_store() {
		return max_store;
	}

	public void setMax_store(int max_store) {
		this.max_store = max_store;
	}

	public int getCurrent_protein_storage() {
		return current_protein_storage;
	}

	public void setCurrent_protein_storage(int current_protein_storage) {
		this.current_protein_storage = current_protein_storage;
	}

	public int getProtein_tot_cells() {
		return protein_tot_cells;
	}

	public void setProtein_tot_cells(int protein_tot_cells) {
		this.protein_tot_cells = protein_tot_cells;
	}

	public int getProtein_max_store() {
		return protein_max_store;
	}

	public void setProtein_max_store(int protein_max_store) {
		this.protein_max_store = protein_max_store;
	}

	public double getExpected_protein_yield_per_cell() {
		return expected_protein_yield_per_cell;
	}

	public void setExpected_protein_yield_per_cell(
			double expected_protein_yield_per_cell) {
		this.expected_protein_yield_per_cell = expected_protein_yield_per_cell;
	}

	public int getNum_cells_hunted() {
		return num_cells_hunted;
	}

	public void setNum_cells_hunted(int num_cells_hunted) {
		this.num_cells_hunted = num_cells_hunted;
	}

	public double getTotal_protein_yield_all_cells() {
		return total_protein_yield_all_cells;
	}

	public void setTotal_protein_yield_all_cells(
			double total_protein_yield_all_cells) {
		this.total_protein_yield_all_cells = total_protein_yield_all_cells;
	}

	public int getHunting_radius() {
		return hunting_radius;
	}

	public void setHunting_radius(int hunting_radius) {
		this.hunting_radius = hunting_radius;
	}

	public int getP_need() {
		return p_need;
	}

	public void setP_need(int p_need) {
		this.p_need = p_need;
	}

	public int getProtein_deficient() {
		return protein_deficient;
	}

	public void setProtein_deficient(int protein_deficient) {
		this.protein_deficient = protein_deficient;
	}

	public int getYears_deficient() {
		return years_deficient;
	}

	public void setYears_deficient(int years_deficient) {
		this.years_deficient = years_deficient;
	}

	public int getTurkey_kept() {
		return turkey_kept;
	}

	public void setTurkey_kept(int turkey_kept) {
		this.turkey_kept = turkey_kept;
	}

	public int getH_Plots() {
		return H_Plots;
	}

	public void setH_Plots(int h_Plots) {
		H_Plots = h_Plots;
	}

	public int getA_Plots() {
		return A_Plots;
	}

	public void setA_Plots(int a_Plots) {
		A_Plots = a_Plots;
	}

	public int getEXP_cal_need() {
		return EXP_cal_need;
	}

	public void setEXP_cal_need(int eXP_cal_need) {
		EXP_cal_need = eXP_cal_need;
	}

	public int getAVG_cal_prod() {
		return AVG_cal_prod;
	}

	public void setAVG_cal_prod(int aVG_cal_prod) {
		AVG_cal_prod = aVG_cal_prod;
	}

	public int getEXP_yield() {
		return EXP_yield;
	}

	public void setEXP_yield(int eXP_yield) {
		EXP_yield = eXP_yield;
	}

	public int getAct_yield() {
		return act_yield;
	}

	public void setAct_yield(int act_yield) {
		this.act_yield = act_yield;
	}

	public int[][] getPast_yield() {
		return past_yield;
	}

	public void setPast_yield(int[][] past_yield) {
		this.past_yield = past_yield;
	}

	public int getActual_cal() {
		return actual_cal;
	}

	public void setActual_cal(int actual_cal) {
		this.actual_cal = actual_cal;
	}

	public int getTotal_cal() {
		return total_cal;
	}

	public void setTotal_cal(int total_cal) {
		this.total_cal = total_cal;
	}

	public int getCal_Produced() {
		return Cal_Produced;
	}

	public void setCal_Produced(int cal_Produced) {
		Cal_Produced = cal_Produced;
	}

	public int getWater_Cost() {
		return Water_Cost;
	}

	public void setWater_Cost(int water_Cost) {
		Water_Cost = water_Cost;
	}

	public int getFuel_Cost() {
		return Fuel_Cost;
	}

	public void setFuel_Cost(int fuel_Cost) {
		Fuel_Cost = fuel_Cost;
	}

	public int getFood_Cost() {
		return Food_Cost;
	}

	public void setFood_Cost(int food_Cost) {
		Food_Cost = food_Cost;
	}

	public int getAg_Cost() {
		return Ag_Cost;
	}

	public void setAg_Cost(int ag_Cost) {
		Ag_Cost = ag_Cost;
	}

	public int getProtein_Cost() {
		return Protein_Cost;
	}

	public void setProtein_Cost(int protein_Cost) {
		Protein_Cost = protein_Cost;
	}

	public int getFW_need() {
		return FW_need;
	}

	public void setFW_need(int fW_need) {
		FW_need = fW_need;
	}

	public int getFWsearchradius() {
		return FWsearchradius;
	}

	public void setFWsearchradius(int fWsearchradius) {
		FWsearchradius = fWsearchradius;
	}

	public int getFwsr() {
		return fwsr;
	}

	public void setFwsr(int fwsr) {
		this.fwsr = fwsr;
	}

	public int getWtsr() {
		return wtsr;
	}

	public void setWtsr(int wtsr) {
		this.wtsr = wtsr;
	}

	public int getW_need() {
		return W_need;
	}

	public void setW_need(int w_need) {
		W_need = w_need;
	}

	public int getWsearchradius() {
		return Wsearchradius;
	}

	public void setWsearchradius(int wsearchradius) {
		Wsearchradius = wsearchradius;
	}

	public int getHsearchradius() {
		return Hsearchradius;
	}

	public void setHsearchradius(int hsearchradius) {
		Hsearchradius = hsearchradius;
	}

	public int getFWHappy() {
		return FWHappy;
	}

	public void setFWHappy(int fWHappy) {
		FWHappy = fWHappy;
	}

	public int getOutsideFlag() {
		return OutsideFlag;
	}

	public void setOutsideFlag(int outsideFlag) {
		OutsideFlag = outsideFlag;
	}

	public int getOutsideCellX() {
		return OutsideCellX;
	}

	public void setOutsideCellX(int outsideCellX) {
		OutsideCellX = outsideCellX;
	}

	public int getOutsideCellY() {
		return OutsideCellY;
	}

	public void setOutsideCellY(int outsideCellY) {
		OutsideCellY = outsideCellY;
	}

	public int getMove_trigger() {
		return move_trigger;
	}

	public void setMove_trigger(int move_trigger) {
		this.move_trigger = move_trigger;
	}

	public int getExchange_count() {
		return exchange_count;
	}

	public void setExchange_count(int exchange_count) {
		this.exchange_count = exchange_count;
	}

	public int getMaize_coop_count() {
		return maize_coop_count;
	}

	public void setMaize_coop_count(int maize_coop_count) {
		this.maize_coop_count = maize_coop_count;
	}

	public int getProtein_coop_count() {
		return protein_coop_count;
	}

	public void setProtein_coop_count(int protein_coop_count) {
		this.protein_coop_count = protein_coop_count;
	}

	public int getTotalMaizeExchanged() {
		return totalMaizeExchanged;
	}

	public void setTotalMaizeExchanged(int totalMaizeExchanged) {
		this.totalMaizeExchanged = totalMaizeExchanged;
	}

	public int getSuccessful_exchange() {
		return successful_exchange;
	}

	public void setSuccessful_exchange(int successful_exchange) {
		this.successful_exchange = successful_exchange;
	}

	public int getExchange_requests() {
		return exchange_requests;
	}

	public void setExchange_requests(int exchange_requests) {
		this.exchange_requests = exchange_requests;
	}

	public int getMaize_requests() {
		return maize_requests;
	}

	public void setMaize_requests(int maize_requests) {
		this.maize_requests = maize_requests;
	}

	public int getMaize_exchanged() {
		return maize_exchanged;
	}

	public void setMaize_exchanged(int maize_exchanged) {
		this.maize_exchanged = maize_exchanged;
	}

	public int getMaize_wasted() {
		return maize_wasted;
	}

	public void setMaize_wasted(int maize_wasted) {
		this.maize_wasted = maize_wasted;
	}

	public int getMaize_given() {
		return maize_given;
	}

	public void setMaize_given(int maize_given) {
		this.maize_given = maize_given;
	}

	public int getProtein_given() {
		return protein_given;
	}

	public void setProtein_given(int protein_given) {
		this.protein_given = protein_given;
	}

	public int getPro_donater() {
		return pro_donater;
	}

	public void setPro_donater(int pro_donater) {
		this.pro_donater = pro_donater;
	}

	public int getMaize_imported() {
		return maize_imported;
	}

	public void setMaize_imported(int maize_imported) {
		this.maize_imported = maize_imported;
	}

	public int getProtein_imported() {
		return protein_imported;
	}

	public void setProtein_imported(int protein_imported) {
		this.protein_imported = protein_imported;
	}

	public int getRemarriage_count() {
		return remarriage_count;
	}

	public void setRemarriage_count(int remarriage_count) {
		this.remarriage_count = remarriage_count;
	}

	public int getRemarriage_attempts() {
		return remarriage_attempts;
	}

	public void setRemarriage_attempts(int remarriage_attempts) {
		this.remarriage_attempts = remarriage_attempts;
	}

	public int getParentHHTagA() {
		return ParentHHTagA;
	}

	public void setParentHHTagA(int parentHHTagA) {
		ParentHHTagA = parentHHTagA;
	}

	public int getParentHHTagB() {
		return ParentHHTagB;
	}

	public void setParentHHTagB(int parentHHTagB) {
		ParentHHTagB = parentHHTagB;
	}

	public int[] getChildHHTag() {
		return ChildHHTag;
	}

	public void setChildHHTag(int[] childHHTag) {
		ChildHHTag = childHHTag;
	}

	public int getChildHHCount() {
		return ChildHHCount;
	}

	public void setChildHHCount(int childHHCount) {
		ChildHHCount = childHHCount;
	}

	public int[] getRelativeHHTag() {
		return RelativeHHTag;
	}

	public void setRelativeHHTag(int[] relativeHHTag) {
		RelativeHHTag = relativeHHTag;
	}

	public int getRelativeHHCount() {
		return RelativeHHCount;
	}

	public void setRelativeHHCount(int relativeHHCount) {
		RelativeHHCount = relativeHHCount;
	}

	public int getNextpick() {
		return nextpick;
	}

	public void setNextpick(int nextpick) {
		this.nextpick = nextpick;
	}

	public int getCoop_state() {
		return coop_state;
	}

	public void setCoop_state(int coop_state) {
		this.coop_state = coop_state;
	}

	public int getProtein_coop_state() {
		return protein_coop_state;
	}

	public void setProtein_coop_state(int protein_coop_state) {
		this.protein_coop_state = protein_coop_state;
	}

	public int getLast_ration() {
		return last_ration;
	}

	public void setLast_ration(int last_ration) {
		this.last_ration = last_ration;
	}

	public int getLast_protein_ration() {
		return last_protein_ration;
	}

	public void setLast_protein_ration(int last_protein_ration) {
		this.last_protein_ration = last_protein_ration;
	}

	public int[] getMemoryInteraction_ID() {
		return MemoryInteraction_ID;
	}

	public void setMemoryInteraction_ID(int[] memoryInteraction_ID) {
		MemoryInteraction_ID = memoryInteraction_ID;
	}

	public int[] getMemoryInteraction_Index() {
		return MemoryInteraction_Index;
	}

	public void setMemoryInteraction_Index(int[] memoryInteraction_Index) {
		MemoryInteraction_Index = memoryInteraction_Index;
	}

	public int[] getMemoryInteraction_X() {
		return MemoryInteraction_X;
	}

	public void setMemoryInteraction_X(int[] memoryInteraction_X) {
		MemoryInteraction_X = memoryInteraction_X;
	}

	public int[] getMemoryInteraction_Y() {
		return MemoryInteraction_Y;
	}

	public void setMemoryInteraction_Y(int[] memoryInteraction_Y) {
		MemoryInteraction_Y = memoryInteraction_Y;
	}

	public int getMemoryInteractionCount() {
		return MemoryInteractionCount;
	}

	public void setMemoryInteractionCount(int memoryInteractionCount) {
		MemoryInteractionCount = memoryInteractionCount;
	}

	public int getDefectingAgentFlag() {
		return DefectingAgentFlag;
	}

	public void setDefectingAgentFlag(int defectingAgentFlag) {
		DefectingAgentFlag = defectingAgentFlag;
	}

	public int getHunt_test_penalty() {
		return hunt_test_penalty;
	}

	public void setHunt_test_penalty(int hunt_test_penalty) {
		this.hunt_test_penalty = hunt_test_penalty;
	}

	public HuntingStrategy getHuntingStrategy() {
		return huntingStrategy;
	}

	public void setHuntingStrategy(HuntingStrategy huntingStrategy) {
		this.huntingStrategy = huntingStrategy;
	}

	public DomesticationStrategy getDomesticationStrategy() {
		return domesticationStrategy;
	}

	public void setDomesticationStrategy(DomesticationStrategy domesticationStrategy) {
		this.domesticationStrategy = domesticationStrategy;
	}

	public GRNExchangeNetwork getGrnNetwork() {
		return grnNetwork;
	}

	public void setGrnNetwork(GRNExchangeNetwork grnNetwork) {
		this.grnNetwork = grnNetwork;
	}

	public BRNExchangeNetwork getBrnNetwork() {
		return brnNetwork;
	}

	public void setBrnNetwork(BRNExchangeNetwork brnNetwork) {
		this.brnNetwork = brnNetwork;
	}

	public DonationExchangeNetwork getDonationNetwork() {
		return donationNetwork;
	}

	public void setDonationNetwork(DonationExchangeNetwork donationNetwork) {
		this.donationNetwork = donationNetwork;
	}

	public static double[] getBirth_per() {
		return birth_per;
	}

	public static void setBirth_per(double[] birth_per) {
		Agent.birth_per = birth_per;
	}

	public static double[] getDeath_per() {
		return death_per;
	}

	public static void setDeath_per(double[] death_per) {
		Agent.death_per = death_per;
	}

	public static int getTot_pop() {
		return tot_pop;
	}

	public static void setTot_pop(int tot_pop) {
		Agent.tot_pop = tot_pop;
	}

	public static int getPop_id() {
		return pop_id;
	}

	public static void setPop_id(int pop_id) {
		Agent.pop_id = pop_id;
	}

	public static double getOldagentpop() {
		return oldagentpop;
	}

	public static void setOldagentpop(double oldagentpop) {
		Agent.oldagentpop = oldagentpop;
	}

	public static double getAgentpop() {
		return agentpop;
	}

	public static void setAgentpop(double agentpop) {
		Agent.agentpop = agentpop;
	}

	public static int getDefectingAgentCount() {
		return DefectingAgentCount;
	}

	public static void setDefectingAgentCount(int defectingAgentCount) {
		DefectingAgentCount = defectingAgentCount;
	}

	public static int getDefectingAgentTurnover() {
		return DefectingAgentTurnover;
	}

	public static void setDefectingAgentTurnover(int defectingAgentTurnover) {
		DefectingAgentTurnover = defectingAgentTurnover;
	}

	public static int getStorage1() {
		return storage1;
	}

	public static void setStorage1(int storage1) {
		Agent.storage1 = storage1;
	}

	public static int getProduction1() {
		return production1;
	}

	public static void setProduction1(int production1) {
		Agent.production1 = production1;
	}

	public static int getFdeaths() {
		return fdeaths;
	}

	public static void setFdeaths(int fdeaths) {
		Agent.fdeaths = fdeaths;
	}

	public static int getTotalage() {
		return totalage;
	}

	public static void setTotalage(int totalage) {
		Agent.totalage = totalage;
	}

	public static int getFkids() {
		return fkids;
	}

	public static void setFkids(int fkids) {
		Agent.fkids = fkids;
	}

	public static int getTotalkids() {
		return totalkids;
	}

	public static void setTotalkids(int totalkids) {
		Agent.totalkids = totalkids;
	}

	public static int getBirthcalls() {
		return birthcalls;
	}

	public static void setBirthcalls(int birthcalls) {
		Agent.birthcalls = birthcalls;
	}

	public static int getParentp() {
		return parentp;
	}

	public static void setParentp(int parentp) {
		Agent.parentp = parentp;
	}

	public static int getKidp() {
		return kidp;
	}

	public static void setKidp(int kidp) {
		Agent.kidp = kidp;
	}

	public static int getBirthp() {
		return birthp;
	}

	public static void setBirthp(int birthp) {
		Agent.birthp = birthp;
	}

	public static int getSuccessfulbirths() {
		return successfulbirths;
	}

	public static void setSuccessfulbirths(int successfulbirths) {
		Agent.successfulbirths = successfulbirths;
	}

	public static int getM2() {
		return m2;
	}

	public static void setM2(int m2) {
		Agent.m2 = m2;
	}

	public static int getSm2() {
		return sm2;
	}

	public static void setSm2(int sm2) {
		Agent.sm2 = sm2;
	}

	public static long getG_countof_move_radius() {
		return g_countof_move_radius;
	}

	public static void setG_countof_move_radius(long g_countof_move_radius) {
		Agent.g_countof_move_radius = g_countof_move_radius;
	}

	public static double getG_average_move_radius() {
		return g_average_move_radius;
	}

	public static void setG_average_move_radius(double g_average_move_radius) {
		Agent.g_average_move_radius = g_average_move_radius;
	}

	public static int getTerm_error() {
		return term_error;
	}

	public static void setTerm_error(int term_error) {
		Agent.term_error = term_error;
	}

	public static int getTAGS() {
		return TAGS;
	}

	public static void setTAGS(int tAGS) {
		TAGS = tAGS;
	}

	public AgentModelSwarm getMySwarm() {
		return mySwarm;
	}

	public double getC_fuel() {
		return C_fuel;
	}

	public double[] getWhoToAskProb() {
		return WhoToAskProb;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void setWorldX(int worldX) {
		this.worldX = worldX;
	}

	public void setWorldY(int worldY) {
		this.worldY = worldY;
	}

	public void setCell(Cell[] cell) {
		this.cell = cell;
	}

	public void setHrs(double hrs) {
		this.hrs = hrs;
	}

	public void setNumBirths(int numBirths) {
		this.numBirths = numBirths;
	}

	public void setNumDeaths(int numDeaths) {
		this.numDeaths = numDeaths;
	}

	public void setState(int state) {
		this.state = state;
	}

	public void setFWout(int fWout) {
		FWout = fWout;
	}

	public void setDonater(int donater) {
		this.donater = donater;
	}

	public void setFamilyUnit(FamilyUnit familyUnit) {
		this.familyUnit = familyUnit;
	}

	// num in following set to b (=ages of parents/8)
	// in AgentModelSwarm.m void initWorld()
	/** Adds num kids of random age (0-15) to the family */
	public void setRandNumKids(int num) {
		int i;

		for (i = 0; i < num; i++) {
			int age = Village.uniformIntRand(0, 15);
			int gender = Village.uniformIntRand(0, 1);
			int tag = pop_id;

			familyUnit.addPerson(age, gender, tag);						
			pop_id++;			
		}

		tot_pop = tot_pop + num;

		return;
	}
	

	// **** initialization ****
	public void setTag(int num) {
		tag = num;
		return;
	}

	private void registerEligibleForMarriage(Individual ind) {
		// note: number of eligible bachelors in
		// household is not used (set to 1 for now)
		ind.eligibleRecord = Eligible.addToEligibleListA(tag, this, 1, ind, x, y, cell[5], ind.getAge(), ind.getMarriageStatus(), ind.getGender(), ind.getTag());
	}

	private void setupNetworks() {
		if (Village.COOP >= 4)
			brnNetwork = new BRNExchangeNetwork(this);

		if (Village.COOP >= 1) {
			grnNetwork = new GRNExchangeNetwork(this);
			donationNetwork = new DonationExchangeNetwork(this);
		}
	}



	public void setWhoToAskProb(double[] A) {
		int i;
		for (i = 0; i < 2 + Village.MAX_CHILD_LINKS
				+ Village.MAX_RELATIVE_LINKS; i++) {
			A[i] = WhoToAskProb[i];
		}

		return;
	}



	public void setWhoToAskProb_Memory(double[] A) {
		if (MemoryInteractionCount > 0) {
			A[MemoryInteraction_Index[0]] += 0.1;
		}
		return;
	}

	public void setWorld(VillageSpace w) {
		world = w;
		// cache locally the world size for later use
		worldX = world.getSizeX();
		worldY = world.getSizeY();
		return;
	}



	// called by -step_procure_conclude at end of each year
	// decrements maize storage, updates state, handles births, updates family,
	// and runs -evalState
	public void step_assess() {
		setMaizeStorage(getMaizeStorage() - ((int) (Village.MAIZE_STORAGE_DECR * getMaizeStorage()))); // spoilage
		update_state();

		// calculate annual birth
		if (state > -1) {
			birth(familyUnit.getWifesAge());
		}
		if (Village.DEBUG) {
			System.out.printf("state is %d\n", state);
		}
		if (state > -1) {
			updateFamily(); // execute family happenings
		}

		if (Village.DEBUG) {
			System.out.printf("state after update family\n");
		}
		if (state > -1) {
			evalState(); // how are we doing
		}
		return;
	}

	public void step_interact() {
		int trade = this.getMySwarm().getEconomy();
		int init_maize_storage, init_protein_storage;

		if (Village.DEBUG) {
			System.out.printf("begin step interact\n");
		}

		if (state == -1) {
			return;
		}

		update_state();

		// Philanthropic model
		if ((trade == 2 || trade == 3 || trade >= 4)
				&& !Village.ENABLE_DEFECTING_AGENT && donationNetwork != null) {
			if ((init_maize_storage = getMaizeStorage()) > Village.PHIL_THRESHHOLD(getMaizeMaxStore())
					&& (Village.TRIGGER_DONATE_MAIZE() & getCoopState()) != 0) {
				// donate takes how much kg to give away, and returns the actual
				// amount donated which in turn is deducted from the current
				// maize_storage
				int don = 0;
				don = donationNetwork
						.donateMaize((int) (init_maize_storage - Village
								.PHIL_THRESHHOLD(getMaizeMaxStore())));
				setMaizeStorage(init_maize_storage - don);

				update_state();
			}

			if ((init_protein_storage = getCurrentProteinStorage()) > Village
					.PROTEIN_PHIL_THRESHHOLD(getProteinMaxStore())
					&& (Village.TRIGGER_DONATE_PRO() & getProteinCoopState()) != 0) {
				// donate takes how much kg to give away, and returns the actual
				// amount donated which in turn is deducted from the current
				// protein_storage
				decreaseCurrentProteinStorage(donationNetwork.donateProtein((int) (init_protein_storage - Village.PROTEIN_PHIL_THRESHHOLD(getProteinMaxStore()))));

				update_state();
			}
		}

		if (trade > 0 && grnNetwork != null) {
			grnNetwork.saveLinks();
		}
		if (trade >= 4 && brnNetwork != null) {
			brnNetwork.saveLinks();
		}
		if (Village.LOG_FOOD) {
			saveFood();
		}

		// Village.ACCEPTANCE INTO Village.BELIEF Village.SPACE
		if (Village.CA_ENABLE) {
			if (Village.uniformDblRand(0.0, 1.0) < Village.ACCEPTANCE_PROB) {
				BeliefSpace.Accept(this);
			}
		}

		if (Village.DEBUG) {
			System.out.printf("end step interact\n");
		}
		return;
	}

	// scheduled by modelActions after -step_procure_winter
	// and just before -step_move (which is for social moves and inactive at the
	// moment), -diffuseDeer, & -removeAgents
	public void step_procure_conclude() {
		season_fam = season_fam / 4;
		if (season_fam == 0) {
			season_fam = 1;
		}
		tot_hrs = planting + weeding + harvesting + plotwork + monitoring
				+ C_fuel + C_water + huntingStrategy.getC_hunt();
		field_hrs = (planting + weeding + harvesting + monitoring) / tot_plots; // hours
		// per
		// plot
		nonhunt_hrs = tot_hrs - huntingStrategy.getC_hunt();
		hrs = tot_hrs / (season_fam * 365); // hours/worker/day

		step_assess(); // decrements maize storage, handles births, updates
		// family, and runs -evalState
		step_interact(); // gets COOP (economy), runs update_state, if COOP GE 2
		// then runs Philanthropic model
		// (donations) in maize & protein, saves links, possibly logs food.

		if (Village.ENABLE_DEFECTING_AGENT) {
			if (DefectingAgentFlag != 0) {
				saveDefector();
			}
		}

		// end the year

		last_move++;

		// this code prints out a warning if the agent is spending more than 16
		// hours per day per household member for the entire year in work
		// season_fam contains average number of workers a family has over the
		// course of one year

		if (hrs > 14) {
			printTooMuchWorkError();			
		}

		agentpop++;
		if (Village.DEBUG) {
			System.out.printf("end step procure\n");
		}
	}

	// Village.AGENT Village.ACTIONS WITH Village.CONCURRENCY AND Village.STATE
	// Village.MODEL Village.APPROACH
	public void step_procure_init() {

		if (Village.DEBUG) {
			System.out.printf("begin step procure\n");
		}
		// begin the year
		time++;
		setAgent_time(getAgent_time() + 1); // another agent year passed

		if (brnNetwork != null)
			brnNetwork.setTListUpdateDead(1);
		Cal_Produced = 0;
		Water_Cost = 0;
		Fuel_Cost = 0;
		Food_Cost = 0;
		Ag_Cost = 0;
		Protein_Cost = 0;
		total_cal = 0; // cals spent during year
		numBirths = 0;
		numDeaths = 0;
		arrivingNewlyweds = 0; // number of births per agent, per year
		departingNewlyweds = 0; // number of deaths per agent, per year
		beginSize = this.getFamilySize(); // size at start of year

		// used for tracking hours spent by agent
		hrs = 0;
		planting = 0;
		weeding = 0;
		harvesting = 0;
		//traveling = 0;
		plotwork = 0;
		monitoring = 0;
		C_fuel = 0; // caloric cost in hours, since it's Cal/caloric increment
		// per hour work for women (Village.WORK_CAL_WOM)
		C_water = 0; // caloric cost in hours, since it's Cal/caloric increment
		// per hour work for women (Village.WORK_CAL_WOM)

		deer_return_rate = -999;
		hare_return_rate = -999;
		rabbit_return_rate = -999;
		hunting_return_rate = -999;

		hunting_protein = 0;
		domestication_protein = 0;
		trading_protein_GRN = 0;
		trading_protein_BRN = 0;
		total_protein = 0;

		hunting_protein_proportion = 0.0;
		domestication_protein_proportion = 0.0;
		trading_protein_GRN_proportion = 0.0;
		trading_protein_BRN_proportion = 0.0;

		// variables used for tracking exchange
		successful_exchange = 0; // maize
		exchange_requests = 0; // counts not kg
		maize_requests = 0; // in KG
		maize_exchanged = 0; // in KG
		maize_wasted = 0; // Amount of maize exchanged and the agent dies anyway
		// at the end of the year
		totalMaizeExchanged = 0;

		if (brnNetwork != null)
			brnNetwork.reset();

		if (grnNetwork != null)
			grnNetwork.reset();

		// reset imported food counters
		maize_imported = 0; // represents all maize recieved by an agent during
		// the year (BRN+GRN+PHIL)
		protein_imported = 0; // represents all protein recieved by an agent
		// during the year (BRN+GRN+PHIL)

		maize_given = 0;
		protein_given = 0;

		donater = 0;
		pro_donater = 0;

		maize_coop_count = 0;
		protein_coop_count = 0;

		// INFLUENCE FROM BELIEF SPACE
		if (Village.CA_ENABLE) {
			if (Village.uniformDblRand(0.0, 1.0) < Village.INFLUENCE_PROB) {
				BeliefSpace.Influence(this);
			}
		}

		//		step_procure_protein();

		return;
	}

	public int step_procure_protein() {
		//		System.out.printf("Begin step_procure_protein\n");

		long hh_protein_need = 0; // will store annual protein need in g per
		// household using input from .scm or
		// Village.h		
		protein_coop_count = 0;
		int hunting_costs = 0;
		int domestication_costs = 0;

		setHunting_protein_costs_proportion(0);
		setDomestication_protein_costs_proportion(0);

		int Protein_Need = this.getMySwarm().getProteinNeed(); // In grams of protein
		int hunt_radius = this.getMySwarm().getHuntingRadius();

		// calculate protein need for hh per year to survive
		hh_protein_need = Protein_Need * getFamilySize() * 365; // 2 adults 12g + 4
		// children avg 6g /
		// 6 = 8g
		//		System.out.printf("hh_protein_need total: %d\n", hh_protein_need);

		setCurrentProteinStorage((int) (getCurrentProteinStorage() * Village.PROTEIN_STORAGE_DECR));

		//		System.out.printf("current_protein_storage = %d\n", getCurrentProteinStorage());

		protein_deficient = 0; // will switch to one if not enough protein is
		// consumed

		hh_protein_need -= getCurrentProteinStorage();
		setCurrentProteinStorage(0);
		//		System.out.printf("hh_protein_need after eating storage: %d\n", hh_protein_need);

		hunting_costs = huntingStrategy.execute(hh_protein_need, hunt_radius);
		hh_protein_need -= getCurrentProteinStorage();
		hunting_protein += getCurrentProteinStorage();
		setCurrentProteinStorage(0);
		//		System.out.printf("hunting_protein: %d\n", hunting_protein);
		//		System.out.printf("hh_protein_need after hunting: %d\n", hh_protein_need);

		domestication_costs = domesticationStrategy.execute(hh_protein_need);
		hh_protein_need -= getCurrentProteinStorage();
		domestication_protein += getCurrentProteinStorage();
		setCurrentProteinStorage(0);
		//		System.out.printf("domestication_protein: %d\n", domestication_protein);
		//		System.out.printf("hh_protein_need after domestication: %d\n", hh_protein_need);

		tradeProtein((int)hh_protein_need);
		//		System.out.printf("hh_protein_need after trading: %d\n", hh_protein_need);

		total_protein += (hunting_protein + domestication_protein + trading_protein_GRN + trading_protein_BRN);
		//		System.out.printf("total_protein: %d\n", total_protein);

		setCurrentProteinStorage(-hh_protein_need);

		//		if (getCurrentProteinStorage() < 0){
		//			setCurrentProteinStorage(0);
		//		}

		//		System.out.printf("current_protein_storage = %d\n", getCurrentProteinStorage());

		proteinPenalty();
		//		System.out.printf("protein_deficient = %d\n", protein_deficient);

		if (total_protein != 0) {
			hunting_protein_proportion = (double)hunting_protein/(double)total_protein;
			domestication_protein_proportion = (double)domestication_protein/(double)total_protein;
			trading_protein_GRN_proportion = (double)trading_protein_GRN/(double)total_protein;
			trading_protein_BRN_proportion = (double)trading_protein_BRN/(double)total_protein;

			//			System.out.printf("hunting_protein_proportion = %f\n", hunting_protein_proportion);
			//			System.out.printf("domestication_protein_proportion = %f\n", domestication_protein_proportion);
			//			System.out.printf("trading_protein_GRN_proportion = %f\n", trading_protein_GRN_proportion);
			//			System.out.printf("trading_protein_BRN_proportion = %f\n", trading_protein_BRN_proportion);
		}

		if ((hunting_costs+domestication_costs) != 0) {
			setHunting_protein_costs_proportion((double)hunting_costs/((double)hunting_costs+(double)domestication_costs));
			setDomestication_protein_costs_proportion((double)domestication_costs/((double)hunting_costs+(double)domestication_costs));
		}

		return (hunting_costs+domestication_costs);

	}


	// **** the seasons ****
	// march april may
	public void step_procure_spring() {
		int caller = 0;
		int ad_plots = this.getMySwarm().getAd_plots();
		actual_cal = 0;
		season_fam = 0;

		calculateEatCal();
		calculateNumberOfWorkers();
		laborCheck(ad_plots);

		int work_cal = plantPlots(caller);
		work_cal += monitorPlots(0,30);
		Ag_Cost += work_cal;
		actualCal(work_cal);

		eatMaize(actual_cal);
		total_cal += actual_cal;
		return;
	}

	// june july august
	public void step_procure_summer() {
		if (state <= -1) {
			return;
		}

		calculateEatCal();
		calculateNumberOfWorkers();

		int work_cal = monitorPlots(45,90);
		Ag_Cost += work_cal;
		actualCal(work_cal);

		if (Village.BURN_WOOD) {
			Fuel_Cost += burnWood();
			actual_cal += Fuel_Cost;
		}

		if(Village.HUNTING){
			Protein_Cost += step_procure_protein();
			actual_cal += Protein_Cost;
		}
		if (state <= -1) {
			return;
		}

		if (Village.DRINK_WATER) {
			Water_Cost += drinkWater();
			actual_cal += Water_Cost;
		}

		eatMaize(actual_cal);
		total_cal += actual_cal;

		return;
	}

	// sept oct nov
	public void step_procure_fall() {
		if (state <= -1) {
			return;
		}

		calculateEatCal();
		calculateNumberOfWorkers();

		int work_cal = monitorPlots(15,45);
		work_cal += harvestPlots();
		Ag_Cost += work_cal;
		actualCal(work_cal);

		Cal_Produced = (int) (act_yield * Village.MAIZE_KG_CAL);
		AVG_cal_prod = (int) (act_yield * Village.MAIZE_KG_CAL + AVG_cal_prod / 2) / 2;

		setMaizeStorage((int) (getMaizeStorage() + act_yield));
		update_state();
		updateYields();

		eatMaize(actual_cal);
		total_cal += actual_cal;
		return;
	}

	// dec jan feb
	public void step_procure_winter() {
		if (state <= -1) {
			return;
		}

		/* DC: Moving the debt recall to the start of the winter season,
            right after the harvest. */
		// call in debts
		if (getMySwarm().getEconomy() >= 4 && brnNetwork != null) {
			brnNetwork.update();
			brnNetwork.callInDebts();
		}

		calculateEatCal();
		calculateNumberOfWorkers();

		eatMaize(actual_cal);
		total_cal += actual_cal;
		return;
	}

	/** Attempts to trade protein using BRN/GRN.  Also handles protein_deficiency */
	protected void tradeProtein(int protein_need) {

		if (protein_need <= 0 || !Village.PROTEIN_TRADE) {
			return;
		}

		// exchange
		int trade = this.getMySwarm().getEconomy();

		if (trade == 1 || trade == 3 || trade >= 4) {
			// call in debts from BRN; 
			if (trade >= 4) {
				brnNetwork.callInDebts();
				protein_need -= getCurrentProteinStorage();
				trading_protein_BRN += (double)getCurrentProteinStorage();
				setCurrentProteinStorage(0);
			}

			if ((Village.TRIGGER_REQUEST_PRO() & getProteinCoopState()) != 0) {
				if (trade >= 3 && protein_coop_count < Village.COOP_ATTEMPTS) {

					int received = 0;
					received = grnNetwork.requestProtein((protein_need));
					setCurrentProteinStorage(getCurrentProteinStorage()
							+ received);
					protein_imported += received;

					protein_need -= getCurrentProteinStorage();
					trading_protein_GRN += getCurrentProteinStorage();
					setCurrentProteinStorage(0);

					if (protein_need <= 0) {
						return;
					}
				}

				if (trade >= 4 && protein_coop_count < Village.COOP_ATTEMPTS) {
					int received = brnNetwork
							.requestProtein((protein_need));
					setCurrentProteinStorage(getCurrentProteinStorage()
							+ received);
					protein_imported += received;

					protein_need -= getCurrentProteinStorage();
					trading_protein_BRN += getCurrentProteinStorage();
					setCurrentProteinStorage(0);

					if (protein_need <= 0) {
						return;
					}
				}
			}
		}
	}

	public void proteinPenalty() {
		// Death by Protein Deficiency

		int p_penalty = this.getMySwarm().getProteinPenalty();

		if (getCurrentProteinStorage() < 0) {

			setCurrentProteinStorage(0);

			if (p_penalty == 2) {
				if (++years_deficient > Village.DEATH_YEAR) {
					death();
				}
			}

			protein_deficient = 1;

		} else {
			years_deficient = 0;
		}
	}

	public void unPlotAll() {
		int i;
		// deplant
		for (i = 0; i < 9; i++) {
			//			System.out.println("Agent: " + this + ", Cell: " + i + ", farm_pl: " + farm_pl[i] + ", tot_plots: " + tot_plots + ", cell plots: " + cell[i].getFarmPl());
			cell[i].changeFarmPl(-1 * farm_pl[i]);
			tot_plots -= farm_pl[i];
			farm_pl[i] = 0;
			//			System.out.println("Agent: " + this + ", Cell: " + i + ", farm_pl: " + farm_pl[i] + ", tot_plots: " + tot_plots + ", cell plots: " + cell[i].getFarmPl() + "\n");
		}
		if (tot_plots != 0) {
			System.err.printf("Village.ERROR: agent %d unplanted wrong %d\n",
					tag, tot_plots);
			System.exit(-1);
		}
		return;
	}

	/** DC: changing return type to a int[]. Replacing use of pointers */
	public int[] unplotDX(int dx, int dy) {
		int i;
		for (i = -1; i < 2; i++) {
			if (farm_pl[i + 1] > 0) {
				dx = i;
				dy = -1;
				return new int[] { dx, dy };
			}
		}
		for (i = -1; i < 2; i++) {
			if (farm_pl[i + 7] > 0) {
				dx = i;
				dy = 1;
				return new int[] { dx, dy };
			}
		}
		if (farm_pl[3] > 0) {
			dx = -1;
			dy = 0;
			return new int[] { dx, dy };
		} else if (farm_pl[5] > 0) {
			dx = 1;
			dy = 0;
			return new int[] { dx, dy };
		}
		dx = dy = 0;
		return new int[] { dx, dy };
	}

	// called every time maize_storage is updated, which is in a lot of places
	protected void update_state() {

		if (getMaizeStorage() >= max_store) {
			coop_state = Village.FULL;
		} else if (getMaizeStorage() >= Village.PHIL_THRESHHOLD) {
			coop_state = Village.PHILANTHROPIST;
		} else if (getMaizeStorage() <= 0) {
			coop_state = Village.CRITICAL;
		} else if (getMaizeStorage() <= last_ration) {
			coop_state = Village.HUNGRY;
		} else {
			coop_state = Village.SATISFIED;
		}

		if (getCurrentProteinStorage() >= protein_max_store) {
			protein_coop_state = Village.FULL;
		} else if (getCurrentProteinStorage() >= Village
				.PROTEIN_PHIL_THRESHHOLD(protein_max_store)) {
			protein_coop_state = Village.PHILANTHROPIST;
		} else if (getCurrentProteinStorage() <= 0) {
			protein_coop_state = Village.CRITICAL;
		} else if (getCurrentProteinStorage() <= protein_max_store / 4) // protein_max_store
			// /4
			// means
			// to
			// represent
			// a
			// season
			// 's
			// worth
			// of
			// protein
		{
			protein_coop_state = Village.HUNGRY;
		} else {
			protein_coop_state = Village.SATISFIED;
		}
		return;
	}

	// **** family yearly actions ****
	void updateFamily() {
		int i, j = 0;
		// int d=0;
		int found, p;
		EligibleRecord mateRecord = null;
		Agent newhouse = null; // new child household
		Agent cloneAgent = null;

		if (Village.DEBUG) {
			System.out.printf("update family getFamilySize() is %d\n", getFamilySize());
		}

		// clone fix
		int worldtime = this.getMySwarm().getWorldTime();
		if (worldtime == 600) {
			return;
		}

		// remove ghost agent ZK
		if (!familyUnit.isLegitimate()) {
			death();
			return;
		}

		// For parents:
		Individual ind = null;

		for (i = 0; i < 2; i++) {
			if (Village.DEBUG) {
				System.out.printf("i=%d", i);
			}
			if (i == 0)
				ind = familyUnit.getWife();
			else
				ind = familyUnit.getHusband();

			if (ind != null) // if a parent exists and is alive, check to see if
				// he/she should die
			{
				if (Village.DEBUG) {
					System.out.printf("ages is %d> 0", ind.getAge());
				}
				ind.increaseAge(); // age one more year

				if (mortality(ind.getAge(), ind.getGender()) != 0) {
					// check mortality on new age
					if (Village.DEBUG) {
						System.err
						.printf(
								"Village.DEBUG -updateFamily: parent death %d\n",
								ind.getAge());
					}
					ind.die();
					ind = null;
//					removePerson(ind);
					break;
				}
			} else // parent is dead. check to see if want to remarry
			{
				// Search Marriage List, add individual to marriage list, set to
				// remarriage
				// remarriage in a year if other parent alive
				// ages[i] = (i==0 ? ages[1] : ages[0]);

				// CAUTION: if both parents are dead - get out of here!
				if (familyUnit.getWife() == null && familyUnit.getHusband() == null) {
					if (promoteChild() == 0) {
						// kill all kids
						familyUnit.removeAllMembers();

						if (state <= -1) {
							state = 0;
						}

						death();
					}
					return;
				}

				if (remarriage_count < Village.MAX_REMARRIAGE_COUNT
						&& remarriage_attempts < Village.MAX_REMARRIAGE_ATTEMPTS) {
					Individual parent = null;

					if (i == 0) {
						j = 1;
						parent = familyUnit.getHusband();
					} else {
						j = 0;
						parent = familyUnit.getWife();
					}

					if (Village.uniformDblRand(0.0, 1.0) < 0.5) // 50% chance of
						// marriage
					{
						remarriage_attempts++;
						if (parent.eligibleRecord == null) {

							/* DC: Shortcut to get only women to search.  We're going to have
							 * the search only have a chance to be successfull if it's a woman searching.
							 */

							if (parent.getGender() == Individual.FEMALE) {
								mateRecord = Eligible.findAndRemoveMate(tag, x,
										y, mySwarm, parent.getGender());
							}

							synchronized (familyUnit) {
								if (mateRecord != null) // found mate, remarry
								{
									remarriage_count++;
									remarriage_attempts = 0;

									Individual mate = mateRecord.individual;
									mateRecord.ptrHH.removePerson(mate);
									mateRecord.ptrHH.setDepartingNewlyweds(mateRecord.ptrHH.getDepartingNewlyweds() + 1);
									familyUnit.addPerson(mate);
									this.arrivingNewlyweds++;

									if (parent == familyUnit.getHusband()) {
										familyUnit.setWife(mate);
									} else
										familyUnit.setHusband(mate);

									if (i == 0) {
										// check for duplicates
										found = 0;
										for (p = 0; p < RelativeHHCount
												&& found == 0; p++) {
											if (ParentHHTagA == RelativeHHTag[p]) {
												found = 1;
											}
										}

										if (found == 0
												&& RelativeHHCount < Village.MAX_RELATIVE_LINKS) {
											RelativeHHTag[RelativeHHCount] = ParentHHTagA; // keep
											// old
											// parent
											// household
											// of
											// dead
											// spouse
											// as
											// a
											// relative
											// later can transfer more links from
											// new parent perhaps
											RelativeHHCount++;
											ParentHHTagA = mateRecord.HHTag; // update
											// parent
											// tag
											// to
											// new
											// parent
											// household
										}
									} else {
										// check for duplicates
										found = 0;
										for (p = 0; p < RelativeHHCount
												&& found == 0; p++) {
											if (ParentHHTagB == RelativeHHTag[p]) {
												found = 1;
											}
										}

										if (found == 0
												&& RelativeHHCount < Village.MAX_RELATIVE_LINKS) {
											RelativeHHTag[RelativeHHCount] = ParentHHTagB; // keep
											// old
											// parent
											// household
											// of
											// dead
											// spouse
											// as
											// a
											// relative
											RelativeHHCount++;
											ParentHHTagB = mateRecord.HHTag; // update
											// parent
											// tag
											// to
											// new
											// parent
											// household
										}
									}

									//Eligible.removeFromEligibleList(mateRecord,
									//mySwarm);

									familyUnit.getWife().setMarriageStatus(1);
									familyUnit.getHusband().setMarriageStatus(1);

									// fix birth control
									child_per_female = 0;
									last_year_child_born = 600;

								} else // mate not found, add self to list
								{
									registerEligibleForMarriage(parent);
								}
							}
						} else // was already on the eligible list, try again
						{
							// Thread safety: ensure that we weren't already chosen
							// for someone else
							mateRecord = null;

							if (parent.eligibleRecord != null  && parent.getGender() == Individual.FEMALE)
								mateRecord = Eligible.findMateAndRemoveBoth(parent.eligibleRecord, tag, x,
										y, mySwarm, parent.getGender());
							if (mateRecord != null && mateRecord.individual != null) // found mate, remarry
							{
								remarriage_count++;
								remarriage_attempts = 0;

								Individual mate = mateRecord.individual;

								mateRecord.ptrHH.removePerson(mate);
								mateRecord.ptrHH.setDepartingNewlyweds(mateRecord.ptrHH.getDepartingNewlyweds() + 1);
								familyUnit.addPerson(mate);
								this.arrivingNewlyweds++;

								if (parent == familyUnit.getHusband()) {
									familyUnit.setWife(mate);
								} else
									familyUnit.setHusband(mate);

								if (i == 0) {
									// check for duplicates
									found = 0;
									for (p = 0; p < RelativeHHCount
											&& found == 0; p++) {
										if (ParentHHTagA == RelativeHHTag[p]) {
											found = 1;
										}
									}
									if (found == 0
											&& RelativeHHCount < Village.MAX_RELATIVE_LINKS) {
										RelativeHHTag[RelativeHHCount] = ParentHHTagA; // keep
										// old
										// parent
										// household
										// of
										// dead
										// spouse
										// as
										// a
										// relative
										// later can transfer more links from
										// new parent perhaps
										RelativeHHCount++;
										ParentHHTagA = mateRecord.HHTag; // update
										// parent
										// tag
										// to
										// new
										// parent
										// household
									}
								} else {
									// check for duplicates
									found = 0;
									for (p = 0; p < RelativeHHCount
											&& found == 0; p++) {
										if (ParentHHTagB == RelativeHHTag[p]) {
											found = 1;
										}
									}
									if (found == 0
											&& RelativeHHCount < Village.MAX_RELATIVE_LINKS) {
										RelativeHHTag[RelativeHHCount] = ParentHHTagB; // keep
										// old
										// parent
										// household
										// of
										// dead
										// spouse
										// as
										// a
										// relative
										RelativeHHCount++;
										ParentHHTagB = mateRecord.HHTag; // update
										// parent
										// tag
										// to
										// new
										// parent
										// household
									}
								}
								Eligible.removeFromEligibleList(mateRecord,
										mySwarm);
								Eligible.removeFromEligibleList(
										parent.eligibleRecord, mySwarm);

								Individual wife = familyUnit.getWife();
								Individual husband = familyUnit.getHusband();

								wife.setMarriageStatus(Individual.MARRIED);
								husband.setMarriageStatus(Individual.MARRIED);
								wife.eligibleRecord = null;
								husband.eligibleRecord = null;

								//tot_pop++;
							}
						}
					}
				}
			}
		}

		//		if (Village.DEFECTOR_PROPAGATE_FAMILY != 0) {
		//			if (newhouse != null) {
		//				if (DefectingAgentFlag != 0) {
		//					newhouse.DefectingAgentFlag = 1;
		//					DefectingAgentCount++;
		//				}
		//			}
		//		}

		// Update links: Method

		// update kids
		for (Individual kid : familyUnit.getKids()) {
			kid.increaseAge();

			if (Village.DEBUG) {
				System.out.printf("kids, here is person i=%d familyUnit.getKidCount()=%d\n", i,
						familyUnit.getKidCount());
			}
			if (kid.getAge() >= 4 && mortality(kid.getAge(), kid.getGender()) == 1) { // mortality
				// rate
				// check
				if (Village.DEBUG) {
					System.err.printf("they said die\n");
				}
				removePerson(kid);
			} else if (kid.getAge() > 15 && Village.uniformDblRand(0.0, 1.0) < 0.5) // from
				// 14
				// to
				// 15
			{
				if (kid.eligibleRecord == null) {
					if (Village.DEBUG) {
						System.out
						.printf(
								"kid[%d] looking to marry ************** \n",
								i);
					}
					mateRecord = null;

					if (kid.getGender() == Individual.FEMALE) {
						mateRecord = Eligible.findAndRemoveMate(tag, x, y,
								mySwarm, kid.getGender());
					}

					if (mateRecord != null) // found mate
					{
						if (Village.DEBUG) {
							System.out
							.printf(
									"kid[%d] found a mate ************** \n",
									i);
						}

						// JAC 3/06 stopgap
						if (oldagentpop < Village.MAX_AGENT) {

							if (kid.getGender() == Village.MALE) {
								newhouse = addHousehold(mateRecord.individual, kid); // JAC
								// 11
								// /
								// 04
							} else {
								newhouse = addHousehold(kid, mateRecord.individual); // JAC
								// 11
								// /
								// 04
							}
						}
						removePerson(kid);

						// JAC 3/06 stopgap
						if (oldagentpop < Village.MAX_AGENT) {
							// copy brothers/sisters links and parental
							// relatives
							for (j = 0; j < ChildHHCount; j++) {
								// check for duplicates
								found = 0;
								for (p = 0; p < newhouse.RelativeHHCount
										&& found == 0; p++) {
									if (ChildHHTag[j] == newhouse.RelativeHHTag[p]) {
										found = 1;
									}
								}
								if (found == 0
										&& newhouse.RelativeHHCount < Village.MAX_RELATIVE_LINKS) {
									newhouse.RelativeHHTag[j] = ChildHHTag[j];
									newhouse.RelativeHHCount++;
								}
							}
						}

						cloneAgent = searchAgentList(mateRecord.HHTag);

						if (cloneAgent != null) {
							cloneAgent.removePerson(mateRecord.individual);
						} else {
							Eligible.removeFromEligibleList(mateRecord, mySwarm);
						}
					} else // mate not found, add self to list
					{
						// note: number of eligible bachelors in household is
						// not used (set to 1 for now)
						registerEligibleForMarriage(kid); // JAC
						// 11
						// /
						// 04
					}
				} else // was already on the eligible list, try again
				{
					// Thread safety: ensure that we weren't already chosen
					// for someone else
					mateRecord = null;
					if (kid.eligibleRecord != null && kid.getGender() == Individual.FEMALE)  // now remove the mate and us
						mateRecord = Eligible.findMateAndRemoveBoth(kid.eligibleRecord, tag, x, y,
								mySwarm, kid.getGender());
					if (mateRecord != null) // found mate, remarry
					{

						// JAC 3/06 stopgap
						if (oldagentpop < Village.MAX_AGENT) {
							if (kid.getGender() == Village.MALE) {
								newhouse = addHousehold(mateRecord.individual, kid);
							} else {
								newhouse = addHousehold(kid, mateRecord.individual);
							}

							// copy brothers/sisters links and parental
							// relatives
							for (j = 0; j < ChildHHCount; j++) {
								// check for duplicates
								found = 0;
								for (p = 0; p < newhouse.RelativeHHCount
										&& found == 0; p++) {
									if (ChildHHTag[j] == newhouse.RelativeHHTag[p]) {
										found = 1;
									}
								}
								if (found == 0
										&& newhouse.RelativeHHCount < Village.MAX_RELATIVE_LINKS) {
									newhouse.RelativeHHTag[j] = ChildHHTag[j];
									newhouse.RelativeHHCount++;
								}
							}

						}
						if (this.getMySwarm().getPopulate_exchange_array_list()) {

							if(this.getMySwarm().getEconomy() == 4){


								HashSet<BRNTradePartner>husbandParentTradePartners = mateRecord.ptrHH.brnNetwork.getTradePartners();
								HashSet<BRNTradePartner>wifeParentTradePartners = brnNetwork.getTradePartners();
								java.util.Iterator<BRNTradePartner> wifeItr = wifeParentTradePartners.iterator();

								//System.out.printf("Dad's length: %d\n", husbandParentTradePartners.size());
								//System.out.printf("Mom's length: %d\n", wifeParentTradePartners.size());

								//boolean odd = false;
								int count = 0;
								for (BRNTradePartner partner : husbandParentTradePartners)
								{
									/*
if (odd)
{
newhouse.brnNetwork.setTradePartner(partner);
wifeItr.next();
}
else
{
newhouse.brnNetwork.setTradePartner(wifeItr.next());
}

odd = !odd;
This commented out section is for randomizing the exchange partners
The new agent household will take only the odds from mother's house
and only the evens from father's house.
									 */

									if (count < 10)
									{
										newhouse.brnNetwork.setTradePartner(partner);

										try
										{
											newhouse.brnNetwork.setTradePartner(wifeItr.next());
										}
										catch(Exception e)
										{
											/// do nothing
										}
									}
									count++;
								}

								//the array list of Quality exchange partners appears to be ranked
								//so by taking the top exchange partners and iterating them from mother's
								//or father's house by odds and evens we get the best partners.
								//populating array for the new household

								cloneAgent = searchAgentList(mateRecord.HHTag);
								if (cloneAgent != null) {
									cloneAgent.removePerson(mateRecord.individual);
								}
								Eligible.removeFromEligibleList(kid.eligibleRecord,
										mySwarm);
								removePerson(kid);
							}
						}
					}
				}
			}
		}
	}


	public void updateYields() {
		int i, j, k;

		// update local record of yields for the spring, in units of kg/ha
		for (i = 0; i < 2; i++) {
			for (j = 0; j < 9; j++) {
				past_yield[j][i + 1] = past_yield[j][i];
			}
		}
		for (j = 0; j < 9; j++) {
			past_yield[j][0] = cell[j].getMaize_prod();
		}
		// add current yield to history

		// calc yield from what happened this season
		k = 0;
		for (i = 0; i < 9; i++) {
			k += farm_pl[i] * past_yield[i][0];
		}
		if (tot_plots > 0) {
			EXP_yield = (k / tot_plots + EXP_yield) / 2;
		} else {
			EXP_yield = (EXP_yield + past_yield[4][0]) / 2;
		}
		return;
	}

	public boolean isAlive() {
		return (state != -1);
	}

	public static int getNextTag() {
		return TAGS++;
	}

	public void setHuntingStrategy() {
		// set the hunting strategy
		if (this.getMySwarm().isDomestication()){
			huntingStrategy = new DomesticationHuntingStrategy(this);
		} else {
			huntingStrategy = new AlternateHuntingStrategy(this);
		}
	}

	public Agent get(int whotoask) {
		return searchAgentList(whotoask);
	}
	//this procedure makes it so that agents look at the wealth of those in their exchange network
	//and preferentially move closer to them

	@SuppressWarnings("unchecked")
	private int boostByProximityOfWealthyPartnersAndKin(int val, Cell location) {
		int ans = val;

		if (brnNetwork != null) {
			HashSet<BRNTradePartner> partners = (HashSet<BRNTradePartner>) brnNetwork.getTradePartners().clone();

			// now eliminate poor ones
			HashSet<Agent>  wealthy = new HashSet<Agent>();
			for (BRNTradePartner partner : partners) {
				Agent agt = partner.getAgent();
				if (agt != null && agt.getCoopState() >= 4)  // philanthropic or better
				{
					wealthy.add(agt);
				}
			}

			for (int relTag : RelativeHHTag) {
				// DC: bug handling - we don't deal with an agent 0
				if (relTag != 0) {
					Agent agt = this.getMySwarm().searchAgentList(relTag);
					if (agt != null && agt.getCoopState() >= 4)  // philanthropic or better
					{
						wealthy.add(agt);
					}
				}
			}

			if (!wealthy.isEmpty()) {
				// find out how many within range of location (BRN)
				int inRange = 0;
				for (Agent agt : wealthy) {
					if (Utilities.distance(agt.getCell(), location) <= Village.MAX_COOP_RADIUS_BRN) {
						inRange++;
					}
				}

				// now increase the value of val by the percentage within range
				ans = val * (1 + inRange / wealthy.size());
			}
			partners = null;
		}

		return ans;
	}

	/**
	 * @return the counter
	 */
	public static int getCounter() {
		return counter;
	}

	/**
	 * @param counter the counter to set
	 */
	public static void setCounter(int counter) {
		Agent.counter = counter;
	}

	/**
	 * @return the agent_time
	 */
	public int getAgent_time() {
		return agent_time;
	}

	/**
	 * @param agent_time the agent_time to set
	 */
	public void setAgent_time(int agent_time) {
		this.agent_time = agent_time;
	}
}
