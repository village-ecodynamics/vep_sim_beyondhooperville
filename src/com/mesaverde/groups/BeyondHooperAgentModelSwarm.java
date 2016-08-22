package com.mesaverde.groups;

import com.mesaverde.model.AgentModelSwarm;
import com.mesaverde.village.*;
//import com.mesaverde.groups.Constants;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class BeyondHooperAgentModelSwarm extends AgentModelSwarm {

	private int warDead = 0;

	/** Output group and HooperAgent statistics? */
	private boolean output = Constants.OUTPUT;

	/** Merge, fight, and pay tribute? */
	private boolean merge_and_fight = Constants.MERGE_AND_FIGHT;

	/**Allow groups in a complex hierarchy to revolt against their dominant group? */
	private boolean revolt = Constants.REVOLT; 

	/** Play Hooper PG Game? */
	private boolean hooper_pg_game = Constants.HOOPER_PG_GAME;

	/**If k-means is set to true, in BeyondGroup groups will fission using a k-means algorithm
	 * If this is set to false, they will fission via a bud-off algorithm */
	private boolean k_means_clustering = Constants.K_MEANS_CLUSTERING;

	/** GROUP_SIZE - A limit to the size of a group. If a group exceeds this size, the agent farthest from 
	 * the geometric centroid of the group will bud off and create a new group. */
	private int group_size = Constants.GROUP_SIZE;

	/** The probability of death from fighting
	 */
	private double s = Constants.S;

	/** The direct tax on the benefit from the public goods game */
	private double beta = Constants.BETA;

	/** The tax on the tribute received */
	private double mu = Constants.MU;

	/**If Alliances is set to true groups in the same lineage will not attack one another */
	private boolean alliances = Constants.ALLIANCES;

	/** PG Game Epigenetic effect? 
	 * When this is true, agent offspring choose their hierarchical 
	 * preference based on their parents experience in the PG game
	 * in the last year. If the parent household's benefits from the 
	 * game outweighed their costs, they adopt their parent's preference.
	 * If the costs outweighed the benefits, they adopt the opposite preference.
	 */
	private boolean pg_outcome_epigenetic_effect = Constants.PG_OUTCOME_EPIGENETIC_EFFECT;

	//    /** The amount that agents are willing to change in one iteration with social learning */
	private double change_rate = Constants.CHANGE_RATE;


	/** CALDAY - amount of calories to feed a family for one day (5435: enough for 2 kids, mom & dad for 1 day) */
	private double calday = Constants.CALDAY;

	/** CALDAY converted into maize-days (number of kg of maize to support a family for one day), approx. 1.5 kg 
	 * multiplying by this converts days to kg */
	private double kg_day = calday / Village.MAIZE_KG_CAL;

	/** Growth rate for benefits as group size increases 
	 * This is k in the function suggested by Kyle which in R is b <- bm * e^(-k/n) where bm = bmax = 60 
	 * smaller values (e.g., 2) increase b faster as n increases*/
	private double group_benefit_growth_rate = Constants.GROUP_BENEFIT_GROWTH_RATE;

	/** Increased storage capabilities for leaders in Beyond Hooperville */
	private int leader_storage = Constants.leader_STORAGE; //multiplier used in Agent.java so the leader can have more storage. 1 means the storage is the same as the other group members
	//2 means it is doubled. 1.5 is 50% more than other members

	/** Maximum benefit produced by contributing to the public good */
	private double b_benefit = Constants.b_BENEFIT; //was 60

	/** Cost of contributing to the public good */
	private int c_cost = Constants.c_COST; //was 20

	/** Cost imposed on defectors (i.e. sanction or punishment; Hooper's s) */
	private int s_sanction = Constants.s_SANCTION; //was 30

	/** Cost imposed on non-taxpayers; Hooper's s-hat) */
	private int s_tax_sanction = Constants.s_TAX_SANCTION;

	/** Cost of monitoring one group member; Hooper's c-sub-m */
	private int cm_monitor_cost = Constants.cm_MONITOR_COST; //was 6

	/** Cost of punishing one defector; Hooper's c-sub-s */
	private int cs_sanction_cost = Constants.cs_SANCTION_COST; 

	/** Cost of punishing one non-taxpayer; Hooper's c-hat-sub-s */
	private int cs_tax_sanction_cost = Constants.cs_TAX_SANCTION_COST;

	/** Cost of walking to punish defectors; set to 1 because it's multiplied by the # of cells between two agents */
	private int cs_sanction_dist = Constants.cs_Sanction_Dist;

	/** The minimum a leader can charge in tax */
	private double tax_rate_min = cm_monitor_cost / b_benefit;

	/** The maximum a leader can charge in tax */
	private double tax_rate_max = 1 - c_cost / b_benefit;

	/** The W coefficient determines whether we are operating in Lanchester's linear
	 * or Square law.
	 * 1 is linear
	 */
	private double w = Constants.W;

	/** Only men fight? */
	private boolean men_only_fight = Constants.MEN_ONLY_FIGHT;

	/** Minimum fighting age. */
	private int min_fight_age = Constants.MIN_FIGHT_AGE;

	/** Maximum fighting age. */
	private int max_fight_age = Constants.MAX_FIGHT_AGE;

	private double requiredContribution = c_cost * kg_day;

	private BeyondHooperMaster hooper2;

	public BeyondHooperAgentModelSwarm() {
		super();		
	}

	/* DC: A lot of the initialization stuff should be done here.
	 * This method is called whenever a SimModel is being reused, such
	 * as when running in batch mode.
	 * @see uchicago.src.sim.engine.SimpleModel#setup()
	 */
	@Override

	public void setup() {
		super.setup();

		Agent.resetStatics();
		BeyondGroup.resetID();
		this.hooper2 = new BeyondHooperMaster(this);
		warDead = 0;
	}

	/** invoked by -addHousehold in Agent.m to add newly formed households
	 * next to parents
	 *
	 * @param atx
	 * @param aty
	 * @param wifeage
	 * @param husbandage
	 * @param wifehh
	 * @param husbandhh
	 * @param w
	 * @param h
	 * @return
	 */
	public BeyondHooperAgent addAgent(int atx, int aty, Individual wife, Individual husband/*int wifeage, int husbandage,
			int wifehh, int husbandhh, int w, int h*/) {
		BeyondHooperAgent agent = new BeyondHooperAgent();		

		agent.setWorld(world);
		agent.setParentHHTagA(wife.getFamily().getAgent().getTag());
		agent.setParentHHTagB(husband.getFamily().getAgent().getTag());
		BeyondHooperAgent wifeFam = (BeyondHooperAgent) wife.getFamily().getAgent();
		agent.getFamilyUnit().setWife(wife);
		agent.getFamilyUnit().setHusband(husband);		
		agent.setXY(atx, aty);

		agent.setFormationDate(getTime());
		agent.setMySwarm(this);

		agent.setPreference(wifeFam.getPreference().copy());

		if(pg_outcome_epigenetic_effect){
			//				System.out.println("Checking for epigenetic effect!");
			//				System.out.println("Parent HH net gain/loss: " + (((BeyondHooperAgent) wifeFam).getPgGameBenefit()-((BeyondHooperAgent) wifeFam).getPgGameLoss()));
			if(wifeFam.getPgGameBenefit()<wifeFam.getPgGameLoss()){
				//					System.out.println("Maternal households lost more than it gained in the PG Game. Switching H preference from " + ((BeyondHooperAgent) agent).getPreference().isH());
				agent.getPreference().setH(!agent.getPreference().isH());
				//					System.out.println("New H preference: " + ((BeyondHooperAgent) agent).getPreference().isH());
			}
		}
		//			System.out.println("Calling -setGroup from -addAgent");
		agent.setGroup(wifeFam.getGroup());



		agent.createEnd();

		agentList.add(agent);
		setAllAgents(getAllAgents() + 1);
		// return self;

		agent.moveHouse(false);

		return agent;
	}

	/**
	 * DC: implement the modelActions in here. Better to do it this way than via
	 * a schedule. This method allows debugging, whereas doing it via a schedule
	 * does not. All the schedule model actions that are performed at each time
	 * step happens here.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void step() {
		try {
			if (Village.DEBUG) {
				System.out.printf("$$setDeathCounter\n");
			}

			for (Agent a : agentList) {
				a.setDeathCounter();
			}

			// JAC 1/05 This action updates the water flow rates in the model
			if (Village.DEBUG) {
				System.out.printf("$$setUpdateXYDataGrid\n");
			}

			database.updateXYDataGrid();
			if (Village.DEBUG) {
				System.out.printf("$$setUpdateCellWorld\n");
			}

			database.updateCellWorld();
			if (Village.DEBUG) {
				System.out.printf("$$setUpdateDeerCells\n");
			}

			// may need to exit here
			if (endSim)
				return;

			database.updateDeerCells();
			if (Village.DEBUG) {
				System.out.printf("$$setFuelProduction\n");
			}

			for (Cell c : cellList) {
				c.setFuelProduction();
			}

			if (Village.DEBUG) {
				System.out.printf("$$step_procure\n");

			}

			setWarDead(0);

			/*
			 * We create a copy of the agentList, this way, we are not affected
			 * by changing elements in that list
			 */
			ArrayList<Agent> cloneList = (ArrayList<Agent>) agentList.clone();
			shuffleAgents(cloneList);
			for (Agent a : cloneList) {
				a.step_procure_init();
			}

			cloneList = (ArrayList<Agent>) agentList.clone();
			shuffleAgents(cloneList);
			for (Agent a : cloneList) {
				a.step_procure_spring();
			}

			cloneList = (ArrayList<Agent>) agentList.clone();
			shuffleAgents(cloneList);
			for (Agent a : cloneList) {
				a.step_interact();
			}
			removeAgents();

			cloneList = (ArrayList<Agent>) agentList.clone();
			shuffleAgents(cloneList);
			for (Agent a : cloneList) {
				a.step_procure_summer();
			}

			cloneList = (ArrayList<Agent>) agentList.clone();
			shuffleAgents(cloneList);
			for (Agent a : cloneList) {
				a.step_interact();
			}
			removeAgents();

			cloneList = (ArrayList<Agent>) agentList.clone();
			shuffleAgents(cloneList);
			for (Agent a : cloneList) {
				a.step_procure_fall();
			}

			cloneList = (ArrayList<Agent>) agentList.clone();
			shuffleAgents(cloneList);
			for (Agent a : cloneList) {
				a.step_interact();
			}
			removeAgents();

			cloneList = (ArrayList<Agent>) agentList.clone();
			shuffleAgents(cloneList);
			for (Agent a : cloneList) {
				a.step_procure_winter();
			}

			removeAgents();

			cloneList = (ArrayList<Agent>) agentList.clone();
			shuffleAgents(cloneList);
			for (Agent a : cloneList) {
				a.step_procure_conclude();
			}

			cloneList = (ArrayList<Agent>) agentList.clone();
			shuffleAgents(cloneList);
			for (Agent a : cloneList) {
				a.getFamilyUnit().claimNullIndividuals();
			}

			// DC: Here we place the hooper game stuff if enabled
			getHooper2().execute();

			database.diffuseDeer();

			// JAC 6/05 print statistics
			boolean heading = false;
			if(this.getWorldTime()==600){
				heading = true;
			}
			for (Agent a : agentList) {
				a.printagentstats(heading);
				heading = false;
			}

			if (Village.DEBUG) {
				System.out.printf("$$updateYear\n");
			}

			System.out.flush();
			updateYear();

			// Experiment to destroy phantom plots. Let's see if it works.
			// Set number of plots in each cell to zero.
			for (Cell c : cellList) {
				c.setFarming_plots(0);
			}

			// Iterate through agents and get list of plot locations
			for (Agent a : agentList) {
				for(int i = 0; i < 9; i++){
					a.getCellAt(i).changeFarmPl(a.getFarmPlAt(i));
				}
			}



			if (Village.DEBUG) {
				System.out.printf("$$updateOccupied\n");
			}

			System.out.flush();
			for (Cell c : cellList) {
				c.updateOccupied();
			}

			if (Village.DEBUG) {
				System.out.printf("$$updateLocalcellList\n");
			}

			System.out.flush();

			if (Village.DEBUG) {
				System.out.printf("$$debug\n");
			}

			debug();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void initWorld() {
		int i, j;
		// First, set up objects used to represent the environment.
		world = new VillageSpace(getWorldXSize(), getWorldYSize());

		// world2 created for deercells
		deerWorld = new VillageSpace(getDeerXSize(), getDeerYSize());
		
		// add things to the world without warnings
		world.setOverwriteWarnings(false);

		// create the cell world
		for (i = 0; i < getWorldYSize(); i++) {
			for (j = 0; j < getWorldXSize(); j++) {
				HashSet<Agent> settlerSet;

				// initialize new cell
				settlerSet = new HashSet<Agent>(); // settler set in cell

				Cell cell = new Cell();
				cell.setWorld(world);
				cell.setDeerWorld(deerWorld);
				cell.setXY(j, i);
				cell.setSettlerSet(settlerSet);
				cell.setOccupied(0);
				cell.setMySwarm(this); // zk

				cellList.add(cell);
			}
		}

		// This randomizes the x and y locations WITHOUT REPLACEMENT.
		final ArrayList<Integer> arr = new ArrayList<Integer>(getWorldXSize() * getWorldYSize()); 
		for (i = 1; i <= getWorldXSize() * getWorldYSize(); i++) {
			arr.add(i);
		}
		Collections.shuffle(arr);

		// create some agents
		for (i = 0; i < getAllAgents(); i++) {
			BeyondHooperAgent agent;
			int a, b, c;

			// get ages for the parents and number kids
			a = Village.uniformIntRand(17, 30);
			c = Village.uniformIntRand(17, 30);
			b = Village.uniformIntRand(1, (a / (Village.HH_SIZE - 2))); // by

			agent = new BeyondHooperAgent();
			agent.setFormationDate(this.getTime());
			agent.setMySwarm(this);
			agent.setWorld(world);
			agent.setXY(xFromCell(arr.get(i)), yFromCell(arr.get(i)));
			agent.getFamilyUnit().createParents(a, c, 0, 0);
			agent.setRandNumKids(b);
			agent.setPreference(new OrganizationPreference(Village.uniformIntRand(0, Integer.MAX_VALUE), this));
			agent.setGroup(this.hooper2.generateNewGroup());

			agent.createEnd();
			
			agentList.add(agent);
		}

		world.setOverwriteWarnings(true);
	}


	public int getWarDead() {
		return warDead;
	}


	public int getCmMonitorCost() {
		return cm_monitor_cost;
	}

	public double getGroup_benefit_growth_rate() {
		return group_benefit_growth_rate;
	}

	public int getWarDeadNum() {
		return warDead; 
	}

	protected void printParams() {
		System.out.printf("FILESUFX = %d\n", File_ID);
		System.out.printf("NEED_MEAT = %d\n", need_meat);
		System.out.printf("HARVEST_ADJUST_FACTOR = %f\n", harvest_adjustment);
		System.out.printf("COOP = %d\n", economy);
		System.out.printf("HUNT_SRADIUS = %d\n", hunting_radius);
		System.out.printf("PROTEIN_PENALTY = %d\n", p_penalty);
		System.out.printf("PROTEIN_NEED = %d\n", p_need);
		System.out.printf("STATE_GOOD = %f\n", state_good);
		System.out.printf("AD_PLOTS = %d\n", ad_plots);
		System.out.printf("max_coop_radius_brn = %d\n", max_coop_radius_brn); //HooperVill
		System.out.printf("cm_monitor_cost = %d\n", cm_monitor_cost); //HooperVill
		System.out.printf("DOMESTICATION = %b\n", domestication);
		System.out.printf("TURKEY_WATER = %b\n", turkey_water);
		System.out.printf("TURKEY_MAIZE_PER = %f\n", turkey_maize_per);
		System.out.printf("group_benefit_growth_rate = %f\n", group_benefit_growth_rate); //HooperVill
		//		System.out.printf("change_rate = %f\n", change_rate); //HooperVill

		String combined = "output/parameters_run_" + File_ID + ".csv";

		try {
			FileWriter out = new FileWriter(new File(combined));
			out.write("FILESUFX," +
					"NEED_MEAT," +
					"HARVEST_ADJUST_FACTOR," +
					"COOP," +
					"HUNT_SRADIUS," +
					"PROTEIN_PENALTY," +
					"PROTEIN_NEED," +
					"STATE_GOOD," +
					"AD_PLOTS," +
					"max_coop_radius_brn," +
					"cm_monitor_cost," +
					"DOMESTICATION," + 
					"TURKEY_WATER," + 
					"TURKEY_MAIZE_PER," + 
					"group_benefit_growth_rate\n");
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			FileWriter out = new FileWriter(new File(combined),true);
			out.write(this.File_ID + "," +
					this.need_meat + "," +
					this.harvest_adjustment + "," +
					this.economy + "," +
					this.hunting_radius + "," +
					this.p_penalty + "," +
					this.p_need + "," +
					this.state_good + "," +
					this.ad_plots + "," +
					this.max_coop_radius_brn + "," +
					this.cm_monitor_cost + "," +
					this.domestication + "," + 
					this.turkey_water + "," +
					this.turkey_maize_per + "," +
					this.group_benefit_growth_rate + "\n");
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void removeAgents() {
		if (Village.DEBUG) {
			System.out.printf("Start Removeagents\n");
		}

		// DC: A quick way to get an updated list while iterating
		ArrayList<Agent> updatedList = new ArrayList<Agent>(agentList.size());

		for (Agent t : agentList) {
			if (t.getFamilySize() <= 0 || t.getState() == -1) {
				t.getCell().removeSettler(t); // zk

				/* This cleans up HooperAgents that either died
				 * or were depleted (people left).  We just make sure
				 * they are removed from their groups.
				 */
				BeyondHooperAgent ha = (BeyondHooperAgent) t;
				if (ha.getGroup() != null)
					ha.setGroup(null);

			} else {
				updatedList.add(t);
			}
		}

		// DC: Now update the agentList
		agentList.clear();
		agentList.addAll(updatedList);

		//setNumAgents(agentList.size());
		if (Village.DEBUG) {
			System.out.printf("End Removeagents\n");
		}
	}

	public void setWarDead(int warDead) {
		this.warDead = warDead;
	}

	public void setGroup_benefit_growth_rate(int rate) { // TK: added for HooperVill sweep
		this.group_benefit_growth_rate = rate;
	}

	public void setCm_Monitor_Cost(int cm_monitor_cost) { // TK: added for HooperVill sweep
		this.cm_monitor_cost = cm_monitor_cost;
	}

	/**
	 * @return the hooper2
	 */
	public BeyondHooperMaster getHooper2() {
		return hooper2;
	}

	/**
	 * @param hooper2 the hooper2 to set
	 */
	public void setHooper2(BeyondHooperMaster hooper2) {
		this.hooper2 = hooper2;
	}

	public boolean isOutput() {
		return output;
	}

	public void setOutput(boolean output) {
		this.output = output;
	}

	public boolean isMerge_and_fight() {
		return merge_and_fight;
	}

	public void setMerge_and_fight(boolean merge_and_fight) {
		this.merge_and_fight = merge_and_fight;
	}

	public boolean isRevolt() {
		return revolt;
	}

	public void setRevolt(boolean revolt) {
		this.revolt = revolt;
	}

	public boolean isHooper_pg_game() {
		return hooper_pg_game;
	}

	public void setHooper_pg_game(boolean hooper_pg_game) {
		this.hooper_pg_game = hooper_pg_game;
	}

	public boolean isK_means_clustering() {
		return k_means_clustering;
	}

	public void setK_means_clustering(boolean k_means_clustering) {
		this.k_means_clustering = k_means_clustering;
	}

	public int getGroup_size() {
		return group_size;
	}

	public void setGroup_size(int group_size) {
		this.group_size = group_size;
	}

	public double getS() {
		return s;
	}

	public void setS(double s) {
		this.s = s;
	}

	public double getBeta() {
		return beta;
	}

	public void setBeta(double beta) {
		this.beta = beta;
	}

	public double getMu() {
		return mu;
	}

	public void setMu(double mu) {
		this.mu = mu;
	}

	public boolean isAlliances() {
		return alliances;
	}

	public void setAlliances(boolean alliances) {
		this.alliances = alliances;
	}

	public boolean isPg_outcome_epigenetic_effect() {
		return pg_outcome_epigenetic_effect;
	}

	public void setPg_outcome_epigenetic_effect(boolean pg_outcome_epigenetic_effect) {
		this.pg_outcome_epigenetic_effect = pg_outcome_epigenetic_effect;
	}

	public double getChange_rate() {
		return change_rate;
	}

	public void setChange_rate(double change_rate) {
		this.change_rate = change_rate;
	}

	public double getCalday() {
		return calday;
	}

	public void setCalday(double calday) {
		this.calday = calday;
	}

	public double getKg_day() {
		return kg_day;
	}

	public void setKg_day(double kg_day) {
		this.kg_day = kg_day;
	}

	public int getLeader_storage() {
		return leader_storage;
	}

	public void setLeader_storage(int leader_storage) {
		this.leader_storage = leader_storage;
	}

	public double getB_benefit() {
		return b_benefit;
	}

	public void setB_benefit(double b_benefit) {
		this.b_benefit = b_benefit;
	}

	public int getC_cost() {
		return c_cost;
	}

	public void setC_cost(int c_cost) {
		this.c_cost = c_cost;
	}

	public int getS_sanction() {
		return s_sanction;
	}

	public void setS_sanction(int s_sanction) {
		this.s_sanction = s_sanction;
	}

	public int getS_tax_sanction() {
		return s_tax_sanction;
	}

	public void setS_tax_sanction(int s_tax_sanction) {
		this.s_tax_sanction = s_tax_sanction;
	}

	public int getCm_monitor_cost() {
		return cm_monitor_cost;
	}

	public void setCm_monitor_cost(int cm_monitor_cost) {
		this.cm_monitor_cost = cm_monitor_cost;
	}

	public int getCs_sanction_cost() {
		return cs_sanction_cost;
	}

	public void setCs_sanction_cost(int cs_sanction_cost) {
		this.cs_sanction_cost = cs_sanction_cost;
	}

	public int getCs_tax_sanction_cost() {
		return cs_tax_sanction_cost;
	}

	public void setCs_tax_sanction_cost(int cs_tax_sanction_cost) {
		this.cs_tax_sanction_cost = cs_tax_sanction_cost;
	}

	public int getCs_sanction_dist() {
		return cs_sanction_dist;
	}

	public void setCs_sanction_dist(int cs_sanction_dist) {
		this.cs_sanction_dist = cs_sanction_dist;
	}

	public double getTax_rate_min() {
		return tax_rate_min;
	}

	public void setTax_rate_min(double tax_rate_min) {
		this.tax_rate_min = tax_rate_min;
	}

	public double getTax_rate_max() {
		return tax_rate_max;
	}

	public void setTax_rate_max(double tax_rate_max) {
		this.tax_rate_max = tax_rate_max;
	}

	public double getW() {
		return w;
	}

	public void setW(double w) {
		this.w = w;
	}

	public boolean isMen_only_fight() {
		return men_only_fight;
	}

	public void setMen_only_fight(boolean men_only_fight) {
		this.men_only_fight = men_only_fight;
	}

	public int getMin_fight_age() {
		return min_fight_age;
	}

	public void setMin_fight_age(int min_fight_age) {
		this.min_fight_age = min_fight_age;
	}

	public int getMax_fight_age() {
		return max_fight_age;
	}

	public void setMax_fight_age(int max_fight_age) {
		this.max_fight_age = max_fight_age;
	}

	public double getRequiredContribution() {
		return requiredContribution;
	}

	public void setRequiredContribution(double requiredContribution) {
		this.requiredContribution = requiredContribution;
	}

	public void setGroup_benefit_growth_rate(double group_benefit_growth_rate) {
		this.group_benefit_growth_rate = group_benefit_growth_rate;
	}
}
