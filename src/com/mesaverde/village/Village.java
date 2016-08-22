package com.mesaverde.village;

import uchicago.src.sim.util.Random;
// this is a test.
public class Village {
// ***************** OUTPUT PARAMETERS ***************** //
	/**
	 * Model Run Identifier used in DataBase.*
	 */
	public static final int FILESUFX = 55;
	
	/** DATA OUTPUT FOR STATISTICAL ANALYSES  
	 * Agent-stats record annual variables for each household,
	 * while System_stats outputs global variables, such as the number of households on the landscape.
	 **/
	
	/**
	 * prints out agent statistics; true = print, false = no output
	 */
	public static final boolean OUTPUT = true;
	public static final boolean OUTPUT_TRADE = false;
	public static final boolean PRINT_AGENT_STATS = true;
	public static final boolean PRINT_SYSTEM_STATS = true;
	public static final boolean PRINT_RUN_PARAMETERS= true;
	
	/**
	 * output and data directories.
	 */
	public static final String OUTPUT_DIR = "output";
	public static final String DATA_DIR = "VEPI_data";
	
	/**
	 * Selects the version of the GUI to run, if being run as GUI
	 * 
	 * 0 - All graphs, landscapes, and options
	 * 1 - GUI for looking at group formation
	 * 2 - GUI for looking at the impact of turkey domestication and protein use
	 */
	public static final int ALL = 0;
	public static final int HOOPER = 1;
	public static final int TURKEY = 2;
	public static int GUI_VERSION = ALL; // was HOOPER;
	
	/**
	 * true= write log cellnnn.out, false=no log (faster)
	 */
	public static final boolean LOG_CELL = false;
	
	/**
	 * Log foodnnn.out
	 */
	public static final boolean LOG_FOOD = false;
	
	
// ***************** WORLD PARAMETERS ***************** //
	/** 
	 * World size in 200m x 200m cells. 
	 */
	public static final int WORLD_X_SIZE = 227;
	public static final int WORLD_Y_SIZE = 200;
	
	/**
	 * UTM coordinates of cell (0,0) 12/04
	 */
	public static final int UTMY = 4165786;
	public static final int UTMX = 676599;

	/**
	 * AgentModelSwarm file for database
	 * use "VEPI_data/year600-1300.dat" for PDSI only
	 * use "VEPI_data/al_year600-1300.dat" for Almagre temperature correction
	 * use "VEPI_data/pr_year600-1300.dat" for prin temperature correction
	 */
	public static final String DATAFILE = DATA_DIR + "/al_year600-1300.dat";
	
	
// ***************** PROTEIN ***************** //	
	/**
	 * number of grams of protein needed by each person in the agent
	 */
	public static final int PROTEIN_NEED = 5;
	
	/**
	 * 1 = penalty to birth/death rates ie, life table mortality/natality used opposed to state good bonus 0 = no penalty
	 */
	public static final int PROTEIN_PENALTY = 1;
	
	/** Controls agent movement depending on availability of protein resources.
	 * 1 = only move to cells where hunting is possible
	 * 0 = can move to cells where no meat was found
	 */
	public static final boolean NEED_MEAT = false;
	
	/** The core parameters controlling animal presence and hunting in the village model.
	 * 
	 * Each toggles a class that extends a general "Animal" class. Animals that require
	 * diffusion and larger cells (Deer) also have classes that extend the Cell
	 * class.
	 */
	public static final boolean DEER = true;
	public static final boolean RABBIT = true;
	public static final boolean HARE = true;

	/**
	 * protein per deer = 10800 based on USDA
	 * database protein on 36kg of cooked deer
	 * meat ("Game meat, deer, cooked, roasted")
	 * 36kg is meat weight of 60 kg deer
	 * Hobbs and Swift 1985, Simms 1987, Christenson 1981
	 */
	public static final double PROTEIN_PER_DEER = 10800;
	
