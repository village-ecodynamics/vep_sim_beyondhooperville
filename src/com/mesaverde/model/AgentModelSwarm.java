package com.mesaverde.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import com.mesaverde.domestication.DomesticationParameters;
import com.mesaverde.filewriters.xmlTradeWriter;
import com.mesaverde.village.Agent;
import com.mesaverde.village.BeliefSpace;
import com.mesaverde.village.Cell;
import com.mesaverde.village.Database;
import com.mesaverde.village.DeerCell;
import com.mesaverde.village.Eligible;
import com.mesaverde.village.EligibleRecord;
import com.mesaverde.village.Individual;
import com.mesaverde.village.Logger;
import com.mesaverde.village.Utilities;
import com.mesaverde.village.Village;
import com.mesaverde.village.VillageSpace;
import com.mesaverde.village.WaterManager;

import uchicago.src.sim.engine.ActionGroup;
import uchicago.src.sim.engine.Controller;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimpleModel;

public class AgentModelSwarm extends SimpleModel {
	protected int numAgents; // total num agents overall
	// Number of active agents
	protected int allAgents;
	protected int which_yield; // these are all simulation parameters
	protected int random_yield; // world time
	protected int time;

	protected int worldXSize;
	protected int worldYSize;
	protected int deerXSize;
	protected int deerYSize;

	protected ArrayList<Agent> agentList;
	ActionGroup modelActions; // scheduling data structures

	// the simulation objects in the world
	protected ArrayList<Cell> cellList; // set of cells in VillageSpace
	ArrayList<DeerCell> deercellList; // set of supercells in VillageSpace
	protected Database database;
	protected VillageSpace world; // 1.2
	protected VillageSpace deerWorld;
	protected int File_ID;
	protected int economy;
	protected int p_need;
	protected int p_penalty;
	protected int hunting_radius;
	protected float harvest_adjustment;
	protected int need_meat;
	protected float state_good;
	protected int soil_degrade;
	protected int ad_plots;
	protected int max_coop_radius_brn = Village.MAX_COOP_RADIUS_BRN;

	protected boolean domestication;
	protected boolean turkey_water;
	protected int proportion_maize_turkey_diet;
	protected double turkey_maize_per;

	protected boolean populate_exchange_array_list;
	protected int trigger_coop_state;
	protected float coop_rate;

	protected boolean trade_boosted_move;

	protected int loggingFrequency = 1; // Frequency of fileI/O

	// DC: Run the whole sim unless shorter time given
	protected int experimentDuration = 700; // When to Stop the Sim

	// DC: static variables taken from methods.
	// Java doesn't allow that, so they have to become static class variables
	protected static final int[] est = new int[] { 154, 157, 983, 964, 964, 1124, 1334,
		2268, 2828, 2828, 14 };
	protected static int est_i;
	protected static final int[] min_est = new int[] { 182, 100, 226, 273, 151, 125,
		345, 407, 1040, 1271, 1516, 1854, 2509, 1189, 14 };
	protected static int oldyear;
	protected static int pop_est_agents_i;
	protected static final int[] min_est2 = new int[] { 314, 427, 1028, 1382, 780, 385,
		731, 674, 1494, 2254, 2472, 2737, 4318, 2931, 14 };
	protected static int oldyear2;
	protected static int pop_est_meth_i2;
	protected static final int[] min_est3 = new int[] { 304, 326, 836, 1030, 370, 289,
		653, 671, 1385, 1940, 2077, 2326, 3234, 1770, 14 };
	protected static int oldyear3;
	protected static int pop_est_meth_i3;
	protected static int debug_i;

	protected Logger logger;

	public xmlTradeWriter xmlwriter;

	protected WaterManager waterManager;

//	protected static BeyondHooperMaster hooper2;
	protected boolean endSim = false;

	// creation stuff

	// creation stuff
	public AgentModelSwarm() {
		super();
		Controller.ALPHA_ORDER = false;				

	}

	/* DC: A lot of the initialization stuff should be done here.
	 * This method is called whenever a SimModel is being reused, such
	 * as when running in batch mode.
	 * @see uchicago.src.sim.engine.SimpleModel#setup()
	 */
	@Override

