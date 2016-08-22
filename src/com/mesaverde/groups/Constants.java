package com.mesaverde.groups;

import com.mesaverde.model.AgentModelSwarm;
import com.mesaverde.village.Village;

public final class Constants {
	/* All numbers are in CALDAYs, which is a standard measure of the cost to support
	 * an average family (2 adults + 2 kids) for 1 day.
	 */
	public static AgentModelSwarm mySwarm;

	/** Output group and HooperAgent statistics? */
	public static final boolean OUTPUT = true;
	
	/** Merge, fight, and pay tribute? */
	public static final boolean MERGE_AND_FIGHT = true;
	
	/**Allow groups in a complex hierarchy to revolt against their dominant group? */
	public static final boolean REVOLT = true; 

	/** Play Hooper PG Game? */
	public static final boolean HOOPER_PG_GAME = true;
	
	/**If k-means is set to true, in BeyondGroup groups will fission using a k-means algorithm
	 * If this is set to false, they will fission via a bud-off algorithm */
	public static final boolean K_MEANS_CLUSTERING = true;
	
	/** GROUP_SIZE - A limit to the size of a group. If a group exceeds this size, the agent farthest from 
	 * the geometric centroid of the group will bud off and create a new group. */
    public static final int GROUP_SIZE = 50;
    
	/** The probability of death from fighting
	 */
	public static final double S = 0.05;
	
	/** The direct tax on the benefit from the public goods game */
	public static final double BETA = 0.5;

	/** The tax on the tribute received */
	public static final double MU = 0.5;
	
	/**If Alliances is set to true groups in the same lineage will not attack one another */
	public static final boolean ALLIANCES = false;
	
	/** PG Game Epigenetic effect? 
	 * When this is true, agent offspring choose their hierarchical 
	 * preference based on their parents experience in the PG game
	 * in the last year. If the parent household's benefits from the 
	 * game outweighed their costs, they adopt their parent's preference.
	 * If the costs outweighed the benefits, they adopt the opposite preference.
	 */
	public static final boolean PG_OUTCOME_EPIGENETIC_EFFECT = true;
	
//    /** The amount that agents are willing to change in one iteration with social learning */
    public static final double CHANGE_RATE = 0.2;
	
	
	/** CALDAY - amount of calories to feed a family for one day (5435: enough for 2 kids, mom & dad for 1 day) */
    public static final double CALDAY = 5435.0;

    /** CALDAY converted into maize-days (number of kg of maize to support a family for one day), approx. 1.5 kg 
     * multiplying by this converts days to kg */
	public static final double KG_DAY = CALDAY / Village.MAIZE_KG_CAL;
	
	/** Growth rate for benefits as group size increases 
	 * This is k in the function suggested by Kyle which in R is b <- bm * e^(-k/n) where bm = bmax = 60 
	 * smaller values (e.g., 2) increase b faster as n increases*/
	public static final double GROUP_BENEFIT_GROWTH_RATE = 2;
	
	/** Increased storage capabilities for leaders in Beyond Hooperville */
	public static final int leader_STORAGE = 1; //multiplier used in Agent.java so the leader can have more storage. 1 means the storage is the same as the other group members
	//2 means it is doubled. 1.5 is 50% more than other members
		
	/** Maximum benefit produced by contributing to the public good */
	public static final double b_BENEFIT = 73; //was 60
	
	/** Cost of contributing to the public good */
	public static final int c_COST = 37; //was 20
	
	/** Cost imposed on defectors (i.e. sanction or punishment; Hooper's s) */
	public static final int s_SANCTION = 56; //was 30
	
	/** Cost imposed on non-taxpayers; Hooper's s-hat) */
	public static final int s_TAX_SANCTION = 56;
	
	/** Cost of monitoring one group member; Hooper's c-sub-m */
	public static final int cm_MONITOR_COST = 4; //was 6
	
	/** Cost of punishing one defector; Hooper's c-sub-s */
	public static final int cs_SANCTION_COST = 11; 
	
	/** Cost of punishing one non-taxpayer; Hooper's c-hat-sub-s */
	public static final int cs_TAX_SANCTION_COST = 11;
	
	/** Cost of walking to punish defectors; set to 1 because it's multiplied by the # of cells between two agents */
	public static final int cs_Sanction_Dist = 1;
	
	/** The minimum a leader can charge in tax */
	public static final double TAX_RATE_MIN = cm_MONITOR_COST / b_BENEFIT;
	
	/** The maximum a leader can charge in tax */
	public static final double TAX_RATE_MAX = 1 - c_COST / b_BENEFIT;
	
	/** The W coefficient determines whether we are operating in Lanchester's linear
	 * or Square law.
	 * 1 is linear
	 */
	public static final double W = 1;
	
	/** Only men fight? */
	public static final boolean MEN_ONLY_FIGHT = true;
	
	/** Minimum fighting age. */
	public static final int MIN_FIGHT_AGE = 15;
	
	/** Maximum fighting age. */
	public static final int MAX_FIGHT_AGE = 50;

	static double requiredContribution = Constants.c_COST * Constants.KG_DAY;
	
	/** Draw individual households on the group display? */
	public static final boolean DRAW_AGENTS = true;
}