	/**
	 * protein per hare = 455.4 g based on
	 * usda database protein on 1.38 kg of
	 * cooked rabbit meat ("Game meat, rabbit, wild, cooked, stewed")
	 * 1.38 kg is meat weight of 2.3 kg hare
	 * Haskell and Reynolds 1947, Simms 1987, Christenson 1981
	 */
	public static final double PROTEIN_PER_HARE = 455.4;
	
	/**
	 * protein per hare = 177.2 g based
	 * on usda database protein on
	 * 537g of cooked rabbit meat ("Game meat, rabbit, wild, cooked, stewed")
	 * 537g is meat weight of 895g rabbit
	 * Haskell and Reynolds 1947, Simms 1987, Christenson 1981
	 */
	public static final double PROTEIN_PER_RABBIT = 177.2;
	
	/**
	 * protein decrement year-to-year in storage
	 */
	public static final float PROTEIN_STORAGE_DECR = 0.75f;
	
	/**
	 * kcal per kg protein; based on USDA data for uncooked wild deer meat.
	 */
	public static final int PROTEIN_KG_CAL = 1200;
	
	/**
	 * amount of maize storage need to become a philanthropist
	 * @param protein_max_store
	 * @return
	 */
	public static final float PROTEIN_PHIL_THRESHHOLD(float protein_max_store) {
		return (protein_max_store * 1.0f);
	}
	
	/**
	 * percentage of food rabbits can use
	 */
	public static final float RABBIT_USE = .7f;
	
	/**
	 * 
	 */
	public static final float HARE_USE = .7f;
	
	/**
	 * percentage of food deer can use
	 */
	public static final float DEER_USE = .5f;
	
	/**
	 * float intrinsic rate of increase for deer
	 */
	public static final float DEER_R = .4f;
	
	/**
	 * float intrinsic rate of increase for hares
	 */
	public static final float HARE_R = 1.75f;
	
	/**
	 * float intrinsic rate of increase for rabbits (MYERS 1964)
	 */
	public static final float RABBIT_R = 2.3f;
	
	/**
	 * amount of food (Megagrams) eaten by deer in single year
	 */
	public static final float DEER_K = .55f;
	
	/**
	 * amount of food (Megagrams) eaten by hares in single year
	 */
	public static final float HARE_K = .04453f;
	
	/**
	 * amount of food (Megagrams) eaten by rabbits in single year
	 */
	public static final float RABBIT_K = .06935f;
	
	/**
	 * 
	 */
	public static final float HARE_WEIGHT = 1.38f;
	
	/**
	 * 
	 */
	public static final float RABBIT_WEIGHT = .537f;

	/**
	 * "false" = no diffusion
	 * "true" = diffusion
	 */
	public static final boolean DIFFUSION = true;
	
	/**
	 * "true" = explicit deer diffusion
	 * "false" = implicit deer diffusion
	 */
	public static final boolean EXPLICIT = false;
	
	/**
	 * these variables are used to test the 
	 * regrowth of animals and plants in
	 * a cell where everything has been destroyed
	 **/
	
	/**
	 * Perform Deer Test that kills off deer pop in an area
	 */
	public static final int DEER_TEST = 0;
	
	/**
	 * Create Flat File of deer populations in deer cells
	 */
	public static final int DEER_OUTPUT = 0;
	
	/**
	 * 
	 */
	public static final int HUNTERS = 1;
	
	/**
	 * 
	 */
	public static final int HLAG_HUNT = 2;
	
	/**
	 * 
	 */
	public static final int RLAG_HUNT = 5;
	
	/**
	 * 
	 */
	public static final int DEER_DISTANCE = 20;
	
	/**
	 * radius for hunting
	 */
	public static final int HUNT_SRADIUS = 20;

	/**
	 * 
	 */
	public static final int MAX_CELLS_HUNTED = 50;
	
	/**
	 * under protein penalty 2, this number is 
	 * the year agent dies if deficient for entire time
	 */
	public static final int DEATH_YEAR = 3;
	