	public void setup() {
		super.setup();

		// Ensure Village.OUTPUT_DIR exists
		if (Village.OUTPUT){
			(new File(Village.OUTPUT_DIR)).mkdirs();
		}
		
		// Ensure Village.DATA_DIR is unzipped
		if (!(new File(Village.DATA_DIR)).exists()){
			Utilities.unzipFile(Village.DATA_DIR + ".zip");
		}
		
		Database.resetStatics();
		Cell.resetStatics();

		endSim = false;

//		if (Village.AGENT_TYPE == Village.HOOPER_AGENTS)
//			BeyondHooperAgent.resetStatics();
//		else
			Agent.resetStatics();

		if (agentList != null) {
			agentList.clear();
			agentList = null;
			//	        super.agentList.clear();
		}

		modelActions = null;

		if (cellList != null) {
			cellList.clear();
			cellList = null;
		}

		if (deercellList != null) {
			deercellList.clear();
			deercellList = null;
		}

		database = null;
		world = null;
		deerWorld = null;
//		setHooper2(null);
		logger = null;
		waterManager = null;

		// Setting the name of our model
		name = "The Village Ecodynamics Project v3.0";

		which_yield = 0;
		random_yield = 442;
		time = 600; // reflects new datafiles starting at year 600 JC 9/15/04

		numAgents = 0; 
//		warDead = 0;

		// Now fill in various simulation parameters with default values.
		// When an instance variable belongs to an object that's not the
		// receiver, the object's type must be made explicit to the compiler
		// through static typing. In referring to the instance variable of a
		// statically typed object, the structure pointer (.) is used:
		est_i = -300;
		oldyear = 0;
		pop_est_agents_i = 0;
		oldyear2 = 0;
		pop_est_meth_i2 = 0;
		oldyear3 = 0;
		pop_est_meth_i3 = 0;
		debug_i = 0;

		allAgents = Village.INITIAL_AGENT_COUNT; // ZK: changed from 154 for

		worldXSize = Village.WORLD_X_SIZE;
		worldYSize = Village.WORLD_Y_SIZE;
		deerXSize = (int)(Math.ceil((double)Village.WORLD_X_SIZE / 5));
		deerYSize = (int)(Math.ceil((double)Village.WORLD_Y_SIZE / 5));

//		if (Village.AGENT_TYPE == Village.HOOPER_AGENTS) {
//			this.hooper2 = new BeyondHooperMaster(this);
//		}

		// create sets to keep track of agents and cells
		agentList = new ArrayList<Agent>();
		cellList = new ArrayList<Cell>(Village.WORLD_X_SIZE * Village.WORLD_Y_SIZE);

		// initialization of scm variables to amounts listed in village.h
		// if scm is used these variables will change to amounts from that file
		File_ID = Village.FILESUFX;
		economy = Village.COOP;
		p_need = Village.PROTEIN_NEED;
		p_penalty = Village.PROTEIN_PENALTY;
		hunting_radius = Village.HUNT_SRADIUS;
		harvest_adjustment = Village.HARVEST_ADJUST_FACTOR;
		soil_degrade = Village.SOIL_DEGRADE;

		if(Village.NEED_MEAT){
			need_meat=1;
		}else{
			need_meat=0;
		}


		state_good = Village.STATE_GOOD;
		ad_plots = Village.AD_PLOTS;
		//		max_coop_radius_brn = Village.MAX_COOP_RADIUS_BRN;  //TK: for HooperVill sweep
//		cm_monitor_cost = Constants.cm_MONITOR_COST;   //TK: for HooperVill sweep
		domestication = Village.DOMESTICATION;
		turkey_water = DomesticationParameters.TURKEY_WATER;
		turkey_maize_per = DomesticationParameters.TURKEY_MAIZE_PER;
		populate_exchange_array_list = Village.POPULATE_EXCHANGE_ARRAY_LIST;
		trade_boosted_move = Village.TRADE_BOOSTED_MOVE;
		trigger_coop_state = Village.TRIGGER_COOP_STATE;
		coop_rate = Village.COOP_RATE;
	}

	/** Now it's time to build the model objects. We use various parameters
	 * inside ourselves to choose how to create things.
	 * 
	 */
	@Override






