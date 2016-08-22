package com.mesaverde.domestication;

/**
 * Turkey domestication parameters.<p>
 * 
 * This class handles the constants for turkey domestication. Certain parameters, such as 
 * {@code TURKEY_MAIZE_PER} and {@code TURKEY_WATER} may be controlled through the Repast 
 * GUI, or through a parameter file.<p>
 * 
 * Domestication itself is toggled in the {@link com.mesaverde.village.Village Village} class.<p>
 * 
 * @author R. Kyle Bocinsky <bocinsky@wsu.edu>
 * @version $Date: 2010/11/22 $
 * @since 1.0
 */
public class DomesticationParameters {

	public static final int NO_DOMESTICATION = 0;
	public static final int STRICT_ECONOMIC_MODEL = 1;
	public static int DOMESTICATION_STRATEGY = STRICT_ECONOMIC_MODEL;
	
	/**
	 * Amount of edible meat per average sized turkey, in grams (13 lbs, 4128 grams of meat).
	 * US Department of Agriculture [www.nal.usda.gov].
	 */
	public static final int PROTEIN_PER_TURKEY = 1238;
	
	/**
	 * Annual turkey caloric need.
	 * This is based on numerous assumptions, for the dietary need of wild turkeys 
	 * is not well documented.  Firstly, I take the average dietary need of turkey
	 * to be 8oz of food.  Using the MAIZE_KG_CAL conversion factor of 3560 kcal/kg,
	 * I arrive at a daily caloric need of turkey of ~ 800 Cal, assuming all calories
	 * came from maize.  This assumption is tempered by the TURKEY_MAIZE_PER adjustment.
	 * 
	 */
	public static final int BASE_CAL_TURKEY = 292000;
	
	/**
	 * Amount of turkey diet assumed to come from maize under domestication.  Following 
	 * Robin Lyle, I assume 50% - 70% of annual turkey diet came from maize stores.
	 */
	public static final double TURKEY_MAIZE_PER = 0.5;
	
	/**
	 * This equation calculates the return rate for keeping turkey, which is a constant 
	 * in the current (11/22/2010) model. We assume turkey are fed for a complete year, and that they receive 
	 * TURKEY_MAIZE_PER of their caloric needs from maize. This value is in grams/Cal expended, 
	 * and is used as the cutoff point where agents will switch to a domestication strategy.
	 */
	public static final double TURKEY_RR = PROTEIN_PER_TURKEY/(BASE_CAL_TURKEY * TURKEY_MAIZE_PER);
	
	/**
	 * Whether or not to require agents to provide water for their turkey.
	 */
	public static final boolean TURKEY_WATER = false;
	
	/**
	 * Amount of water, in kg, per day needed by a single turkey.  This is calculated at 2x the 
	 * amount of food, by weight, needed per day.  Actual turkey water consumption appears to be 
	 * very poorly recorded.  This number comes from a professional turkey hatchery.
	 * 
	 * http://www.millerhatcheries.com/Information/Turkeys/turkey_rearing.htm
	 */
	public static final double TURKEY_WATER_NEED = 0.454;
	
}