	/**
	 * Initial protein storage endowment multiplier per family member to a new agent.
	 * Defines amount of protein allotted beyond a year's storage.
	 * So, 1.33 is 1 1/3 years of protein to get the agent started.
	 * This accounts for spoilage.
	 */
	public static final double PROTEIN_ENDOWMENT = 1.33;
	

// ***************** MAIZE and PRODUCTIVITY ***************** //
	/**
	 * Note: VW max harvests are >3 times higher than max harvests
	 * calculated by Kohler et al. 1986, Orcutt (various) and Wetterstrom,
	 * so for comparability purpose we define an adjustment factor allowing
	 * these to be scaled according to earlier estimates. To use Van West's
	 * estimates, set ADJUST_FACTOR=1; to scale them to be roughly equal to
	 * earlier estimates, use ADJUST_FACTOR=3.
	 * GENERAL BACKGROUND: Wetterstrom (1986:47) suggests yields of about
	 * 600 kg/ha for Arroyo Hondo. At this productivity, assuming 60% of
	 * Cal are from maize, 1 ha would feed about 5 people using 2000 Cal/year.
	 * Therefore, if we were using Wetterstrom's productivities, one of our
	 * .4 ha (1-ac) plots would feed about 2 people. Very roughly, 1 ha feeds
	 * 1 household (very generously).
	 */
	public static final float HARVEST_ADJUST_FACTOR = 0.75f;

	/**
	 * Toggle for soil degradation
	 * 0=no degradation;
	 * 1=light degrade down to 30%
	 * 2=heavy degrade down to 60%
	 */
	public static final int SOIL_DEGRADE = 1; 
	
	/**
	 * if >1, assumes fields planted every other
	 * year, or "thinly". Carla used this adjustment
	 * in her carrying capacity calculations
	 * 1 makes no adjustment, 2 divides yields
	 * in half, etc.
	 */
	public static final float FALLOW_FACTOR = 1.0f;
	
	/**
	 * max plots of .4 ha (about 1 ac) per cell
	 */
	public static final int MAX_PLOTS = 9;
	
	/**
	 * Number of additional plots an agent can plant above the number of workers in the agent
	 */
	public static final int AD_PLOTS = 2;
	
	/**
	 * Initial maize storage endowment per family member to a new agent, in kg
	 */
	public static final int MAIZE_ENDOWMENT = 300;
	
	/**
	 * Initial maize maximum storage per family member, in kg.
	 * 
	 * see Maize Use by Rural Mesoamerican Household in Human Organization
	 * 49(2):135-139
	 * by James W. Stuart (1990) for various estimates of maize intake in
	 * g/person/day
	 * estimates of 300-500 g/person/day appear plausible
	 * at .4 Kg/person/day, a family of 4 needs 584 kg/year maize
	 */
	public static final int MAIZE_INITIAL_MAX = 350; //kg
	
	/**
	 * maize decrement year-to-year in storage
	 */
	public static final float MAIZE_STORAGE_DECR = 0.1f;
	
	/**
	 * This is the average cost in calories it takes to plant
	 * and maintain 1 plot excluding harvest costs
	 */
	public static final int AVG_FARM_COST = 101760;
	
	/**
	 * kcal per kg; dividing by this converts kcal to kg
	 */
	public static final int MAIZE_KG_CAL = 3560;
	
	/**
	 * prop of diet assumed to come from maize
	 * adjustment for MAIZE_PER is applied in Agent.m:
	 * to -eatMaize (draws down maize_storage by only
	 * MAIZE_PER of cals_spent);
	 * to E(use kg) reported by Debug;
	 * in -evalState to calculate max_stor, used in setting household state;
	 * in computing value for cals returned (EXP_cal_need) by -calcExpCalUse
	 * 
	 * in computing exp_needed in calcFarmPl
	 * base are for a 3-month season, drawing on Wing & Brown 1979:17-72
	 * base estimates are for completely sedentary people
	 * these are unadjusted for MAIZE_PER
	 */
	public static final float MAIZE_PER = 0.7f;
	

// ***************** EXCHANGE ***************** //
//	/**
//	 * 0=Disable Reciprocity/Cooperation between households;
//	 * 1=Enable (request ?)
//	 * 2=Philanthropic reciprocity model
//	 * 3=Both 1 and 2
//	 * 4=Balanced Reciprocity (With 3)
//	 */
	public static final int COOP = 4;
	