	public void buildModel() {
		logger = new Logger();
		logger.init();

		// allow our parent class to build anything.
		Eligible.InitializeEligibleList();
		// Eligible.InitializeEligibleList(this);

		// create the objects in our world
		BeliefSpace.InitializeBeliefSpace();

		initWorld();
		initDeerWorld(); // used for deer cells

		initDataBase();

		printParams();

		if (experimentDuration > 0) {
			this.setStoppingTime(experimentDuration + 1);
		}

		waterManager = new WaterManager(this);
		waterManager.update();

		if(Village.OUTPUT_TRADE){
			xmlwriter = new xmlTradeWriter();	
		}

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
	public Agent addAgent(int atx, int aty, Individual wife, Individual husband/*int wifeage, int husbandage,
			int wifehh, int husbandhh, int w, int h*/) {
		Agent agent = Utilities.createAgent();		

		agent.setWorld(world);
		agent.setParentHHTagA(wife.getFamily().getAgent().getTag());
		agent.setParentHHTagB(husband.getFamily().getAgent().getTag());
//		Agent wifeFam = wife.getFamily().getAgent();
		agent.getFamilyUnit().setWife(wife);
		agent.getFamilyUnit().setHusband(husband);		
		agent.setXY(atx, aty);

//		int wifehh = wife.getFamily().getAgent().getTag();
//		FamilyUnit husbandFam = husband.getFamily();
//
//		int husbandhh = -1;
//
//		if (husbandFam != null) {
//			/*int husbandhh = husband.getFamily().getAgent().getTag(); */
//			husbandhh = husbandFam.getAgent().getTag();
//		}

		agent.setFormationDate(getTime());
		agent.setMySwarm(this);


		// ZK
//		if (getTime() > 0) {
//			agent.setParentHHLinksWife(wifehh, husbandhh);
//		}

//		if(agent instanceof BeyondHooperAgent){	
//			//			System.out.println("New agent: " + agent);
//
//			//			System.out.println("motherHH: " + wifeFam);
//			//			System.out.println("motherHH group: " + ((BeyondHooperAgent) wifeFam).getGroup());
//			//			System.out.println("motherHH preference: " + ((BeyondHooperAgent) wifeFam).getPreference());
//
//			((BeyondHooperAgent) agent).setPreference(((BeyondHooperAgent) wifeFam).getPreference().copy());
//
//			if(Constants.PG_OUTCOME_EPIGENETIC_EFFECT){
//				//				System.out.println("Checking for epigenetic effect!");
//				//				System.out.println("Parent HH net gain/loss: " + (((BeyondHooperAgent) wifeFam).getPgGameBenefit()-((BeyondHooperAgent) wifeFam).getPgGameLoss()));
//				if(((BeyondHooperAgent) wifeFam).getPgGameBenefit()<((BeyondHooperAgent) wifeFam).getPgGameLoss()){
//					//					System.out.println("Maternal households lost more than it gained in the PG Game. Switching H preference from " + ((BeyondHooperAgent) agent).getPreference().isH());
//					((BeyondHooperAgent) agent).getPreference().setH(!((BeyondHooperAgent) agent).getPreference().isH());
//					//					System.out.println("New H preference: " + ((BeyondHooperAgent) agent).getPreference().isH());
//				}
//			}
//			//			System.out.println("Calling -setGroup from -addAgent");
//			((BeyondHooperAgent) agent).setGroup(((BeyondHooperAgent) wifeFam).getGroup());
//
//			//			System.out.println("Agent group: " + ((BeyondHooperAgent) agent).getGroup());
//			//			System.out.println("Agent preference: " + ((BeyondHooperAgent) agent).getPreference());
//			//			System.out.println("");
//		}

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

//			setWarDead(0);

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
//			if (Village.AGENT_TYPE == 2) {
//				getHooper2().execute();
//			}

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
			Agent agent;
			int a, b, c;

			// get ages for the parents and number kids
			a = Village.uniformIntRand(17, 30);
			c = Village.uniformIntRand(17, 30);
			b = Village.uniformIntRand(1, (a / (Village.HH_SIZE - 2))); // by

			agent = Utilities.createAgent();
//			agent.setTag(i); // unique ID

			agent.setFormationDate(getTime());
			agent.setMySwarm(this);
			agent.setWorld(world);
			agent.setXY(xFromCell(arr.get(i)), yFromCell(arr.get(i)));
			agent.getFamilyUnit().createParents(a, c, 0, 0);
			agent.setRandNumKids(b);

//			if(agent instanceof BeyondHooperAgent){
//				//				System.out.println("New agent: " + agent);
//
//				((BeyondHooperAgent) agent).setPreference(new OrganizationPreference(Village.uniformIntRand(0, Integer.MAX_VALUE)));
//				//				System.out.println("Calling -setGroup from -initWorld");
//				((BeyondHooperAgent) agent).setGroup(this.hooper2.generateNewGroup());
//				//				System.out.println("New group: " + ((BeyondHooperAgent) agent).getGroup());
//				//				System.out.println("Agent preference: " + ((BeyondHooperAgent) agent).getPreference());
//				//				System.out.println("");
//			}

			agent.createEnd();
			agentList.add(agent);
		}

		world.setOverwriteWarnings(true);
	}