	/**
	 * number of times to request food in GRN and BRN network
	 */
	public static final int COOP_ATTEMPTS = 8;
	
	/**
	 * 2: mem+roulette
	 * 1: roulette learning
	 * 0: random
	 */
	public static final int COOP_LEARNING = 1;
	
	/**
	 * Chance that an agent would cooperate 
	 * Affects GRN & BRN 
	 */
	public static final float COOP_RATE = 1.00f;

	/**
	 * Chance that an agent would cooperate on the GRN network (0=no coop on GRN)
	 */
	public static final int GRN_COOP_RATE = 1;

	/**
	 * 
	 */
	public static final boolean COOP_DEBUG = false;
	
	/**
	 * 
	 */
	public static final boolean BRN_COOP_DEBUG = false;
	
	/**
	 * Allow the agent to ask for food n times a year
	 */
	public static final int MAX_COOP_COUNT_PER_ANNUM = 4;

	/**
	 * amount of maize storage need to become a philanthropist
	 * @param max_store
	 * @return
	 */
	public static final float PHIL_THRESHHOLD(float max_store) {
		return (max_store * 1.0f);
	}
	


	/**
	 * philanthropy donation radius 20
	 */
	public static final int PHIL_RADIUS = 15;
	
	/**
	 * % reward in terms of increased yearly maximum storage limits
	 */
	public static final float PHIL_INCREASE = 0.01f;
	
	/**
	 * Number of philanthropic attempts
	 */
	public static final int PHIL_N = 2;

	/**
	 * COOP STATES used by coop_state
	 */
	public static final int CRITICAL = 1;
	public static final int HUNGRY = 2;
	public static final int SATISFIED = 4;
	public static final int PHILANTHROPIST = 8;
	public static final int FULL = 16;
	public static final int DEAD = 0;

	/**
	 * use bitwise or, then use bitwise and to check
	 * @return
	 */
	public static int TRIGGER_DONATE() {
		return FULL;
	}

    public static int TRIGGER_DONATE_MAIZE() {
        return TRIGGER_DONATE();
    }

    public static int TRIGGER_DONATE_PRO() {
        return TRIGGER_DONATE();
    }

    public static int TRIGGER_REQUEST() {
		return CRITICAL | HUNGRY;
		//return mySwarm.getTrigger_coop_state();
	}

    public static int TRIGGER_REQUEST_MAIZE() {
		return TRIGGER_REQUEST();
	}

    public static int TRIGGER_REQUEST_PRO() {
		return TRIGGER_REQUEST();
	}

	public static int REQUEST_FROM() {
		return (FULL | PHILANTHROPIST | SATISFIED);
	}

    public static int REQUEST_FROM_MAIZE() {
        return REQUEST_FROM();
    }

    public static int REQUEST_FROM_PRO() {
        return REQUEST_FROM();
    }

	/** DC: Changing to 10 as per discussion with Dr. Kohler
	 * 
	 */
	public static final int MAX_TRADER_LIST_SIZE = 10;
	
	/**
	 * 
	 */
	public static final int MAX_TRADE_NEIGHBOR_SEARCH = 5;
	
	/**
	 * 
	 */
	public static final int MAX_TRADE_ATTEMPTS = 1;
	public static final float TRADE_INTEREST_RATE = 0.1f; // not implemented yet
	public static final int DEAD_TRADER_CLEANUP_FREQUENCY = 1; // clean dead links every 50/4 years - was 4
	public static final int NEIGHBOR_TRADER_UPDATE_FREQUENCY = 1; // was 4
	//public static final int TRADE_COOP_RATE 0.5        // Chance that an agent would cooperate

	/* HubNET */

	public static final int MAX_HLIST_LIST_SIZE = 42; //initially 42
	public static final int MAX_HLIST_RADIUS = 20;
	public static final int MAX_HLIST_NEIGHBOR_SEARCH = 40;
	public static final int MAX_HLIST_ATTEMPTS = 1;
	public static final int DEAD_HLIST_CLEANUP_FREQUENCY = 4; // clean dead links every 50/4 years
	public static final int NEIGHBOR_HLIST_UPDATE_FREQUENCY = 4;

	public static final int HUBNET_UNION = 0; // U and Intersect are mutually exclusive, pick one or the other.
	public static final int HUBNET_INTERSECT = 1; // they define if a hub is based on either GRN or BRN (union), or both (Inters.)

	/**
	 * Limit the amount of maize and protein that can be traded
	 */
	public static final int MAX_EXCHANGE_AMOUNT = 50;
	
	/**
	 * grams protein
	 */
	public static final int MAX_PROTEIN_EXCHANGE_AMOUNT = 10000;
	
	/**
	 * Split max_coop_radius into 2 variables,
	 * one for GRN and one for BRN
	 */
	public static final int MAX_COOP_RADIUS_GRN = 30;
	
	/**
	 * Agents will go further for BRN
	 */
	public static final int MAX_COOP_RADIUS_BRN = 40;
	
	/**
	 * 
	 */
	public static final int PHIL_THRESHHOLD = 0;	

	/**
	 * 
	 * @param max_store
	 * @return
	 */
	public static final double PHIL_THRESHHOLD(int max_store) {
		return max_store;
	}

	/**
	 * 
	 * @return
	 */
	public static int DONATE_TO() {
		return CRITICAL | HUNGRY | SATISFIED;
	}

	/**
	 * 
	 * @return
	 */
	public static int DONATE_TO_MAIZE() {
		return DONATE_TO();
	}

	/**
	 * 
	 * @return
	 */
	public static int DONATE_TO_PRO() {
		return DONATE_TO();
	}

	
// ***************** CULTURAL ALGORITHMS ***************** //
	/** New Belief space and exchange variables **/
    
    /**
     * Toggle Cultural Algorithm ON="true"; OFF="false"
     */
	public static final boolean CA_ENABLE = true;
	
	/**
	 * Toggle Situational Knowledge in BeliefSpace ON=TRUE;OFF=FALSE
	 */
	public static final boolean SITUATIONAL = true;
	
	/**
	 * Toggle Normative Knowledge in BeliefSpace ON="true"; OFF="false"
	 */
	public static final boolean NORMATIVE = true;
	
	/**
	 * Toggle Historical/temporal Knowledge in BeliefSpace ON="true"; OFF="false"
	 */
	public static final boolean HISTORY = false;

	/**
	 * custom memory of positive interactions
	 */
	public static final int MEMORY = 0;

	/**
	 * Rate an Agent is to be influenced by the BeliefSpace
	 */
	public static final float INFLUENCE_PROB = 0.8f;
	
	/**
	 * Rate an Agent will be evaluated for considering it in the BeliefSpace
	 */
	public static final int ACCEPTANCE_PROB = 1;
	
	/**
	 * Mutation rate for the belief space knowledge
	 */
	public static final float MUTATION = 0.01f;
	
	/**
	 * Maximum number of Exemplars in the BeliefSpace
	 */
	public static final int MAX_EXAMPLAR = 50;

	/**
	 * Agent memory
	 * How many successful exchange partners to remember (max)
	 */
	public static final int MAX_MEM_INTERACTION = 1;