	public void initDeerWorld() {
		int i, j;

		// create sets to keep track of agents and cells
		deercellList = new ArrayList<DeerCell>();

		// add things to the world without the warnings that VillageSpace would
		// ordinarily generate?
		deerWorld.setOverwriteWarnings(false);

		// create the deercell world 1 deercell holds 25 regular cells so
		// deercell
		// world is 1/5 the x and y dimension
		for (i = 0; i < getWorldYSize() / 5; i++) {
			for (j = 0; j < getWorldXSize() / 5 + 1; j++) {
				// printf("deer y = %d, deer x = %d\n", i, j);

				// initialize new cell
				DeerCell deercell = new DeerCell();
				deercell.setWorld(deerWorld);
				deercell.setXY(j, i);
				deercell.setInitDeer(4.2509f);
				deercellList.add(deercell);
			}
		}

		deerWorld.setOverwriteWarnings(true);
	}

	public void initDataBase() {
		// init database
		// which_yield values: 0 use data file
		// 1 random normal around avg
		// 2 user supplied
		// 3 peaked

		database = new Database();
		database.setWorld(world);
		database.setDeerWorld(deerWorld);
		database.setMySwarm(this);
		database = database.createEnd();

		// init water resources
		buildDatabase("VEPI_data/hydro.data", "setWaterType");
		buildDatabase("VEPI_data/FVPSprings.data", "setSpringType");

		// init elevation data
		buildDatabase("VEPI_data/dem.data", "setElevation");

		// init weather station data
		buildDatabase("VEPI_data/station.data", "setStation");

		// init soil information
		buildDatabase("VEPI_data/soil.data", "setSoilType");
		buildDatabase("VEPI_data/degrade.data", "setSoilDegrade");

		// init productivity of specific species by soil
		buildSingleArray("VEPI_data/deer.data", "setDeerProd");
		buildSingleArray("VEPI_data/shrubdeer.data", "setShrubDeerProd");
		buildSingleArray("VEPI_data/grassdeer.data", "setGrassDeerProd");
		buildSingleArray("VEPI_data/treedeer.data", "setTreeDeerProd");
		buildSingleArray("VEPI_data/hare.data", "setHareProd");
		buildSingleArray("VEPI_data/shrubhare.data", "setShrubHareProd");
		buildSingleArray("VEPI_data/grasshare.data", "setGrassHareProd");
		buildSingleArray("VEPI_data/treehare.data", "setTreeHareProd");
		buildSingleArray("VEPI_data/rabbit.data", "setRabbitProd");
		buildSingleArray("VEPI_data/shrubrabbit.data", "setShrubRabbitProd");
		buildSingleArray("VEPI_data/grassrabbit.data", "setGrassRabbitProd");
		buildSingleArray("VEPI_data/treerabbit.data", "setTreeRabbitProd");
		buildSingleArray("VEPI_data/tree.data", "setTreeProd");
		buildSingleArray("VEPI_data/shrub.data", "setShrubProd");

		// init almagre productivity correction by year
		database.setDataFile("VEPI_data/almagre600_1983.data");
		database.setArrayFileLength(1383);
		database.setAlmagreArray();
		database.closeDataFile();

		// init prin productivity correction by year
		database.setDataFile("VEPI_data/prin600_1983.data");
		database.setArrayFileLength(1383);
		database.setPrinArray();
		database.closeDataFile();

		// init productivity correction based on soil suitability codes
		buildSingleArray("VEPI_data/SCMRed.data", "setScmr");

		// init standing crop values for vegetation
		buildDatabase("VEPI_data/SCtree.data", "setSCtreeProd");
		buildDatabase("VEPI_data/SCshrub.data", "setSCshrubProd");

		// init Pueblo period site locations
//		buildDatabase("VEPI_data/p1.data", "setSiteType1");
//		buildDatabase("VEPI_data/p2.data", "setSiteType2");
//		buildDatabase("VEPI_data/p3.data", "setSiteType3");

		// init Pueblo period site proximities
//		buildDatabase("VEPI_data/p1prox.data", "setSiteProx1");
//		buildDatabase("VEPI_data/p2prox.data", "setSiteProx2");
//		buildDatabase("VEPI_data/p3prox.data", "setSiteProx3");

		// init Model Period site locations
//		buildDatabase("VEPI_data/mp6.data", "setSiteType6");
//		buildDatabase("VEPI_data/mp7.data", "setSiteType7");
//		buildDatabase("VEPI_data/mp8.data", "setSiteType8");
//		buildDatabase("VEPI_data/mp9.data", "setSiteType9");
//		buildDatabase("VEPI_data/mp10.data", "setSiteType10");
//		buildDatabase("VEPI_data/mp11.data", "setSiteType11");
//		buildDatabase("VEPI_data/mp12.data", "setSiteType12");
//		buildDatabase("VEPI_data/mp13.data", "setSiteType13");
//		buildDatabase("VEPI_data/mp14.data", "setSiteType14");
//		buildDatabase("VEPI_data/mp15.data", "setSiteType15");
//		buildDatabase("VEPI_data/mp16.data", "setSiteType16");
//		buildDatabase("VEPI_data/mp17.data", "setSiteType17");
//		buildDatabase("VEPI_data/mp18.data", "setSiteType18");
//		buildDatabase("VEPI_data/mp19.data", "setSiteType19");

		// setup for reading in yields
		database.setDataFile(Village.DATAFILE);
		database.setDataFileLength(701);
		database.setSelMethod("setMaizePotential");
		database.setWhichYield(which_yield);
		database.setRandomYield(getRandom_yield());
		database.initYields();
	}