	/**
	 * ENABLE AGENT TO DIRECT ITS MOVE
	 */
	public static final int MOVE_STRATEGY = 0;

	
	
	
	/**
	 * (possible values 1-4), used by agents to determine what kind of water resources needed
	 */
	public static final int H2O_TYPE = 2;
	
	/**
	 * default .1
	 * natality/mortality revision for good times, negative for bad times
	 */
	public static final float STATE_GOOD = 0.1f;
	
	
	
	
	
	/** BEGIN OTHER PARAMETERS FOR STANDARD RUN **/
	

	
	
	//TODO: WHAT ARE THESE?
	public static final int wrapX(int dx, int x, int worldX) {
		return (x + dx + worldX) % worldX;
	}

	public static final int wrapY(int dy, int y, int worldY) {
		return (y + dy + worldY) % worldY;
	}

    /** 
     * In cells/hour
	 * 1 km = 5 cells so 15 = 3 km/hour.
	 * Value given here overridden in calcTravelCal()
	 */
	public static final int TRAVEL_SPEED = 15;
	
	/**
	 * search radius for water in cells
	 */
	public static final int H2O_RAD = 2;
	
	/**
	 *  "true" to make agents consider water distribution in -evalCell (Agent.m), otherwise false
	 */
	public static final boolean NEED_H2O = true;
	
	/**
	 * "true" = enable social moves, otherwise "false"
	 */
	public static final boolean SOCIAL = false;
	
	/**
	 * radius searched in -moveHouse (Agent.m)
	 * default is 20
	 */
	public static final int MOVE_RAD = 40;

	/**
	 * radius searched when daughter household formed
	 */
	public static final int BUD_OFF = (int) MOVE_RAD / 2;
	

		
	/**
	 * max n households per cell
	 */
	public static final int HOUSE_LIMIT = 200;


	

	
	

	
	/**
	 * 1872 Cal * 91.25 days
	 */
	public static final int BASE_CAL_MAN = 170820;
	
	/**
	 * 1560 Cal * 91.25 days
	 */
	public static final int BASE_CAL_WOM = 142350;
	
	/**
	 * 1000 Cal * 91.25 days
	 */
	public static final int BASE_CAL_KID = 91250;
	
	/**
	 * caloric increments per hour work
	 */
	public static final int WORK_CAL_MAN = 240;
	public static final int WORK_CAL_WOM = 200;
	public static final int WORK_CAL_KID = 92;

	/**
	 * Amount able to be carried in one trip in kg
	 */
	public static final int CARRY_CAPACITY = 20;
	
	/**
	 * Number of hours it takes to collect 1 load of deadwood fuel
	 */
	public static final int DW_COLLECT = 1;
	
	/**
	 * Number of hours (was 2) it takes to collect 1 load of standing crop fuel
	 */
	public static final int SC_COLLECT = 4;
	
	/**
	 * length of search array used for movement
	 */
	public static final int S_ARRAY = 100;


	

	


	/**
	 * End of the Pueblo I period ala Pecos Classification
	 */
	public static final int PIYEAR = 910;
	
	/**
	 * End of the Pueblo II period ala Pecos Classification
	 */
	public static final int PIIYEAR = 1140;
	
	/**
	 * End of the Pueblo III period ala Pecos Classification
	 */
	public static final int PIIIYEAR = 1280;
	
	/**
	 * Ending year for present simulation.
	 */
	public static final int PIVYEAR = 1281;

	/**
	 * End of Phase 6 period Crow Canyon
	 */
	public static final int P6YEAR = 725;
	
	/**
	 * End of Phase 7 period Crow Canyon
	 */
	public static final int P7YEAR = 800;
	
	/**
	 * End of Phase 8 period Crow Canyon
	 */
	public static final int P8YEAR = 840;
	
	/**
	 * End of Phase 9 period Crow Canyon
	 */
	public static final int P9YEAR = 880;
	
	/**
	 * End of Phase 10 period Crow Canyon
	 */
	public static final int P10YEAR = 920;
	
	/**
	 * End of Phase 11 period Crow Canyon
	 */
	public static final int P11YEAR = 980;
	
	/**
	 * End of Phase 12 period Crow Canyon
	 */
	public static final int P12YEAR = 1020;
	
	/**
	 * End of Phase 13 period Crow Canyon
	 */
	public static final int P13YEAR = 1060;
	
	/**
	 * End of Phase 14 period Crow Canyon
	 */
	public static final int P14YEAR = 1100;
	
	/**
	 * End of Phase 15 period Crow Canyon
	 */
	public static final int P15YEAR = 1140;
	
	/**
	 * End of Phase 16 period Crow Canyon
	 */
	public static final int P16YEAR = 1180;
	
	/**
	 * End of Phase 17 period Crow Canyon
	 */
	public static final int P17YEAR = 1225;
	
	/**
	 * End of Phase 18 period Crow Canyon
	 */
	public static final int P18YEAR = 1260;
	
	/**
	 * End of Phase 19 period Crow Canyon
	 */
	public static final int P19YEAR = 1280;

	
	/**
	 * turns on/off some debugging output for agent TAG
	 * false suppresses a lot of output
	 */
	public static final boolean DEBUG = false;
	public static final int TAG = 1;

	/**
	 * 1 = RANDOM
	 */
	public static final int MATE_SELECTION_METHOD = 1;
	
	/**
	 * 
	 */
	public static final int MAX_CHILD_LINKS = 20;
	
	/**
	 * 
	 */
	public static final int MAX_RELATIVE_LINKS = 20;

	/**
	 * HH_SIZE > 2 default is 10: lowest feasible value is 6
	 */
	public static final int HH_SIZE = 10;
	
	/**
	 * 
	 */
	public static final int INITIAL_AGENT_COUNT = 200;

	/**
	 * Max agents on landscape
	 */
	public static final int MAX_AGENT = 10000;

	/**
	 * 
	 */
	public static final int MALE = 1;
	
	/**
	 * 
	 */
	public static final int FEMALE = 0;
	
	/**
	 * 
	 */
	public static final int SINGLE = 0;
	
	/**
	 * 
	 */
	public static final int MARRIED = 1;

	/**
	 * 2: enable multiple variable search
	 * 1: enable greedy search in SearchNeighborhood
	 * 0: to disable and use regular approach
	 */
	public static final int GREEDY = 2;
	
	/**
	 * 
	 */
	public static final int MIN_MOVE_RAD = 0;
	
	/**
	 * 
	 */
	public static final int MAX_MOVE_RAD = 40;

	/**
	 * "true" = saves link files
	 * "false" = do not run
	 */
	public static final boolean SAVE_LINKS = false;

	// GAME
	public static final boolean ENABLE_DEFECTING_AGENT = false;
	
	/**
	 * 
	 */
	public static final int MAX_DEFECTORS = 1;
	
	/**
	 * 1: kinship only
	 * 2: trade only
	 * 3: defect in both
	 */
	public static final int DEFECTOR_TYPE = 3;
	
	/**
	 * allow children to inherit defection
	 */
	public static final int DEFECTOR_PROPAGATE_FAMILY = 1;

	/** definitions below used in animal and firewood production **/
	
		
	/**
	 * average percent of shrub standing crop that is new growth
	 */
	public static final float SHRUB_SC_PERCENT = .013f;
	
	/**
	 * average percent of shrub standing crop that is dead wood
	 */
	public static final float SHRUB_DW_PERCENT = .0335f;
	
	/**
	 * average percent of tree standing crop that is new growth
	 */
	public static final float TREE_SC_PERCENT = .013f;
	
	/**
	 * average percent of tree standing crop that is dead wood
	 */
	public static final float TREE_DW_PERCENT = .0655f;

	/**
	 * # of cells that household will search further 
	 * out to get deadwood before harvesting standing crop
	 */
	public static final int FW_SEARCH_DISTANCE = 10;
	


	/**
	 * 
	 */
	public static final int WOOD_NEED = 1130;
	