	/** This function tells the database how to read in certain files
	 * 
	 * @param file
	 * @param method
	 */
	public void buildDatabase(String file, String method) {
		database.setDataFile(file);
		database.setDataFileLength(1);
		database.setSelMethod(method);
		database.setWhichYield(-1);
		database.updateCellWorld();
		database.closeDataFile();
	}


	public void systemStatsReset(){
		Cell.tot_deer=0;
		Cell.tot_hare=0;
		Cell.tot_rabbits=0;
		Cell.tot_SC=0;
		Cell.tot_DW=0;
		Cell.tot_pop=0;
	}

	/**
	 * 
	 * @param file
	 * @param method
	 */
	public void buildSingleArray(String file, String method) {
		database.setDataFile(file);
		database.setArrayFileLength(193);
		database.setSelMethod(method);
		database.updateCWSingle();
		database.closeDataFile();
	}

	/**
	 * 
	 */
	public void debug() {
		for (Object agt : agentList) {
			Agent a = (Agent) agt;
			if (a.getTag() < debug_i) {
				debug_i = getAllAgents() - 1;
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	public int getAd_plots() {
		return ad_plots;
	}

	/**
	 * 
	 * @return
	 */
	public int getAgentCount() {
		return getNumAgents();
	}

//	public int getWarDead() {
//		return getWarDeadNum();
//	}


//	public int getCmMonitorCost() {
//		return cm_monitor_cost;
//	}
//
//	public double getGroup_benefit_growth_rate() {
//		return group_benefit_growth_rate;
//	}

	//	public double getChange_rate() {
	//		return change_rate;
	//	}

	public int getMaxCoopRadiusBrn() {
		return max_coop_radius_brn;
	}

	/** These methods provide access to the objects inside the ModelSwarm.
	 * These objects are the ones visible to other classes via message call.
	 * In theory we could just let other objects use Probes to read our state,
	 * but message access is frequently more convenient.
	 * 
	 * @return
	 */
	public ArrayList<Agent> getAgentList() {
		return agentList;
	}

	public int getAllAgents() {
		return allAgents;
	}

	public ArrayList<Cell> getCellList() {
		return cellList;
	}

	public ArrayList<DeerCell> getDeerCellList() {
		return deercellList;
	}

	public int getEconomy() {
		return economy;
	}

	public int getEstAgents() {
		est_i++;
		if (est_i > -1) {
			if (est_i < 400) {
				return est[est_i / 40];
			} else {
				return est[10];
			}
		} else {
			return est[0];
		}
	}

	/** Frequency of fileI/O
	 * 
	 * @return
	 */
	public int getExperimentDuration() {
		return experimentDuration;
	}

	public int getFile_ID() {
		return File_ID;
	}

	public int getFileID() {
		return File_ID;
	}

	public double getHarvest_adjustment() {
		return harvest_adjustment;
	}

	public double getHarvestAdjust() {
		return harvest_adjustment;
	}

	public int getHunting_radius() {
		return hunting_radius;
	}

	public int getHuntingRadius() {
		return hunting_radius;
	}

	public int getSoilDegrade() {
		return soil_degrade;
	}

	public ArrayList<Cell> getLocalcellList() {
		// DC: getting rid of this, so let it return an empty list
		return new ArrayList<Cell>();
		//return localcellList; 
	}

	/**
	 * @return the need_meat
	 */
	public int getNeed_meat() {
		return need_meat;
	}

	public boolean isDomestication() {
		return domestication;
	}

	public void setDomestication(boolean domestication) {
		this.domestication = domestication;
	}

	public boolean isTurkey_water() {
		return turkey_water;
	}

	public void setTurkey_water(boolean turkey_water) {
		this.turkey_water = turkey_water;
	}

	public double getTurkey_maize_per() {
		return turkey_maize_per;
	}

	public void setTurkey_maize_per(double turkey_maize_per) {
		this.turkey_maize_per = turkey_maize_per;
	}

	public int getProportion_maize_turkey_diet() {
		return proportion_maize_turkey_diet;
	}

	public void setProportion_maize_turkey_diet(int proportion_maize_turkey_diet) {
		this.proportion_maize_turkey_diet = proportion_maize_turkey_diet;
	}

	public int getNumAgents() {
		numAgents = agentList.size();

		return numAgents;
	}

//	public int getWarDeadNum() {
//		return warDead; 
//	}

	public int getP_need() {
		return p_need;
	}

	public int getP_penalty() {
		return p_penalty;
	}

	public boolean getPopulate_exchange_array_list() {
		return populate_exchange_array_list;
	}

	public void setPopulate_exchange_array_list(boolean populate_exchange_array_list) {
		if(economy == 4)
		{
			this.populate_exchange_array_list = populate_exchange_array_list;
		}
	}

	public boolean getTrade_boosted_move() {
		return trade_boosted_move;
	}

	public void setTrade_boosted_move(boolean trade_boosted_move) {
		if(economy == 4)
		{
			this.trade_boosted_move = trade_boosted_move;
		}
	}

	public int getTrigger_coop_state() {
		return trigger_coop_state;
	}

	public void setTrigger_coop_state(int input) {
		this.trigger_coop_state = input;
	}

	public float getCoop_rate() {
		return coop_rate;
	}

	public void setCoop_rate(float coop_rate) {
		this.coop_rate = coop_rate;
	}

	/**Estimates changed for new pop numbers
	 * functions in older versions called int getMinEstAgents and int
	 * getMaxEstAgents
	 * 
	 * Numbers calculated for Varien Paper on Pop estimates for area
	 * 
	 * @return
	 */
	public int getPopEstAgentsMethod1() {
		if (getTime() > oldyear) {
			oldyear = getTime();
			pop_est_agents_i++;
		}
		if (pop_est_agents_i < 702) {
			if (pop_est_agents_i < 126) {
				return min_est[0];
			} else if (pop_est_agents_i < 201) {
				return min_est[1];
			} else if (pop_est_agents_i < 241) {
				return min_est[2];
			} else if (pop_est_agents_i < 281) {
				return min_est[3];
			} else if (pop_est_agents_i < 321) {
				return min_est[4];
			} else if (pop_est_agents_i < 381) {
				return min_est[5];
			} else if (pop_est_agents_i < 421) {
				return min_est[6];
			} else if (pop_est_agents_i < 461) {
				return min_est[7];
			} else if (pop_est_agents_i < 501) {
				return min_est[8];
			} else if (pop_est_agents_i < 541) {
				return min_est[9];
			} else if (pop_est_agents_i < 581) {
				return min_est[10];
			} else if (pop_est_agents_i < 626) {
				return min_est[11];
			} else if (pop_est_agents_i < 661) {
				return min_est[12];
			} else if (pop_est_agents_i < 701) {
				return min_est[13];
			}

		} else {
			return min_est[14];
		}
		return min_est[14];
	}

	/**
	 * 
	 * @return
	 */
	public int getPopEstAgentsMethod2() {
		// Numbers calculated for Varien Paper on Pop estimates for area
		if (getTime() > oldyear2) {
			oldyear2 = getTime();
			pop_est_meth_i2++;
		}
		if (pop_est_meth_i2 < 702) {
			if (pop_est_meth_i2 < 126) {
				return min_est2[0];
			} else if (pop_est_meth_i2 < 201) {
				return min_est2[1];
			} else if (pop_est_meth_i2 < 241) {
				return min_est2[2];
			} else if (pop_est_meth_i2 < 281) {
				return min_est2[3];
			} else if (pop_est_meth_i2 < 321) {
				return min_est2[4];
			} else if (pop_est_meth_i2 < 381) {
				return min_est2[5];
			} else if (pop_est_meth_i2 < 421) {
				return min_est2[6];
			} else if (pop_est_meth_i2 < 461) {
				return min_est2[7];
			} else if (pop_est_meth_i2 < 501) {
				return min_est2[8];
			} else if (pop_est_meth_i2 < 541) {
				return min_est2[9];
			} else if (pop_est_meth_i2 < 581) {
				return min_est2[10];
			} else if (pop_est_meth_i2 < 626) {
				return min_est2[11];
			} else if (pop_est_meth_i2 < 661) {
				return min_est2[12];
			} else if (pop_est_meth_i2 < 701) {
				return min_est2[13];
			}

		} else {
			return min_est2[14];
		}
		return min_est2[14];
	}

	/**
	 * 
	 * @return
	 */
	public int getPopEstAgentsMethod3() {
		// Numbers calculated for Varien Paper on Pop estimates for area
		if (getTime() > oldyear3) {
			oldyear3 = getTime();
			pop_est_meth_i3++;
		}
		if (pop_est_meth_i3 < 702) {
			if (pop_est_meth_i3 < 126) {
				return min_est3[0];
			} else if (pop_est_meth_i3 < 201) {
				return min_est3[1];
			} else if (pop_est_meth_i3 < 241) {
				return min_est3[2];
			} else if (pop_est_meth_i3 < 281) {
				return min_est3[3];
			} else if (pop_est_meth_i3 < 321) {
				return min_est3[4];
			} else if (pop_est_meth_i3 < 381) {
				return min_est3[5];
			} else if (pop_est_meth_i3 < 421) {
				return min_est3[6];
			} else if (pop_est_meth_i3 < 461) {
				return min_est3[7];
			} else if (pop_est_meth_i3 < 501) {
				return min_est3[8];
			} else if (pop_est_meth_i3 < 541) {
				return min_est3[9];
			} else if (pop_est_meth_i3 < 581) {
				return min_est3[10];
			} else if (pop_est_meth_i3 < 626) {
				return min_est3[11];
			} else if (pop_est_meth_i3 < 661) {
				return min_est3[12];
			} else if (pop_est_meth_i3 < 701) {
				return min_est3[13];
			}

		} else {
			return min_est3[14];
		}
		return min_est3[14];
	}

	public int getProteinNeed() {
		return p_need;
	}

	public int getProteinPenalty() {
		return p_penalty;
	}

	public int getRandom_yield() {
		return random_yield;
	}

	@Override
	public Schedule getSchedule() {
		return schedule;
	}

	public int getTime() {
		return time;
	}

	public int getWhich_yield() {
		return which_yield;
	}

	public VillageSpace getWorld() {
		return world;
	}

	public int getWorldTime() {
		return getTime();
	}

	public int getDeerXSize() {
		return deerXSize;
	}

	public int getDeerYSize() {
		return deerYSize;
	}

	public int getWorldXSize() {
		return worldXSize;
	}

	public int getWorldYSize() {
		return worldYSize;
	}

	public void log(String str, String file, boolean useYr) {
		logger.write(str, file, useYr, getWorldTime());
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
//		System.out.printf("cm_monitor_cost = %d\n", cm_monitor_cost); //HooperVill
		System.out.printf("DOMESTICATION = %b\n", domestication);
		System.out.printf("TURKEY_WATER = %b\n", turkey_water);
		System.out.printf("TURKEY_MAIZE_PER = %f\n", turkey_maize_per);
//		System.out.printf("group_benefit_growth_rate = %f\n", group_benefit_growth_rate); //HooperVill
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
//					"cm_monitor_cost," +
					"DOMESTICATION," + 
					"TURKEY_WATER," + 
					"TURKEY_MAIZE_PER\n");
//					"group_benefit_growth_rate\n");
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
//					this.cm_monitor_cost + "," +
					this.domestication + "," + 
					this.turkey_water + "," +
					this.turkey_maize_per + "\n");
//					this.group_benefit_growth_rate + "\n");
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

//				/* This cleans up HooperAgents that either died
//				 * or were depleted (people left).  We just make sure
//				 * they are removed from their groups.
//				 */
//				if (Village.AGENT_TYPE == Village.HOOPER_AGENTS) {
//					BeyondHooperAgent ha = (BeyondHooperAgent) t;
//					if (ha.getGroup() != null)
//						ha.setGroup(null);
//				}
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

	public void setAllAgents(int allAgents) {
		this.allAgents = allAgents;
	}

	public void setExperimentDuration(int experimentDuration) {
		this.experimentDuration = experimentDuration;
	}

	public void setFile_ID(int val) {
		File_ID = val;
	}

	public void setNumAgents(int numAgents) {
		this.numAgents = numAgents;
	}

//	public void setWarDead(int warDead) {
//		this.warDead = warDead;
//	}

//	public void setGroup_benefit_growth_rate(int rate) { // TK: added for HooperVill sweep
//		this.group_benefit_growth_rate = rate;
//	}

	//	public void setChange_rate(double rate) { // TK: added for HooperVill sweep
	//		this.change_rate = rate;
	//	}

//	public void setCm_Monitor_Cost(int cm_monitor_cost) { // TK: added for HooperVill sweep
//		this.cm_monitor_cost = cm_monitor_cost;
//	}

	//	public void setMax_Coop_Radius_Brn(int max_coop_radius_brn) { // TK: added for HooperVill sweep
	//		this.max_coop_radius_brn = max_coop_radius_brn;
	//	}

	public void setEconomy(int econ) {
		economy = econ;
	}

	public void setP_need(int p_need) {
		this.p_need = p_need;
	}

	public void setP_penalty(int p_penalty) {
		this.p_penalty = p_penalty;
	}

	public void setHunting_radius(int hunting_radius) {
		this.hunting_radius = hunting_radius;
	}

	public void setHarvest_adjustment(double harvest_adjustment) {
		this.harvest_adjustment = (float) harvest_adjustment;
	}

	public void setSoilDegrade (int soil_degrade) {
		this.soil_degrade = soil_degrade;
	}

	public void setNeed_meat(int need_meat) {
		this.need_meat = need_meat;
	}

	public void setState_good(float state_good) {
		this.state_good = state_good;
	}

	public void setAd_plots(int ad_plots) {
		this.ad_plots = ad_plots;
	}

	public void setRandom_yield(int random_yield) {
		this.random_yield = random_yield;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public void setWhich_yield(int which_yield) {
		this.which_yield = which_yield;
	}

	public void setWorldXSize(int worldXSize) {
		this.worldXSize = worldXSize;
	}

	public void setWorldYSize(int worldYSize) {
		this.worldYSize = worldYSize;
	}


	/** Shuffles the agent list.  Uses a REPAST random seed to ensure repeatability of results */
	protected void shuffleAgents(ArrayList<?> cloneList) {
		Collections.shuffle(cloneList, new java.util.Random(Village.uniformIntRand(0, Integer.MAX_VALUE)));		
	}

	public void stopRunning() {		
		endSim = true;

		this.getController().stopSim(); // Terminate the simulation.
	}

	public void updateYear() {	
		time++;
		int pop = 0;
		database.setWorldTime(time);
		database.setPopLevel(agentList.size());
		
		if (time>1299) {
			System.out.printf("\n\n *** WHOA! Look at the time! Pack up the kids, we're flying south! ***\n\n");
			this.stopRunning();
		}

		pop = agentList.size();
		System.out.printf("YEAR: %d\n", time);
		System.out.println("population: " + pop);
//		if(Village.AGENT_TYPE==Village.HOOPER_AGENTS){
//			System.out.println("Groups: " + hooper2.getGroups().size() + "\n");
//		}
	}

	public float getState_good() {
		return state_good;
	}

	public WaterManager getWaterManager() {
		return waterManager;
	}

	public Agent searchAgentList(int ptag) {
		Agent targetAgent = null;		
		int listsize = agentList.size();
		int i;

		if (listsize > 0) {
			for (i = 0; i < listsize; i++) {              
				targetAgent = (Agent) agentList.get(i);             

				if (targetAgent.getTag() == ptag) {
					return targetAgent;
				}
			}
		}

		if (Village.DEBUG)
			System.err.printf("Was unable to find an agent #%d\n", ptag);
		return null;
	}

	/**
	 * @return the hooper2
	 */
//	public BeyondHooperMaster getHooper2() {
//		return hooper2;
//	}

	/**
	 * @param hooper2 the hooper2 to set
	 */
//	public void setHooper2(BeyondHooperMaster hooper2) {
//		this.hooper2 = hooper2;
//	}

	//    /** Resets static variables in classes in the project */
	//    private void resetStatics() {
	//        Database.resetStatics();
	//
	//        if (Village.AGENT_TYPE == Village.HOOPER_AGENTS) {
	//            HooperAgent.resetStatics();
	//        } else if (Village.AGENT_TYPE == Village.REGULAR_AGENTS) {
	//            Agent.resetStatics();
	//        }
	//    }

	public int getNumMales(){
		ArrayList<Integer> eligibleGenders = new ArrayList<Integer>();
		for (EligibleRecord person : Eligible.getEligibleList()){
			eligibleGenders.add(person.getGender());
		}	

		int males = Collections.frequency(eligibleGenders,1);
		return males;

	}

	public int getNumFemales(){
		ArrayList<Integer> eligibleGenders = new ArrayList<Integer>();
		for (EligibleRecord person : Eligible.getEligibleList()){
			eligibleGenders.add(person.getGender());
		}

		int females = Collections.frequency(eligibleGenders,0);
		return females;

	}

	public int yFromCell(int cell){
		int rownr = (int)((cell - 1)/this.getWorldXSize()) + 1;
		return rownr;
	}
	public int xFromCell(int cell){
		int colnr = (int)(cell - ((yFromCell(cell) - 1) * this.getWorldXSize()));
		return colnr;
	}
}