	/**
	 * 
	 */
	public static final int WATER_NEED = 3650;

	/**
	 * track number of times an agent got remarried, 
	 * allow MAX_REMARRIAGE_COUNT only then remove it.
	 */
	public static final int MAX_REMARRIAGE_COUNT = 2; 
	
	/**
	 * n = 5 track number of times and agent attempted 
	 * to get remarried, allow MAX_REMARRIAGE_ATTEMPTS 
	 * only then remove it.
	 */
	public static final int MAX_REMARRIAGE_ATTEMPTS = 5; 

	/**
	 * Kill agents if they do not remarry after certain 
	 * attempts (code given to us has this on for some reason)
	 * toggling this switch with low numbers of 
	 * remarriage attempts drastically reduces population sizes.
	 */
	public static final boolean REMOVE_FAILED_REMARRIAGE = false;


	
	/**
	 * Perform NUKE test
	 * "true" = yes
	 * "false" = no
	 */
	public static final boolean NUKE = false;
	
	/**
	 * Year that Nuke test is performed
	 */
	public static final int NUKE_T = 705;
	
	/**
	 * X-coordinate of deer cell to be destroyed
	 */
	public static final int NUKE_X = 39;
	
	/**
	 * Y-coordinate of deer cell to be destroyed
	 */
	public static final int NUKE_Y = 5;


	/**
	 * creates average growth flat files for NPP and new deadwood
	 */
	public static final int FUEL_TEST = 0;

	/**
	 * Faster Hunting Search Algorithm
	 * This Algorithm reduces program run time by condensing the number of hunting searches
	 * when an agent decides to move.  Instead of hunting at every possible move location
	 * this algorithm will create a single number for all 25 cells in a 5 cell square.
	 */
	public static final boolean SPEED_MOVE = false;

	
	/**
	 * Initialize the random number generator
	 */
	static {
		Random.createUniform();
	}
	
	/** DC: returns a uniform random float in the range of [min,max) */
	public synchronized static double uniformDblRand(double min, double max) {
		return Random.uniform.nextDoubleFromTo(min, max);
	}

	/** DC: returns a uniform random int in the range of [min,max] */
	public synchronized static int uniformIntRand(int min, int max) {
		return Random.uniform.nextIntFromTo(min, max);
	}

	// DC: Needed for Specialization, since we now have different types of agents
	// 0 - Non-Specialized Agents
	// 1 - Specialized Agents
	// 2 - Hooper Agents
	public static final int REGULAR_AGENTS = 0;
	public static final int SPECIALIZED_AGENTS = 1;
	public static final int HOOPER_AGENTS = 2;
	public static int AGENT_TYPE = REGULAR_AGENTS;
	public static final boolean ENABLE_LEARNING_FOR_HOOPER = true;
	
	//these following variables are for testing Hooperville in Stefani Crabtree SAA 04/08/14
	public static final boolean POPULATE_EXCHANGE_ARRAY_LIST = true;
    /** true indicates we will use proximity of trade partners to boost the
     * move value of a cell.
     */
	public static final boolean TRADE_BOOSTED_MOVE = false;
	public static final int TRIGGER_COOP_STATE = 0;

    /* DC: Since I'm aiming to merge usage of the code from the specialization
     * branch, we need to be able to turn on and off multithreading
     */
    public static boolean ENABLE_MULTITHREADING = false;


	/**
	 * 0=Disable Reciprocity/Cooperation between households;
	 * 1=Enable (request ?)
	 * 2=Philanthropic reciprocity model
	 * 3=Both 1 and 2
	 * 4=Balanced Reciprocity (With 3)
	 */
//	public static final int COOP = 0; 
    public static final boolean HUNTING = true;
    public static final boolean DOMESTICATION = true;
    public static final boolean PROTEIN_TRADE = true;
	public static final boolean BURN_WOOD = true;
	public static final boolean DRINK_WATER = true;
	
}
