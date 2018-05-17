package com.mesaverde.village;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.locks.*;

import com.mesaverde.hunting.*;
import uchicago.src.sim.engine.CustomProbeable;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.util.ProbeUtilities;
import com.mesaverde.model.AgentModelSwarm;
import com.mesaverde.model.ObserverAgentModel;

public class Cell implements Drawable, CustomProbeable {

	VillageSpace world; // 1.2

	VillageSpace deerworld; // 1.2
	private// 1.2
	//coordinate locations for cell
	float UTM_N;
	private float UTM_E;
	int x, y; // coordinates in world
	private// coordinates in world
	int deer_X;
	private int deer_Y;

	private int lineageID;// The LineageID of the cell if it has been claimed as the territory of an agent

	AgentModelSwarm mySwarm;
	HashSet<Agent> settlerSet; // set of agents on individual cell

	int world_x_size = Village.WORLD_X_SIZE;
	int world_y_size = Village.WORLD_Y_SIZE;

	private int HHnumber;
	int occupied; // History of Occupation by Agents

	private int occuAnnum;
	int proxtotal;
	private int occuprox;
	int occuprox1;
	int occuprox2;
	int occuprox3;
	int worldTime;
	int display;
	int adisplay;
	int total_occupation;
	int CC_years;
	int BH_years;
	int HH_years;

	//variables used for calculating vegetation productivity
	double adjust_factor; //adjustment factor based on data being used in the model

	int cold_proxy; //way to determine which cold proxy to use

	double cold_corr; //adjustment to maize production based on elevation

	private int maize_pot; // Possible Veg Production in lieu of soil degradation
	private int maize_prod;
	int veg_pot;
	int plant_pot;
	float almagre; //Almagre correction factor

	float prin; //Almagre correction factor

	float scmr; //Soil Correction maize reduction from soil restriction codes in Soil Survey Database selected soil interpretation for hand planting for soils with a U-unsuitable

	//water resource variables
	int water_type; // water type info

	private float water; //amount of water

	float max_water; //max amount of water

	int elevation; // UTM N & E elevation in m

	private int FVPsoil; // soil type changed from soil to FVPsoil to reflect new soil codes
	String station; //Weather station

	int stnNum; // weather station 1 = Bluff, 2 = Cortez, 3 = Yellowjacket, 4 = Mesa Verde

	//variables used for production, diffusion, and hunting of animals
	float deer_prod; // prod values are percentages of total veg production that are used by those resources
	float gdeer_prod;
	float sdeer_prod;
	float tdeer_prod;
	float hare_prod;
	float ghare_prod;
	float share_prod;
	float thare_prod;
	float rabbit_prod;
	float grabbit_prod;
	float srabbit_prod;
	float trabbit_prod;
	float harefood; // amount of food in a given step for these animals
	float sharefood;
	float tharefood;
	float rabbitfood;
	float srabbitfood;
	float trabbitfood;
	float deerfood;
	float sdeerfood;
	float tdeerfood;
	int Imgrabbits;
	int Imghares;
	float Imgdeer;

	//variables used for production of firewood on landscape
	float tree_prod;
	float shrub_prod;

	private float fireWood; //JAC 12/01/04 amount of Standing crop
	int shrubgrowth;
	int treegrowth;
	int grassgrowth;
	float SCtree; //standing crop of trees in Kg

	float SCshrub; //standing crop of shrubs in Kg

	float DWtree; //amount of tree deadwood in Kg

	float DWshrub; //amount of shrub deadwood in Kg

	float DWWood; //JAC 12/01/04 amount of dead wood

	float ImgDwWood; //JAC 12/01/04 amount of DW used for calc imaginary fuel costs

	float ImgScWood; //JAC 12/01/04 amount of SC used for calc imaginary fuel costs

	private//JAC 12/01/04 amount of SC used for calc imaginary fuel costs
	int degrade; // percent maize_pot is degraded
	private// Boolean if 0 then no degradation; if 1 the degrade.
	float degradeFactor;
	int sitetype1; // presence/absence PI sites

	private int siteprox1;
	int sitetype2; // presence/absence PII sites

	private int siteprox2;
	int sitetype3; // presence/absence PIII sites

	private int siteprox3;

	// Number of households and locations of sites during individual periods (6-19)
	private int currentHH; // graphics variable used when cell is probed to tell the number of households in cell based on MPperiod

	int sitetype6;
	int sitetype7;
	int sitetype8;
	int sitetype9;
	int sitetype10;
	int sitetype11;
	int sitetype12;
	int sitetype13;
	int sitetype14;
	int sitetype15;
	int sitetype16;
	int sitetype17;
	int sitetype18;
	int sitetype19;
	double avgyieldcounter;
	FileWriter out;
	int lastplot;

	private int farming_plots; // num plots used for farming
	float maxfuel;
	float fwprod_corr;
	int comcenter; //yes = 1

	int wateruse;
	int deadAgents;

	//JAC 2/7 Fuel test variables
	float fuelGrowth;
	float deadGrowth;

	private AnimalTracker animalTracker = new AnimalTracker();

	//11/04 JAC
	public static int hampop = 0;
	public static int bighampop = 0;
	public static int communpop = 0;
	public static int population = 0;
	public static float hamper = 0;
	public static float bigper = 0;
	public static float commper = 0;
	//JAC 2/05 these public static variables are for the nuke test
	public static int n_rabbit = 0;
	public static int n_hare = 0;
	public static float n_firewood = 0;
	public static float n_DWWood = 0;
	public static int n_tprod = 0;
	public static int n_sprod = 0;
	//JAC 3/05 productivity variables
	public static float[][] prod_by_soil = new float[194][700];
	public static int[] numsoil = new int[194];
	public static int timed = 0;
	//JAC 6/05 total amounts
	public static int resetdata = 600;
	public static float tot_deer = 0;
	public static int tot_hare = 0;
	public static int tot_rabbits = 0;
	public static float tot_SC = 0;
	public static float tot_DW = 0;
	//JAC 3/06 total pop
	public static int tot_pop = 0;

	private static final int[] output_time = { 609, Village.P6YEAR, Village.P7YEAR,
		Village.P8YEAR, Village.P9YEAR, Village.P10YEAR, Village.P11YEAR,
		Village.P12YEAR, Village.P13YEAR, Village.P14YEAR, Village.P15YEAR,
		Village.P16YEAR, Village.P17YEAR, Village.P18YEAR, Village.P19YEAR }; 

	// Locking constructs
	private Lock woodLock;
	private Lock waterLock;
	private Lock huntLock;
	private Lock farmLock;
	private DeerCell deerCell;

	public static void resetStatics() {
		hampop = 0;
		bighampop = 0;
		communpop = 0;
		population = 0;
		hamper = 0;
		bigper = 0;
		commper = 0;
		//JAC 2/05 these  variables are for the nuke test
		n_rabbit = 0;
		n_hare = 0;
		n_firewood = 0;
		n_DWWood = 0;
		n_tprod = 0;
		n_sprod = 0;
		//JAC 3/05 productivity variables
		prod_by_soil = new float[194][700];
		numsoil = new int[194];
		timed = 0;
		//JAC 6/05 total amounts
		resetdata = 600;
		tot_deer = 0;
		tot_hare = 0;
		tot_rabbits = 0;
		tot_SC = 0;
		tot_DW = 0;
		//JAC 3/06 total pop
		tot_pop = 0;
	}

	// creation stuff
	public Cell() {				
		this.setLineageID(0);
		// init cell state, no usage yet
		farming_plots = 0;
		lastplot = 0;
		//JAC 9/25/04  initializes adjust factor based on what Village.DATAFILE is being used
		//TAK designed to reduce average productivity in beanfields to about
		//500 kg/ha
		if (Village.DATAFILE.equals("VEPI_data/year600-1300.dat")) {
			adjust_factor = .554f;
			cold_proxy = 0;
		} else if (Village.DATAFILE.equals("VEPI_data/al_year600-1300.dat")) {
			adjust_factor = .682f;
			cold_proxy = 1;
		} else if (Village.DATAFILE.equals("VEPI_data/pr_year600-1300.dat")) {
			adjust_factor = .693f;
			cold_proxy = 2;
		} else {
			System.err
			.println("ERROR: Unknown Village.DATAFILE used Model requires maize adjust factor ");
			System.exit(-1);
		}

		// Will create resource locks if multithreading is enabled
		createLocks();
	}



	public synchronized void addDeadAgent() {
		deadAgents++;
	}

	// agent manipulation
	public synchronized void addSettler(Agent agent) {
		if (agent != null)
			settlerSet.add(agent);

	}

	// num = number of plots to change in the cell
	// ag_num = number of plots a given agent has previously vested in this plot
	public void changeFarmPl(int num) {  
		if (Village.ENABLE_MULTITHREADING) {
			getFarmLock().lock();
			farming_plots += num;
			getFarmLock().unlock();
		} else {
			farming_plots += num;
		}

		if (farming_plots < 0) {
			System.err
			.print(String
					.format(
							"ERROR: (%d,%d) farm ha less than than 0 (have %d, want %d)\n",
							x, y, farming_plots, num));
			System.exit(-1);
		}
		if (farming_plots > 10) {
			System.err
			.print(String
					.format(
							"ERROR: (%d,%d) farm ha greater than 4 (have %d, want %d)\n",
							x, y, farming_plots, num));
			System.exit(-1);
		}
	}

	/** DC: 5/14/08 - moved most of the stuff from createEnd to the constructor */
	public void createEnd() {
		if (settlerSet == null) {
			System.err.println("ERROR: (" + x + "," + y
					+ ") no settler cell exists");
			System.exit(-1);
		}        
	}

	// debugging stuff
	public void debugX(int inx, int iny) {
		if ((x == inx && y == iny) || inx == -1) {
			System.out.printf("x: %d y: %d settlers: %d planted: %d\n", x, y,
					settlerSet.size(), farming_plots);
		}

	}

	@Override
	public void draw(SimGraphics r) { // 1.2
		int t, s;

		switch (display) {
		case 0:
			t = maize_prod / 40; // Carla's values

			if (t < 0)
				t = 0;

			if (t > 49)
				t = 49;

			r.drawFastRect(getMappedColor(4 + t));
			break;

		case 1:
			t = (elevation - 1500) / 22;
			if (t > 49)
				t = 49;

			r.drawFastRect(getMappedColor(104 + t));
			break;

		case 2:
			t = (int) (FVPsoil / 2.26);
			if (t > 49)
				t = 49;

			r.drawFastRect(getMappedColor(54 + t));
			break;

		case 3:
			t = water_type;
			if (water_type > 3)
				t = 3;

			if (water_type == 1)
				t = 0;

			r.drawFastRect(getMappedColor(t));
			break;

		case 4:
			t = maize_prod / 40; // possibly degraded values

			if (t > 49)
				t = 49;

			r.drawFastRect(getMappedColor(4 + t));
			s = water_type;
			if (water_type > 3)
				s = 3;

			if (water_type > 1) {
				r.drawFastRect(getMappedColor(s));
			}
			break;

		case 5:
			t = (elevation - 1500) / 22;
			if (t > 49)
				t = 49;

			r.drawFastRect(getMappedColor(104 + t));
			s = water_type;
			if (water_type > 3)
				s = 3;

			if (water_type > 1)
				r.drawFastRect(getMappedColor(s));

			break;

		case 6:
			t = (int) (FVPsoil / 2.26);
			if (t > 49)
				t = 49;

			r.drawFastRect(getMappedColor(54 + t));
			s = water_type;
			if (water_type > 3)
				s = 3;

			if (water_type > 1)
				r.drawFastRect(getMappedColor(s));

			break;

		case 7:
			t = maize_prod / 40; // possibly degraded values

			if (t < 0)
				t = 0;

			if (t > 49)
				t = 49;

			r.drawFastRect(getMappedColor(4 + t));
			s = water_type;
			if (water_type > 3)
				s = 3;

			if (water_type > 1)
				r.drawFastRect(getMappedColor(s));

			if (sitetype1 > 0)
				r.drawFastRect(getMappedColor(158));

			break;

		case 8:
			t = maize_prod / 40; // possibly degraded values

			if (t < 0)
				t = 0;

			if (t > 49)
				t = 49;

			r.drawFastRect(getMappedColor(4 + t));
			s = water_type;
			if (water_type > 3)
				s = 3;

			if (water_type > 1)
				r.drawFastRect(getMappedColor(s));

			if (sitetype2 > 0)
				r.drawFastRect(getMappedColor(159));

			break;

		case 9:
			t = maize_prod / 40; // possibly degraded values

			if (t < 0)
				t = 0;

			if (t > 49)
				t = 49;

			r.drawFastRect(getMappedColor(4 + t));
			s = water_type;
			if (water_type > 3)
				s = 3;

			if (water_type > 1)
				r.drawFastRect(getMappedColor(s));

			if (sitetype3 > 0)
				r.drawFastRect(getMappedColor(160));

			break;

		case 10:
			t = water_type;
			if (water_type > 3)
				t = 3;

			r.drawFastRect(getMappedColor(t));
			if (sitetype1 > 0)
				r.drawFastRect(getMappedColor(158));

			break;

		case 11:
			t = water_type;
			if (water_type > 3)
				t = 3;

			r.drawFastRect(getMappedColor(t));
			if (sitetype2 > 0)
				r.drawFastRect(getMappedColor(159));

			break;

		case 12:
			t = water_type;
			if (water_type > 3)
				t = 3;

			r.drawFastRect(getMappedColor(t));
			if (sitetype3 > 0)
				r.drawFastRect(getMappedColor(160));

			break;

		case 13:
			t = elevation / 75;
			if (t < 0)
				t = 0;

			if (t > 49)
				t = 49;

			r.drawFastRect(getMappedColor(104 + t));
			s = water_type;
			if (water_type > 3)
				s = 3;

			if (water_type > 1)
				r.drawFastRect(getMappedColor(s));

			if (sitetype1 > 0)
				r.drawFastRect(getMappedColor(158));

			break;

		case 14:
			t = elevation / 75;
			if (t < 0)
				t = 0;

			if (t > 49)
				t = 49;

			r.drawFastRect(getMappedColor(104 + t));
			s = water_type;
			if (water_type > 3)
				s = 3;

			if (water_type > 1)
				r.drawFastRect(getMappedColor(s));

			if (sitetype2 > 0)
				r.drawFastRect(getMappedColor(159));

			break;

		case 15:
			t = elevation / 75;
			if (t < 0)
				t = 0;

			if (t > 49)
				t = 49;

			r.drawFastRect(getMappedColor(104 + t));
			s = water_type;
			if (water_type > 3)
				s = 3;

			if (water_type > 1)
				r.drawFastRect(getMappedColor(s));

			if (sitetype3 > 0)
				r.drawFastRect(getMappedColor(160));

			break;

		case 16:
			t = occupied / 2;
			if (t < 0)
				t = 0;

			if (t > 49)
				t = 49;

			r.drawFastRect(getMappedColor(4 + t));
			break;

		case 17:
			t = occuAnnum / 2;
			if (t < 0)
				t = 0;

			if (t > 49)
				t = 49;

			r.drawFastRect(getMappedColor(4 + t));
			break;

		case 18:
			t = getSiteProx1();
			if (t < 0)
				t = 0;

			if (t > 49)
				t = 49;

			r.drawFastRect(getMappedColor(153 - t));
			break;

		case 19:
			t = getSiteProx2();
			if (t < 0)
				t = 0;

			if (t > 49)
				t = 49;

			r.drawFastRect(getMappedColor(153 - t));
			break;

		case 20:
			t = getSiteProx3();
			if (t < 0)
				t = 0;

			if (t > 49)
				t = 49;

			r.drawFastRect(getMappedColor(153 - t));
			break;

		case 21:
			t = occuprox;
			if (t < 0)
				t = 0;

			if (t > 49)
				t = 49;

			r.drawFastRect(getMappedColor(4 + t));
			break;

		case 22:
			t = degrade;
			if (t < 0)
				t = 0;

			if (t > 0)
				t = 70;

			r.drawFastRect(getMappedColor(t));
			break;

		case 23:
			t = (int) animalTracker.getAmount(Deer.class); //shows deer populations

			if (t < 0)
				t = 0;

			if (t > 49)
				t = 49;

			r.drawFastRect(getMappedColor(4 + t));
			break;

		case 24:
			t = animalTracker.getIntAmount(Rabbit.class); //shows rabbit populations

			if (t < 0)
				t = 0;

			if (t > 49)
				t = 49;

			r.drawFastRect(getMappedColor(4 + t));
			break;

		case 25:
			t = animalTracker.getIntAmount(Hare.class); //shows hare populations

			if (t < 0)
				t = 0;

			if (t > 49)
				t = 49;

			r.drawFastRect(getMappedColor(4 + t));
			break;

		case 26:
			t = (int) (shrub_prod * 100); //shows shrubs populations

			if (t < 0)
				t = 0;

			if (t > 49)
				t = 49;

			r.drawFastRect(getMappedColor(4 + t));
			break;

		case 27:
			t = (int) (tree_prod * 100); //shows tree populations

			if (t < 0)
				t = 0;

			if (t > 49)
				t = 49;

			r.drawFastRect(getMappedColor(4 + t));
			break;

		case 28:
			t = (int) (fireWood / 4000); //JAC 9/25/04 altered to allow greater variablity --shows amount of firewood on landscape

			s = (int) (DWWood / 500); //JAC 9/25/04 altered to allow greater variablity

			if (t < 0)
				t = 0;

			if (t > 49)
				t = 49;

			r.drawFastRect(getMappedColor(4 + t));
			if (DWWood > 0) {
				if (s < 0)
					s = 0;

				if (s > 24)
					s = 24;

				r.drawFastRect(getMappedColor(186 + s));
			}
			break;

		case 29:
			t = maize_prod / 40; //shows actual sites on landscape based on modeling period

			if (t < 0)
				t = 0;

			if (t > 49)
				t = 49;

			r.drawFastRect(getMappedColor(4 + t));

			if (worldTime < 1300) {
				if (worldTime < 725) {

					if (sitetype6 > 0)
						r.drawFastRect(getMappedColor(158));

					if (sitetype6 > 2)
						r.drawFastRect(getMappedColor(159));

					if (sitetype6 > 8) {
						r.drawFastRect(getMappedColor(160));
						drawRectNoMove(r, x - 1, y - 1, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));

					}

				} else if (worldTime < 800) {

					if (sitetype7 > 0)
						r.drawFastRect(getMappedColor(158));

					if (sitetype7 > 2)
						r.drawFastRect(getMappedColor(159));

					if (sitetype7 > 8) {
						r.drawFastRect(getMappedColor(160));
						drawRectNoMove(r, x - 1, y - 1, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));

					}

				} else if (worldTime < 840) {

					if (sitetype8 > 0)
						r.drawFastRect(getMappedColor(158));

					if (sitetype8 > 2)
						r.drawFastRect(getMappedColor(159));

					if (sitetype8 > 8) {
						r.drawFastRect(getMappedColor(160));
						drawRectNoMove(r, x - 1, y - 1, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));

					}
				} else if (worldTime < 880) {
					if (sitetype9 > 0)
						r.drawFastRect(getMappedColor(158));

					if (sitetype9 > 2)
						r.drawFastRect(getMappedColor(159));

					if (sitetype9 > 8) {
						r.drawFastRect(getMappedColor(160));
						drawRectNoMove(r, x - 1, y - 1, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));

					}

				} else if (worldTime < 920) {
					if (sitetype10 > 0)
						r.drawFastRect(getMappedColor(158));

					if (sitetype10 > 2)
						r.drawFastRect(getMappedColor(159));

					if (sitetype10 > 8) {
						r.drawFastRect(getMappedColor(160));
						drawRectNoMove(r, x - 1, y - 1, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));

					}

				} else if (worldTime < 980) {
					if (sitetype11 > 0)
						r.drawFastRect(getMappedColor(158));

					if (sitetype11 > 2)
						r.drawFastRect(getMappedColor(159));

					if (sitetype11 > 8) {
						r.drawFastRect(getMappedColor(160));
						drawRectNoMove(r, x - 1, y - 1, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));

					}

				} else if (worldTime < 1020) {
					if (sitetype12 > 0)
						r.drawFastRect(getMappedColor(158));

					if (sitetype12 > 2)
						r.drawFastRect(getMappedColor(159));

					if (sitetype12 > 8) {
						r.drawFastRect(getMappedColor(160));
						drawRectNoMove(r, x - 1, y - 1, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));

					}

				} else if (worldTime < 1060) {
					if (sitetype13 > 0)
						r.drawFastRect(getMappedColor(158));

					if (sitetype13 > 2)
						r.drawFastRect(getMappedColor(159));

					if (sitetype13 > 8) {
						r.drawFastRect(getMappedColor(160));
						drawRectNoMove(r, x - 1, y - 1, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));

					}

				} else if (worldTime < 1100) {
					if (sitetype14 > 0)
						r.drawFastRect(getMappedColor(158));

					if (sitetype14 > 2)
						r.drawFastRect(getMappedColor(159));

					if (sitetype14 > 8) {
						r.drawFastRect(getMappedColor(160));
						drawRectNoMove(r, x - 1, y - 1, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));

					}

				} else if (worldTime < 1140) {
					if (sitetype15 > 0)
						r.drawFastRect(getMappedColor(158));

					if (sitetype15 > 2)
						r.drawFastRect(getMappedColor(159));

					if (sitetype15 > 8) {
						r.drawFastRect(getMappedColor(160));
						drawRectNoMove(r, x - 1, y - 1, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));

					}

				} else if (worldTime < 1180) {
					if (sitetype16 > 0)
						r.drawFastRect(getMappedColor(158));

					if (sitetype16 > 2)
						r.drawFastRect(getMappedColor(159));

					if (sitetype16 > 8) {
						r.drawFastRect(getMappedColor(160));
						drawRectNoMove(r, x - 1, y - 1, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));

					}

				} else if (worldTime < 1225) {
					if (sitetype17 > 0)
						r.drawFastRect(getMappedColor(158));

					if (sitetype17 > 2)
						r.drawFastRect(getMappedColor(159));

					if (sitetype17 > 8) {
						r.drawFastRect(getMappedColor(160));
						drawRectNoMove(r, x - 1, y - 1, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));

					}

				} else if (worldTime < 1260) {
					if (sitetype18 > 0)
						r.drawFastRect(getMappedColor(158));

					if (sitetype18 > 2)
						r.drawFastRect(getMappedColor(159));

					if (sitetype18 > 8) {
						r.drawFastRect(getMappedColor(160));
						drawRectNoMove(r, x - 1, y - 1, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));

					}

				} else if (worldTime < 1300) {
					if (sitetype19 > 0)
						r.drawFastRect(getMappedColor(158));

					if (sitetype19 > 2)
						r.drawFastRect(getMappedColor(159));

					if (sitetype19 > 8) {
						r.drawFastRect(getMappedColor(160));
						drawRectNoMove(r, x - 1, y - 1, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));
						drawRectNoMove(r, x - 1, y, getMappedColor(160));

					}

				}
			}

			break;

		case 30:
			//shows what weather station cell belongs to.

			if (stnNum == 1)
				r.drawFastRect(getMappedColor(156));
			else if (stnNum == 2)
				r.drawFastRect(getMappedColor(155));
			else if (stnNum == 3)
				r.drawFastRect(getMappedColor(157));
			else
				r.drawFastRect(getMappedColor(47));
			break;

		case 31:
			t = 0;
			t = (int) max_water;
			if (max_water > 0)
				t = 3;

			r.drawFastRect(getMappedColor(t));
			break;

		default:
			System.err.println("ERROR: (" + x + "," + y
					+ ") display for cell not set correctly\n");
			System.exit(-1);
		}

		switch (adisplay) {

		case 0:
			break;

		case 1:

			//this code draws the agents on the landscape.  This is done through the cell as opposed to the agent drawing mechanism 
			//because we want to know the number of agents within a particular cell Jason 9/25/04
			if (HHnumber > 0)
				r.drawFastRect(getMappedColor(154));

			break;

		case 2:

			//this code draws the agents on the landscape.  This is done through the cell as opposed to the agent drawing mechanism 
			//because we want to know the number of agents within a particular cell Jason 9/25/04
			if (HHnumber > 0)
				r.drawFastRect(getMappedColor(154));

			if (HHnumber > 2)
				r.drawFastRect(getMappedColor(157));

			if (HHnumber > 8) {
				if (x == 0) {
					r.drawFastRect(getMappedColor(156));
					drawRectNoMove(r, x - 1, y, getMappedColor(156));
				} else {
					r.drawFastRect(getMappedColor(156));
					drawRectNoMove(r, x - 1, y - 1, getMappedColor(156));
					drawRectNoMove(r, x - 1, y, getMappedColor(156));
					drawRectNoMove(r, x - 1, y, getMappedColor(156));
				}
			}
			break;

		case 3:

			//this code draws the agents on the landscape.  This is done through the cell as opposed to the agent drawing mechanism 
			//because we want to know the number of agents within a particular cell Jason 9/25/04
			if (deadAgents > 0)
				r.drawFastRect(getMappedColor(154));

			break;

		case 4:

			//this code is for Kyle's thesis. It draws the agents in a cell by averaging the proportion of protein gained from 
			//turkey for each agent in the cell, and then assigns a color based on that number.
			if (worldTime < 1300) {
				if (worldTime < Village.P6YEAR) {

					if (sitetype6 > 0)
						r.drawFastRect(new Color(255, 255, 255));

				} else if (worldTime < Village.P7YEAR) {

					if (sitetype7 > 0)
						r.drawFastRect(new Color(255, 255, 255));

				} else if (worldTime < Village.P8YEAR) {

					if (sitetype8 > 0)
						r.drawFastRect(new Color(255, 255, 255));

				} else if (worldTime < Village.P9YEAR) {
					if (sitetype9 > 0)
						r.drawFastRect(new Color(255, 255, 255));

				} else if (worldTime < Village.P10YEAR) {
					if (sitetype10 > 0)
						r.drawFastRect(new Color(255, 255, 255));

				} else if (worldTime < Village.P11YEAR) {
					if (sitetype11 > 0)
						r.drawFastRect(new Color(255, 255, 255));

				} else if (worldTime < Village.P12YEAR) {
					if (sitetype12 > 0)
						r.drawFastRect(new Color(255, 255, 255));

				} else if (worldTime < Village.P13YEAR) {
					if (sitetype13 > 0)
						r.drawFastRect(new Color(255, 255, 255));

				} else if (worldTime < Village.P14YEAR) {
					if (sitetype14 > 0)
						r.drawFastRect(new Color(255, 255, 255));

				} else if (worldTime < Village.P15YEAR) {
					if (sitetype15 > 0)
						r.drawFastRect(new Color(255, 255, 255));

				} else if (worldTime < Village.P16YEAR) {
					if (sitetype16 > 0)
						r.drawFastRect(new Color(255, 255, 255));

				} else if (worldTime < Village.P17YEAR) {
					if (sitetype17 > 0)
						r.drawFastRect(new Color(255, 255, 255));

				} else if (worldTime < Village.P18YEAR) {
					if (sitetype18 > 0)
						r.drawFastRect(new Color(255, 255, 255));

				} else if (worldTime < Village.P19YEAR) {
					if (sitetype19 > 0)
						r.drawFastRect(new Color(255, 255, 255));
				}
			}
			if (HHnumber > 0) {
				double p = 0;
				double sum = 0;
				Iterator<Agent> i = this.getSettlerSet().iterator();
				while (i.hasNext()) {
					Agent next = (Agent) i.next();
					sum += next.getDomestication_protein_proportion();
				}
				p = 255*(sum/(HHnumber));
				Color col = new Color((int)p, 8, (int)(255-p));
				float xScale = r.getXScale();
				float yScale = r.getYScale();
				// System.out.printf("drawing parameters: [%f,%f]\n",xScale, yScale);
				r.drawFastRect(col);
				r.setDrawingCoordinates((x*6-xScale), (y*6-yScale), 0);
				r.drawFastRect(col);
				r.setDrawingCoordinates((x*6), (y*6-yScale), 0);
				r.drawFastRect(col);
				r.setDrawingCoordinates((x*6-xScale), (y*6), 0);
				r.drawFastRect(col);
				r.setDrawingCoordinates(x*6, y*6, 0);
				//r.setDrawingCoordinates(x+1, y+1, 0);
				//r.drawFastRect(col);
				//r.setDrawingCoordinates(x, y, 0);

				break;
			}
		}
	}

	/** DC: In the draw function, not every Cell was painting itself at its location, so we're going to temporarily
	 * change the location, draw, then change back.
	 * @param g
	 * @param locx
	 * @param locy
	 */
	private void drawRectNoMove(SimGraphics g, int locx, int locy, Color col) {
		int oldX = x, oldY = y;
		x = locx;
		y = locy;
		g.drawFastRect(col);
		x = oldX;
		y = oldY;
	}

	public AnimalTracker getAnimalTracker() {		
		return animalTracker;
	}

	public int getBHYears() {
		return BH_years;
	}

	public float getBigHamlet() {
		return bigper;
	}

	public int getCCYears() {
		return CC_years;
	}

	public int getCommunity() {
		return comcenter;
	}

	public float getCommunityPercent() {
		return commper;
	}

	public int getCurrentHH() {
		return currentHH;
	}

	public float getDeerfood() {
		return deerfood;
	}

	public int getDegrade() {
		return degrade;
	}

	public float getdegradeFactor() {
		return degradeFactor;
	}

	public float getDProteinPotential(int img) {

		if (img == 0) {
			double deerAmount = animalTracker.getAmount(Deer.class);
			if (deerAmount > 1)
				return (float) deerAmount;
			else
				return 0;
		} else {
			if (Imgdeer > 1)
				return Imgdeer;
			else
				return 0;
		}
	}

	public float getDWPotential(int img) {
		if (img == 0)
			return DWWood;
		else
			return ImgDwWood;
	}

	public int getElevation() {
		return elevation;
	}

	public int getFarming_plots() {
		return farming_plots;
	}

	// set cell state

	public int getFarmPl() {
		return farming_plots;
	}

	public float getFireWood() {
		return fireWood;
	}

	public// UTM N & E elevation in m
	int getFVPsoil() {
		return FVPsoil;
	}

	public float getFWPotential(int img) {
		if (img == 0)
			return fireWood;
		else
			return ImgScWood;
	}

	public synchronized float getHamlet() {
		return hamper;
	}


	public int getHHnumber() {
		return HHnumber;
	}

	public synchronized int getHHYears() {
		return HH_years;
	}

	public float getLProteinPotential(int img) {
		int Lags, ImgLags;
		Lags = 0;
		ImgLags = 0;
		int hares = (int) animalTracker.getAmount(Hare.class);		
		if (hares >= (Village.HLAG_HUNT + 1))
			Lags += hares;

		if (Imghares >= (Village.HLAG_HUNT + 1))
			ImgLags += Imghares;

		int rabbits = (int) animalTracker.getAmount(Rabbit.class);
		if (rabbits >= (Village.RLAG_HUNT + 1))
			Lags += rabbits;

		if (Imgrabbits >= (Village.RLAG_HUNT + 1))
			ImgLags += Imgrabbits;

		if (img == 0)
			return Lags;
		else
			return ImgLags;
	}

	public int getMaize_pot() {
		return maize_pot;
	}

	public int getMaize_prod() {
		return maize_prod;
	}

	private Color getMappedColor(int col) {
		return ObserverAgentModel.getColorMap().getColor(col);
	}

	public float getMaxWater() {
		return max_water;
	}

	public synchronized int getNumHouses() {
		return HHnumber;
	}

	public synchronized int getOccuAnnum() {
		return occuAnnum;
	}

	public synchronized int getOccupied() {
		return occupied;
	}

	public int getOccuProx() {
		return occuprox;
	}

	public int getOccuProx1() {
		return occuprox1;
	}

	public int getOccuProx2() {
		return occuprox2;
	}

	public int getOccuProx3() {
		return occuprox3;
	}

	public synchronized int getPopulation() {
		return population;
	}

	/** DC: This method instructs Repast about what properties of Cells to display.
	 *  This was implemented in AgentObserverSwarm.setCellProbeMap().
	 * @return The list of properties to display.
	 */
	@Override
	public String[] getProbedProperties() {
		String[] props = new String[] { "UTM_N", "UTM_E", "x", "y", "deer_X",
				"deer_Y", "farming_plots", "maize_pot", "maize_prod", "deer",
				"rabbits", "hares", "water", "elevation", "FVPsoil", "station",
				"degrade", "degradeFactor", "currentHH", "siteprox1",
				"sitetype2", "siteprox2", "sitetype3", "siteprox3", "occupied",
				"occuAnnum", "occuprox", "HHnumber", "fireWood", "shrubgrowth",
		"treegrowth" };

		return props;
	}

	public int getProxTotal() {
		return proxtotal;
	}

	public synchronized HashSet<Agent> getSettlerSet() {
		return settlerSet;
	}

	public synchronized int getShrubs() {
		return shrubgrowth;
	}

	public int getSiteProx1() {
		return siteprox1;
	}

	public int getSiteProx2() {
		return siteprox2;
	}

	public int getSiteProx3() {
		return siteprox3;
	}

	public int getSiteType1() {
		return sitetype1;
	}

	public int getSiteType2() {
		return sitetype2;
	}

	public int getSiteType3() {
		return sitetype3;
	}

	public int getSoilDegrade() {
		return degrade;
	}

	//TODO: What is this doing?!
	public float getSoilProd() {
		return deer_prod;
	}

	public int getSoilType() {
		return FVPsoil; //JAC 9/25/04 soil changed to FVPsoil

	}

	//sends weather station
	public String getStation() {
		return station;
	}

	public synchronized int getTotOccu() {
		return total_occupation;
	}

	public synchronized int getTrees() {
		return treegrowth;
	}

	public float getUTM_E() {
		return UTM_E;
	}

	// get cell state
	public float getUTM_N() {
		return UTM_N;
	}

	public synchronized float getVegPot() {
		return veg_pot;
	}

	public float getWater() {
		return water;
	}

	public int getWaterType() {
		return water_type;
	}

	public int getWorldTime() {
		return worldTime;
	}

	// ZK
	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	/** DC: performs the method on this object */
	public void perform(String method, Class<?> data) {
		try {
			Method m = this.getClass().getMethod(method, data.getClass());					
			m.invoke(this, data);
		} catch (Exception e) {
			System.err.println("Can't invoke method: " + method + "with input: " + data);
		}
	}

	public void perform(String method, float[] data) {
		try {
			Method m = this.getClass().getMethod(method, data.getClass());
			m.invoke(this, data);
		} catch (Exception e) {
			System.err.println("Can't invoke method: " + method + "with input: " + data);
		}
	}

	public void perform(String method, int data) {
		try {
			Method m = this.getClass().getMethod(method, Integer.TYPE);			
			m.invoke(this, data);
		} catch (Exception e) {
			System.err.println("Can't invoke method: " + method + "with input: " + data);
			e.printStackTrace();		
		}
	}

	public void probeSettlers() {
		if (getNumHouses() > 0) {
			synchronized (this) {
				for (Agent a : settlerSet) {
					ProbeUtilities.probe(a);
				}
			}
			ProbeUtilities.updateProbePanels();
		}
	}

	//JAC 12/04 called each year to reset available water in cell
	public void refreshWater() {
		water = max_water;

	}

	public synchronized void removeSettler(Agent agent) {
		settlerSet.remove(agent);
	}

	/** Reports maize productivity for a cell given input UTMs.
	 * This method is called at the end of each year by Database.updateCells.
	 * First, reportGardens determines whether the given UTMs are
	 * in the cell at hand (the if statements), then writes the productivity data
	 * to the file garden_cells.data in the output folder. 
	 * 
	 * @param garden_utm_n Garden plot Northing
	 * @param garden_utm_e Garden plot easting
	 */
	public void reportGardens(int garden_utm_n, int garden_utm_e) {
		if (Village.OUTPUT){
			if ((garden_utm_n<=UTM_N) && (garden_utm_n>(UTM_N-200))) {
				if ((garden_utm_e>=UTM_E) && (garden_utm_e<(UTM_E+200))) {

					try {
						if (worldTime == 601) {
							FileWriter out = new FileWriter(new File("output/garden_cells.data"));
							out
							.write("Year\tGarden_UTM_E\tGarden_UTM_N\tCell_UTM_E\tCell_UTM_N\tMaize_Prod\n");
							out.close();
						}

						FileWriter out = new FileWriter(new File("output/garden_cells.data"), true);
						out.write(worldTime + "\t" + garden_utm_e + "\t" + garden_utm_n + "\t" + UTM_E + "\t" + UTM_N + "\t"
								+ maize_prod + "\n");
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			}		
		}
	}

	//JAC 11/04
	public void resetImgWood() {
		ImgScWood = fireWood; //sets Imaginary Crop

		ImgDwWood = DWWood; //sets Imaginary Crop

	}

	public Agent searchAgentList(int ptag) {
		Agent targetAgent = null;
		ArrayList<Agent> agentList = mySwarm.getAgentList();

		synchronized (agentList) {
			int listsize = agentList.size();
			int i;

			if (listsize > 0) {
				for (i = 0; i < listsize; i++) {
					targetAgent = agentList.get(i);
					if (targetAgent.getTag() == ptag) {
						return targetAgent;
					}
				}
			}
		}

		return null;
	}

	// display
	public void setADisplay(int d) {
		// 0 hides agents
		// 1 displays agents
		// 2 displays communities
		// 3 displays dead agents
		adisplay = d;

	}

	//JAC 9/25/04  sets almagre correction (1)
	public void setAlmagre(float i) {
		almagre = i;
	}

	//JAC 1/05 sets yearly available water
	public void setAvailableWater(float mw) {
		max_water = (mw * 365000);//mw is in m3/per day, max water in liters per year

	}

	//This function calculates the cold correction that affects maize growth using either the almagre or prin correction with the elevation of the cell.
	public void setColdCorrelation() {
		cold_corr = 1;
		if (cold_proxy > 0) {
			if (elevation > 2150)//only used if elevation of cell is greater than 2150 m
			{
				if (cold_proxy == 1)//uses almagre data
				{
					if (almagre < 0)//only used in cold (negative) years
					{
						cold_corr = ((2395 - (double) elevation) / 246)
								* ((3.06434 + almagre) / 3.06434);
					}
				}
				if (cold_proxy == 2)//uses prin data
				{
					if (prin < 0)//only used in cold (negative) years
					{
						cold_corr = ((2395 - (double) elevation) / 246)
								* ((2.98493 + prin) / 2.98493);
					}
				}
				if (elevation > 2395)//if elevation is over 2395 m no corn can grow
				{
					cold_corr = 0;
				}
			}
		}

	}

	public synchronized void setCurrentHH(int val) {
		currentHH = val;
	}

	public void setDeer(float d) {
		animalTracker.setAmount(Deer.class, d);
	}

	public synchronized void setDeer(float i, float eaten) {
		animalTracker.setAmount(Deer.class, i);		

		sdeerfood = sdeerfood * Village.DEER_USE;
		sdeerfood = (eaten * sdeerfood);
		tdeerfood = tdeerfood * Village.DEER_USE;
		tdeerfood = (eaten * tdeerfood);
	}

	//JAC 2/05 production has been split for animals into its three components
	//to better model the interaction of animals on the fuel resources in the model
	public void setDeerProd(float[] array) {
		if (FVPsoil == 0) //JAC 9/25/04 soil changed to FVPsoil
		{
			deer_prod = 0;
		} else {
			deer_prod = array[(FVPsoil - 1)]; //JAC 9/25/04 soil changed to FVPsoil

		}

	}

	public void setDeerWorld(VillageSpace w) { //JAC 3/05
		deerworld = w;
	}

	public void setDegrade(int degrade) {
		this.degrade = degrade;
	}

	public void setDegradeFactor(float degradeFactor) {
		this.degradeFactor = degradeFactor;
	}

	// display
	public void setDisplay(int d) {
		// 0 displays maize potential
		// 1 displays soil
		// 2 displays elevation
		// 3 displays water
		// 4 displays elevation and water
		// 5 displays soil and water
		// 6 displays PI habitations (AD 700-900)
		// 7 displays PII habitations (AD 900-1100)
		// 8 displays PIII habitations (AD 1100-1300)
		// 23 displays deer
		// 24 displays rabbits
		// 25 displays hares
		// 26 displays firewood
		display = d;

	}

	//JAC 11/04
	public void setDWPotential(float DWpotential, int img) {
		if (img == 0) {
			DWWood = DWpotential;
			ImgDwWood = DWWood; //sets Imaginary Crop

		}
		if (img == 1) {
			ImgDwWood = DWpotential; //sets Imaginary Crop only

		}
	}

	public void setElevation(int height) {
		elevation = height;

	}

	public void setFarming_plots(int farming_plots) {
		this.farming_plots = farming_plots;
	}

	// farming info
	public void setFarmPl(int num) {
		farming_plots = num;
	}

	public void setFireWood(float fireWood) {
		this.fireWood = fireWood;
	}

	public void setFuelProduction() throws IOException {

		//rabbit and harefood is in megagrams so they need to be multiplied
		//by 1000, deerfood at this point has not been calculated based on how
		//much deer can use so it is times by Village.DEER_USE,  the sum of these numbers
		//is subtracted from the total vegetation of the cell to determine
		//plant productivity for firewood

		//shrubgrowth = total shrubgrowth minus the amount of shrubs that animals
		//eat multiplied by .75 which accounts for foilage

		shrubgrowth = (int) ((shrubgrowth - (sdeerfood + sharefood + srabbitfood)) * .75);
		treegrowth = (int) ((treegrowth - (tdeerfood + tharefood + trabbitfood)) * .75);

		//This code outputs the vegetation growth and deadwood growth averaged within the
		//different periods
		fuelGrowth += ((shrubgrowth * (1 - Village.SHRUB_DW_PERCENT)) + (treegrowth * (1 - Village.TREE_DW_PERCENT)));
		deadGrowth += ((shrubgrowth * Village.SHRUB_DW_PERCENT) + (treegrowth * Village.TREE_DW_PERCENT));

		//outputs mean growth in flat files for each period
		if (Village.FUEL_TEST != 0) {
			if (worldTime == Village.P6YEAR) {
				//first divide by number of years in period than output data into flat file

				fuelGrowth = fuelGrowth / 125;
				out = new FileWriter(new File("output/FuelGrowthP6.data"), true);
				if (x == world_x_size - 1) {
					out.write(String.format("%f\n", fuelGrowth));
					if (y == world_y_size - 1)
						out.write(String.format("\n"));

				} else
					out.write(String.format("%f\t", fuelGrowth));
				out.close();

				deadGrowth = deadGrowth / 125;
				out = new FileWriter(new File("output/DeadGrowthP6.data"), true);
				if (x == world_x_size - 1) {
					out.write(String.format("%f\n", deadGrowth));
					if (y == world_y_size - 1)
						out.write(String.format("\n"));

				} else
					out.write(String.format("%f\t", deadGrowth));
				out.close();

				fuelGrowth = 0.0f;
				deadGrowth = 0.0f;

			} else if (worldTime == Village.P7YEAR) {
				fuelGrowth = fuelGrowth / 75;
				out = new FileWriter(new File("output/FuelGrowthP7.data"), true);
				if (x == world_x_size - 1) {
					out.write(String.format("%f\n", fuelGrowth));
					if (y == world_y_size - 1)
						out.write(String.format("\n"));

				} else
					out.write(String.format("%f\t", fuelGrowth));
				out.close();

				deadGrowth = deadGrowth / 75;
				out = new FileWriter(new File("output/DeadGrowthP7.data"), true);
				if (x == world_x_size - 1) {
					out.write(String.format("%f\n", deadGrowth));
					if (y == world_y_size - 1)
						out.write(String.format("\n"));

				} else
					out.write(String.format("%f\t", deadGrowth));
				out.close();

				fuelGrowth = 0.0f;
				deadGrowth = 0.0f;

			} else if (worldTime == Village.P8YEAR) {
				fuelGrowth = fuelGrowth / 40;
				out = new FileWriter(new File("output/FuelGrowthP8.data"), true);
				if (x == world_x_size - 1) {
					out.write(String.format("%f\n", fuelGrowth));
					if (y == world_y_size - 1)
						out.write(String.format("\n"));

				} else
					out.write(String.format("%f\t", fuelGrowth));
				out.close();

				deadGrowth = deadGrowth / 40;
				out = new FileWriter(new File("output/DeadGrowthP8.data"), true);
				if (x == world_x_size - 1) {
					out.write(String.format("%f\n", deadGrowth));
					if (y == world_y_size - 1)
						out.write(String.format("\n"));

				} else
					out.write(String.format("%f\t", deadGrowth));
				out.close();

				fuelGrowth = 0.0f;
				deadGrowth = 0.0f;

			} else if (worldTime == Village.P9YEAR) {
				fuelGrowth = fuelGrowth / 40;
				out = new FileWriter(new File("output/FuelGrowthP9.data"), true);
				if (x == world_x_size - 1) {
					out.write(String.format("%f\n", fuelGrowth));
					if (y == world_y_size - 1)
						out.write(String.format("\n"));

				} else
					out.write(String.format("%f\t", fuelGrowth));
				out.close();

				deadGrowth = deadGrowth / 40;
				out = new FileWriter(new File("output/DeadGrowthP9.data"), true);
				if (x == world_x_size - 1) {
					out.write(String.format("%f\n", deadGrowth));
					if (y == world_y_size - 1)
						out.write(String.format("\n"));

				} else
					out.write(String.format("%f\t", deadGrowth));
				out.close();

				fuelGrowth = 0.0f;
				deadGrowth = 0.0f;

			} else if (worldTime == Village.P10YEAR) {
				fuelGrowth = fuelGrowth / 40;
				out = new FileWriter(new File("output/FuelGrowthP10.data"),
						true);
				if (x == world_x_size - 1) {
					out.write(String.format("%f\n", fuelGrowth));
					if (y == world_y_size - 1)
						out.write(String.format("\n"));

				} else
					out.write(String.format("%f\t", fuelGrowth));
				out.close();

				deadGrowth = deadGrowth / 40;
				out = new FileWriter(new File("output/DeadGrowthP10.data"),
						true);
				if (x == world_x_size - 1) {
					out.write(String.format("%f\n", deadGrowth));
					if (y == world_y_size - 1)
						out.write(String.format("\n"));

				} else
					out.write(String.format("%f\t", deadGrowth));
				out.close();

				fuelGrowth = 0.0f;
				deadGrowth = 0.0f;

			} else if (worldTime == Village.P11YEAR) {
				fuelGrowth = fuelGrowth / 60;
				out = new FileWriter(new File("output/FuelGrowthP11.data"),
						true);
				if (x == world_x_size - 1) {
					out.write(String.format("%f\n", fuelGrowth));
					if (y == world_y_size - 1)
						out.write(String.format("\n"));

				} else
					out.write(String.format("%f\t", fuelGrowth));
				out.close();

				deadGrowth = deadGrowth / 60;
				out = new FileWriter(new File("output/DeadGrowthP11.data"),
						true);
				if (x == world_x_size - 1) {
					out.write(String.format("%f\n", deadGrowth));
					if (y == world_y_size - 1)
						out.write(String.format("\n"));

				} else
					out.write(String.format("%f\t", deadGrowth));
				out.close();

				fuelGrowth = 0.0f;
				deadGrowth = 0.0f;

			} else if (worldTime == Village.P12YEAR) {
				fuelGrowth = fuelGrowth / 40;
				out = new FileWriter(new File("output/FuelGrowthP12.data"),
						true);
				if (x == world_x_size - 1) {
					out.write(String.format("%f\n", fuelGrowth));
					if (y == world_y_size - 1)
						out.write(String.format("\n"));

				} else
					out.write(String.format("%f\t", fuelGrowth));
				out.close();

				deadGrowth = deadGrowth / 40;
				out = new FileWriter(new File("output/DeadGrowthP12.data"),
						true);
				if (x == world_x_size - 1) {
					out.write(String.format("%f\n", deadGrowth));
					if (y == world_y_size - 1)
						out.write(String.format("\n"));

				} else
					out.write(String.format("%f\t", deadGrowth));
				out.close();

				fuelGrowth = 0.0f;
				deadGrowth = 0.0f;

			} else if (worldTime == Village.P13YEAR) {
				fuelGrowth = fuelGrowth / 40;
				out = new FileWriter(new File("output/FuelGrowthP13.data"),
						true);
				if (x == world_x_size - 1) {
					out.write(String.format("%f\n", fuelGrowth));
					if (y == world_y_size - 1)
						out.write(String.format("\n"));

				} else
					out.write(String.format("%f\t", fuelGrowth));
				out.close();

				deadGrowth = deadGrowth / 40;
				out = new FileWriter(new File("output/DeadGrowthP13.data"),
						true);
				if (x == world_x_size - 1) {
					out.write(String.format("%f\n", deadGrowth));
					if (y == world_y_size - 1)
						out.write(String.format("\n"));

				} else
					out.write(String.format("%f\t", deadGrowth));
				out.close();

				fuelGrowth = 0.0f;
				deadGrowth = 0.0f;

			} else if (worldTime == Village.P14YEAR) {
				fuelGrowth = fuelGrowth / 40;
				out = new FileWriter(new File("output/FuelGrowthP14.data"),
						true);
				if (x == world_x_size - 1) {
					out.write(String.format("%f\n", fuelGrowth));
					if (y == world_y_size - 1)
						out.write(String.format("\n"));

				} else
					out.write(String.format("%f\t", fuelGrowth));
				out.close();

				deadGrowth = deadGrowth / 40;
				out = new FileWriter(new File("output/DeadGrowthP14.data"),
						true);
				if (x == world_x_size - 1) {
					out.write(String.format("%f\n", deadGrowth));
					if (y == world_y_size - 1)
						out.write(String.format("\n"));

				} else
					out.write(String.format("%f\t", deadGrowth));
				out.close();

				fuelGrowth = 0.0f;
				deadGrowth = 0.0f;

			} else if (worldTime == Village.P15YEAR) {
				fuelGrowth = fuelGrowth / 40;
				out = new FileWriter(new File("output/FuelGrowthP15.data"),
						true);
				if (x == world_x_size - 1) {
					out.write(String.format("%f\n", fuelGrowth));
					if (y == world_y_size - 1)
						out.write(String.format("\n"));

				} else
					out.write(String.format("%f\t", fuelGrowth));
				out.close();

				deadGrowth = deadGrowth / 40;
				out = new FileWriter(new File("output/DeadGrowthP15.data"),
						true);
				if (x == world_x_size - 1) {
					out.write(String.format("%f\n", deadGrowth));
					if (y == world_y_size - 1)
						out.write(String.format("\n"));

				} else
					out.write(String.format("%f\t", deadGrowth));
				out.close();

				fuelGrowth = 0.0f;
				deadGrowth = 0.0f;

			} else if (worldTime == Village.P16YEAR) {
				fuelGrowth = fuelGrowth / 40;
				out = new FileWriter(new File("output/FuelGrowthP16.data"),
						true);
				if (x == world_x_size - 1) {
					out.write(String.format("%f\n", fuelGrowth));
					if (y == world_y_size - 1)
						out.write(String.format("\n"));

				} else
					out.write(String.format("%f\t", fuelGrowth));
				out.close();

				deadGrowth = deadGrowth / 40;
				out = new FileWriter(new File("output/DeadGrowthP16.data"),
						true);
				if (x == world_x_size - 1) {
					out.write(String.format("%f\n", deadGrowth));
					if (y == world_y_size - 1)
						out.write(String.format("\n"));

				} else
					out.write(String.format("%f\t", deadGrowth));
				out.close();

				fuelGrowth = 0.0f;
				deadGrowth = 0.0f;

			} else if (worldTime == Village.P17YEAR) {
				fuelGrowth = fuelGrowth / 45;
				out = new FileWriter(new File("output/FuelGrowthP17.data"),
						true);
				if (x == world_x_size - 1) {
					out.write(String.format("%f\n", fuelGrowth));
					if (y == world_y_size - 1)
						out.write(String.format("\n"));

				} else
					out.write(String.format("%f\t", fuelGrowth));
				out.close();

				deadGrowth = deadGrowth / 45;
				out = new FileWriter(new File("output/DeadGrowthP17.data"),
						true);
				if (x == world_x_size - 1) {
					out.write(String.format("%f\n", deadGrowth));
					if (y == world_y_size - 1)
						out.write(String.format("\n"));

				} else
					out.write(String.format("%f\t", deadGrowth));
				out.close();

				fuelGrowth = 0.0f;
				deadGrowth = 0.0f;

			} else if (worldTime == Village.P18YEAR) {
				fuelGrowth = fuelGrowth / 35;
				out = new FileWriter(new File("output/FuelGrowthP18.data"),
						true);
				if (x == world_x_size - 1) {
					out.write(String.format("%f\n", fuelGrowth));
					if (y == world_y_size - 1)
						out.write(String.format("\n"));

				} else
					out.write(String.format("%f\t", fuelGrowth));
				out.close();

				deadGrowth = deadGrowth / 35;
				out = new FileWriter(new File("output/DeadGrowthP18.data"),
						true);
				if (x == world_x_size - 1) {
					out.write(String.format("%f\n", deadGrowth));
					if (y == world_y_size - 1)
						out.write(String.format("\n"));

				} else
					out.write(String.format("%f\t", deadGrowth));
				out.close();

				fuelGrowth = 0.0f;
				deadGrowth = 0.0f;

			} else if (worldTime == Village.P19YEAR) {
				fuelGrowth = fuelGrowth / 20;
				out = new FileWriter(new File("output/FuelGrowthP19.data"),
						true);
				if (x == world_x_size - 1) {
					out.write(String.format("%f\n", fuelGrowth));
					if (y == world_y_size - 1)
						out.write(String.format("\n"));

				} else
					out.write(String.format("%f\t", fuelGrowth));
				out.close();

				deadGrowth = deadGrowth / 20;
				out = new FileWriter(new File("output/DeadGrowthP19.data"),
						true);
				if (x == world_x_size - 1) {
					out.write(String.format("%f\n", deadGrowth));
					if (y == world_y_size - 1)
						out.write(String.format("\n"));

				} else
					out.write(String.format("%f\t", deadGrowth));
				out.close();

				fuelGrowth = 0.0f;
				deadGrowth = 0.0f;

			}
		}

		//shrub and tree productivity is the product of total vegetation productivity
		//times the percentage of vegetation that is shrubs or trees
		//times .75 each accounts for foliage that does not become
		//wood

		fireWood += ((shrubgrowth * (1 - Village.SHRUB_DW_PERCENT)) + (treegrowth * (1 - Village.TREE_DW_PERCENT)));
		DWWood += ((shrubgrowth * Village.SHRUB_DW_PERCENT) + (treegrowth * Village.TREE_DW_PERCENT));
		if (fireWood > (maxfuel)) {
			DWWood += (fireWood - (maxfuel));
			fireWood = (maxfuel);
		}

		//JAC 3/06  Deadwood decay rate changed from .96 to .8
		DWWood = (DWWood * .8f); //decay rate for deadwood

		//set imaginary resources for movement algorythims
		ImgScWood = fireWood; //JAC 12/01/04 sets Imaginary Crop

		ImgDwWood = DWWood; //JAC 12/01/04 sets Imaginary Crop

		Imgdeer = (float) animalTracker.getAmount(Deer.class);
		Imgrabbits = (int) animalTracker.getAmount(Rabbit.class);
		Imghares = (int) animalTracker.getAmount(Hare.class);

		if (Village.NUKE) {
			if (deer_X == Village.NUKE_X) {
				if (deer_Y == Village.NUKE_Y) {

					n_rabbit += animalTracker.getAmount(Rabbit.class);
					n_hare += animalTracker.getAmount(Hare.class);
					n_firewood += fireWood;
					n_DWWood += DWWood;
					n_tprod += treegrowth;
					n_sprod += shrubgrowth;

					int xtest, ytest;
					xtest = (deer_X * 5) + 4;
					ytest = (deer_Y * 5) + 4;
					if (x == xtest) {
						if (y == ytest) {
							out = new FileWriter(new File("nuked_cell.data"),
									true);
							if (worldTime == 600)
								out
								.write(String
										.format("Year\t Standing Crop\t Dead Wood\t Tree Prod\t Shrub Prod\t Deer\t Rabbit\t Hare\n"));

							out.write(String.format(
									"%d\t %f\t %f\t %d\t %d\t %f\t %d\t %d\n",
									worldTime, n_firewood, n_DWWood, n_tprod,
									n_sprod, animalTracker.getAmount(Deer.class), n_rabbit, n_hare));
							out.close();

							n_rabbit = 0;
							n_hare = 0;
							n_firewood = 0;
							n_DWWood = 0;
							n_tprod = 0;
							n_sprod = 0;
						}
					}
				}
			}
		}
	}

	public void setFVPsoil(int FVPsoil) {
		this.FVPsoil = FVPsoil;
	}

	//JAC 11/04
	public void setFWPotential(float FWpotential, int img) {
		if (img == 0) {
			fireWood = FWpotential;
			ImgScWood = fireWood; //sets Imaginary Crop

		}
		if (img == 1) {
			ImgScWood = FWpotential; //sets Imaginary Crop only

		}
	}

	public void setGrassDeerProd(float[] array) {
		if (FVPsoil == 0) //JAC 9/25/04 soil changed to FVPsoil
		{
			gdeer_prod = 0;
		} else {
			gdeer_prod = array[(FVPsoil - 1)]; //JAC 9/25/04 soil changed to FVPsoil

		}

	}

	public void setGrassHareProd(float[] array) {
		if (FVPsoil == 0) //JAC 9/25/04 soil changed to FVPsoil
		{
			ghare_prod = 0;
		} else {
			ghare_prod = array[(FVPsoil - 1)]; //JAC 9/25/04 soil changed to FVPsoil

		}
	}

	public void setGrassRabbitProd(float[] array) {
		if (FVPsoil == 0) //JAC 9/25/04 soil changed to FVPsoil
		{
			grabbit_prod = 0;
		} else {
			grabbit_prod = array[(FVPsoil - 1)]; //JAC 9/25/04 soil changed to FVPsoil

		}

	}

	public void setHareProd(float[] array) {
		if (FVPsoil == 0) //JAC 9/25/04 soil changed to FVPsoil
		{
			hare_prod = 0;
		} else {
			hare_prod = array[(FVPsoil - 1)]; //JAC 9/25/04 soil changed to FVPsoil

		}
	}

	public synchronized void setHHnumber(int hh) {
		HHnumber = hh;

	}

	//JAC 3/05
	public void setHuntedDeer(int killed_deer) {
		int xx, yy;

		xx = (x / 5);
		yy = (y / 5);


		DeerCell deercell = getDeerCell();

		// zk: leopard fix ***
		if (deercell == null)
			System.out.printf("deercell is null\n null location = [%d,%d]\n",
					xx, yy);
		else
			deercell.setHuntedDeerXY(xx, yy, world, killed_deer);
		animalTracker.setAmountHunted(Deer.class, killed_deer);
	}

	//JAC 10/21/04
	//DANGER this function will set numbers that should not be changed
	//in cells used for model calculations; it
	//should only be used with local graph cells
	public synchronized void setLocalMaize(int maize) {
		maize_prod = maize;
	}

	public synchronized void setMaize_pot(int maize_pot) {
		this.maize_pot = maize_pot;
	}

	public synchronized void setMaize_prod(int maize_prod) {
		this.maize_prod = maize_prod;
	}

	// veg units read are SPUB (lbs beans per acre), we want SPUM
	// where SPUM = (SPUB*10+4)*2.368 (gives yield in kg maize
	// for 1 ha [VW world_y_size - 14:123])
	// How degradation works: If more than 20% (2/PLOTS) of the potential
	// plots in a cell are being farmed, then soil degrades by a factor
	// proportionate to the number of plots being farmed;
	// If exactly 20% are being farmed, it is assumed that local rotation
	// can offset local degradation;
	// If fewer than 20% are being farmed, then soil slowly recuperates.
	public void setMaizePotential(int veg) {

		//reset population percentage counters
		if (x == 0) {
			if (y == 0) {
				hampop = 0;
				bighampop = 0;
				communpop = 0;
				population = 0;
			}
		}

		//reset other yearly variables
		wateruse = 0;
		animalTracker.resetAllHunted();				
		deadAgents = 0;

		if (veg > -1) {
			setColdCorrelation();//JAC 9/25/04 calculates cold correlation

			maize_pot = (int) (((veg * 10) + 4) * 2.36775 * adjust_factor
					* cold_corr * scmr);//JAC 9/25/04 adjusts maize production based on production file and cold correction

		} else {
			System.err.print(String.format(
					"ERROR: (%d,%d) maize potential cannot be negative %d\n",
					x, y, veg));
			System.exit(-1);
		}

		int soilDegrade = Village.SOIL_DEGRADE;

		if (soilDegrade == 0)
			maize_prod = maize_pot;

		// mild degradation (to 70% of potential)
		if (soilDegrade == 1) {
			//degrade the soil if more than 2 plots are farmed
			if (farming_plots > 2) {
				degradeFactor -= 0.05 / ((Village.MAX_PLOTS + 1) - farming_plots);
				if (degradeFactor < 0.7)
					degradeFactor = 0.7f;
				// a fully farmed cell loses 30% of fertility in 6 years (but never gets
				// worse than that)

				maize_prod = (int) (maize_pot * degradeFactor);
			}

			//recover soil is less than 2 plots are farmed
			if (farming_plots < 2) {
				degradeFactor += 0.01; //recovery is 10 times slower than degradation

				if (degradeFactor > 1)
					degradeFactor = 1;

				maize_prod = (int) (maize_pot * degradeFactor);
			}
		}

		// severe degradation (to 40% of potential)
		if (soilDegrade == 2) {
			if (farming_plots > 2) {
				degradeFactor -= 0.1 / ((Village.MAX_PLOTS + 1) - farming_plots);
				if (degradeFactor < 0.4)
					degradeFactor = 0.4f;
				// a fully farmed cell loses 70% of fertility in 6 years (but never gets
				// worse than that)

				maize_prod = (int) (maize_pot * degradeFactor);
			}
			if (farming_plots < 2) {
				degradeFactor += 0.01; //recovery is 10 times slower than degradation

				if (degradeFactor > 1)
					degradeFactor = 1;

				maize_prod = (int) (maize_pot * degradeFactor);
			}
		}

		// The code below sets the total amount of vegetation a cell creates which is used with the percent each animal species eats to determine animal populations
		if (veg > -1) {
			if (veg == 0)
				veg_pot = 0;
			else {
				// veg_pot = (int)"(((veg*10)+4)" gets to actual beans per
				// acre" * 3.1745)-116.4617" reverses beans per acre to natural
				// vegetation * 1.12 converts from lbs/acre to Kilo/hectacre
				// *1.212 correction factor for allows us to put this into
				// normal year productivity currency of the soil survey
				// publication
				veg_pot = (int) (((veg * 10 + 4) * 3.1745 - 116.4617) * 1.12 * 1.212);
			}
		} else {
			System.err
			.print(String
					.format(
							"ERROR: (%d,%d) vegetation potential cannot be negative %d\n",
							x, y, veg));
			System.exit(-1);
		}

		/***************************************************************************

		average productivity per cell per soil

		 ************************************************************************/
		if (timed < 700)    // DC: BUG - Had to deal with ArrayIndexOutOfBoundsException
			prod_by_soil[FVPsoil][timed] += maize_prod;

		if (worldTime == 600)
			numsoil[FVPsoil] = numsoil[FVPsoil] + 1;

		//JAC 2/05 Altering plant growth to separate the three groups

		//first we calculate the number of plots being used for planting
		//and subtract that percent from total
		updateMaxFuel();

		//This code simulates complete catastrophe in a single deer cell.  At time Village.NUKE_T,
		//all production is destroyed. The area is then allowed
		//to recover at base rates.

		if (Village.NUKE) {
			if (deer_X == Village.NUKE_X) {
				if (deer_Y == Village.NUKE_Y) {

					if (worldTime == Village.NUKE_T) {
						veg_pot = 75;
						fireWood = 0;
						DWWood = 0;
					}
				}
			}

		}

		//these divide the productivity into the 3 components
		//times 4 gives us productivity per cell and fwprod_corr is the plot use correction
		shrubgrowth = (int) (((veg_pot * 4) * shrub_prod) * fwprod_corr);
		treegrowth = (int) (((veg_pot * 4) * tree_prod) * fwprod_corr);
		grassgrowth = (int) ((veg_pot * 4 * fwprod_corr) - (shrubgrowth + treegrowth));

		//sets the amount of food that deer eat
		//this combines the three groups of deer food into a single number
		//that is used by the deer cells
		deerfood = ((veg_pot * 4) * gdeer_prod) * fwprod_corr;
		tdeerfood = ((veg_pot * 4) * tdeer_prod) * fwprod_corr;
		sdeerfood = ((veg_pot * 4) * sdeer_prod) * fwprod_corr;
		deerfood += (sdeerfood + tdeerfood);

		//these call the other animal and update them
		updateRabbits();
		updateHares();


	}

	public void setMySwarm(AgentModelSwarm s) {
		mySwarm = s;
	}

	public void setOccuAnnum(int occuAnnum) {
		this.occuAnnum = occuAnnum;
	}

	public void setOccupied(int i) {
		int j, k;

		currentHH = 0;
		//JAC 1/06 deer diff test
		animalTracker.setAmount(Deer.class, Deer.INITIAL_AMOUNT);	
		avgyieldcounter = 0;
		animalTracker.setAmount(Hare.class, 6);
		animalTracker.setAmount(Rabbit.class, 6);				
		animalTracker.resetAllHunted();		
		SCshrub = 0;
		SCtree = 0;
		almagre = 1; //

		prin = 1;
		// System.out.printf("rabbits =%d\n",rabbits);
		occuAnnum = i;
		occupied = i;
		occuprox = i;
		proxtotal = i;
		//JAC 11/04
		hampop = 0;
		bighampop = 0;
		communpop = 0;
		wateruse = 0;
		max_water = 0;

		if (x == 0) {
			if (y == 0) {
				for (j = 0; j < 193; j++) {
					for (k = 0; k < 700; k++) {
						prod_by_soil[j][k] = 0;
					}
					numsoil[j] = 0;
					//   System.out.printf("%f %d \n", prod_by_soil[j][0], numsoil[j]);
				}
			}
		}

		deadAgents = 0;

		total_occupation = 0;
		CC_years = 0;
		BH_years = 0;
		HH_years = 0;

		//JAC 2/7 initialize Fuel variables
		fuelGrowth = 0.0f;
		deadGrowth = 0.0f;
	}


	public void setOccuProx(int occuprox) {
		this.occuprox = occuprox;
	}

	//JAC 9/25/04 sets prin correction (2)
	public void setPrin(float i) {
		prin = i;
	}

	public void setRabbitProd(float[] array) {
		if (FVPsoil == 0) //JAC 9/25/04 soil changed to FVPsoil
		{
			rabbit_prod = 0;
		} else {
			rabbit_prod = array[(FVPsoil - 1)]; //JAC 9/25/04 soil changed to FVPsoil

		}

	}

	public void setScmr(float[] array) {
		if (FVPsoil == 0)
			scmr = 0;
		else
			scmr = array[(FVPsoil - 1)];
	}

	public synchronized void setSCshrubProd(int shrub) {

		SCshrub = (shrub / Village.SHRUB_SC_PERCENT); //standing crop = average productivity/percent average productivity makes up standing crop

		DWshrub = (SCshrub * Village.SHRUB_DW_PERCENT); //shrub deadwood = standing crop times a percentage

		SCshrub = (SCshrub - DWshrub); //deadwood is part of SC so SC is reduced by that amount

		fireWood = SCtree + SCshrub;
		DWWood = DWshrub + DWtree;
		// System.out.printf(" SCtree = %d, SCshrub = %d, firewood = %f ",SCtree, SCshrub, fireWood);

	}

	public synchronized void setSCtreeProd(int tree) {
		SCtree = (tree / Village.TREE_SC_PERCENT); //standing crop = average productivity/percent average productivity makes up standing crop

		DWtree = (SCtree * Village.TREE_DW_PERCENT); //tree deadwood = standing crop times a percentage

		SCtree = (SCtree - DWtree); //deadwood is part of SC so SC is reduced by that amount

	}

	public synchronized void setSettlerSet(HashSet<Agent> a) {
		settlerSet = a;
	}

	public synchronized void setShrubDeerProd(float[] array) {
		if (FVPsoil == 0) // JAC 9/25/04 soil changed to FVPsoil
		{
			sdeer_prod = 0;
		} else {
			sdeer_prod = array[(FVPsoil - 1)]; //JAC 9/25/04 soil changed to FVPsoil

		}

	}

	public synchronized void setShrubHareProd(float[] array) {
		if (FVPsoil == 0) // JAC 9/25/04 soil changed to FVPsoil
		{
			share_prod = 0;
		} else {
			share_prod = array[(FVPsoil - 1)]; //JAC 9/25/04 soil changed to FVPsoil

		}

	}

	public synchronized void setShrubProd(float[] array) {
		if (FVPsoil == 0) // JAC 9/25/04 soil changed to FVPsoil
		{
			shrub_prod = 0;
		} else {
			shrub_prod = array[(FVPsoil - 1)]; //JAC 9/25/04 soil changed to FVPsoil

		}

	}

	public synchronized void setShrubRabbitProd(float[] array) {
		if (FVPsoil == 0) // JAC 9/25/04 soil changed to FVPsoil
		{
			srabbit_prod = 0;
		} else {
			srabbit_prod = array[(FVPsoil - 1)]; //JAC 9/25/04 soil changed to FVPsoil

		}

	}

	public void setSiteProx1(int siteprox1) {
		this.siteprox1 = siteprox1;
	}

	public void setSiteProx2(int siteprox2) {
		this.siteprox2 = siteprox2;
	}

	public void setSiteProx3(int siteprox3) {
		this.siteprox3 = siteprox3;
	}

	public void setSiteType1(int t1) {
		sitetype1 = t1;

	}

	public void setSiteType10(int t10) {
		sitetype10 = t10;

	}

	public void setSiteType11(int t11) {
		sitetype11 = t11;

	}

	public void setSiteType12(int t12) {
		sitetype12 = t12;

	}

	public void setSiteType13(int t13) {
		sitetype13 = t13;

	}

	public void setSiteType14(int t14) {
		sitetype14 = t14;

	}

	public void setSiteType15(int t15) {
		sitetype15 = t15;

	}

	public void setSiteType16(int t16) {
		sitetype16 = t16;

	}

	public void setSiteType17(int t17) {
		sitetype17 = t17;

	}

	public void setSiteType18(int t18) {
		sitetype18 = t18;

	}

	public void setSiteType19(int t19) {
		sitetype19 = t19;

	}

	public void setSiteType2(int t2) {
		sitetype2 = t2;

	}

	public void setSiteType3(int t3) {
		sitetype3 = t3;

	}

	//New site breakdown
	public void setSiteType6(int t6) {
		sitetype6 = t6;

	}

	public void setSiteType7(int t7) {
		sitetype7 = t7;

	}

	public void setSiteType8(int t8) {
		sitetype8 = t8;

	}

	public void setSiteType9(int t9) {
		sitetype9 = t9;

	}

	public void setSoilDegrade(int d) {

		if (Village.SOIL_DEGRADE == 1)
			degrade = d;

		if (Village.SOIL_DEGRADE == 0)
			degrade = 0;

		if (Village.SOIL_DEGRADE == 10)
			degrade = 1;

		degradeFactor = 1; // no matter what (then changed in -setMaizePotential)

	}

	public void setSoilType(int s) {

		FVPsoil = s; //JAC 9/25/04 soil changed to FVPsoil

	}

	// sets spring location information
	public void setSpringType(int w) {
		if (w == 4) {
			setWaterType(w);

			max_water = 0;
		}

	}

	//JAC 9/25/04 sets the weather station that cell is part of
	public void setStation(int s) {
		stnNum = s;
		if (s == 1)
			station = "BL";
		else if (s == 2)
			station = "CO";
		else if (s == 3)
			station = "YJ";
		else
			station = "MV";

	}

	public void setStation(String str) {
		this.station = str;
	}

	public void setTreeDeerProd(float[] array) {
		if (FVPsoil == 0) //JAC 9/25/04 soil changed to FVPsoil
		{
			tdeer_prod = 0;
		} else {
			tdeer_prod = array[(FVPsoil - 1)]; //JAC 9/25/04 soil changed to FVPsoil

		}

	}

	public void setTreeHareProd(float[] array) {
		if (FVPsoil == 0) //JAC 9/25/04 soil changed to FVPsoil
		{
			thare_prod = 0;
		} else {
			thare_prod = array[(FVPsoil - 1)]; //JAC 9/25/04 soil changed to FVPsoil

		}

	}

	public void setTreeProd(float[] array) {
		if (FVPsoil == 0) //JAC 9/25/04 soil changed to FVPsoil
		{
			tree_prod = 0;
		} else {
			tree_prod = array[(FVPsoil - 1)]; //JAC 9/25/04 soil changed to FVPsoil

		}

	}

	public void setTreeRabbitProd(float[] array) {
		if (FVPsoil == 0) //JAC 9/25/04 soil changed to FVPsoil
		{
			trabbit_prod = 0;
		} else {
			trabbit_prod = array[(FVPsoil - 1)]; //JAC 9/25/04 soil changed to FVPsoil

		}

	}

	public void setTrees(int val) {
		this.treegrowth = val;
	}

	public void setUTM_E(float UTM_E) {
		this.UTM_E = UTM_E;
	}

	public void setUTM_N(float UTM_N) {
		this.UTM_N = UTM_N;
	}

	//JAC 1/05 sets amount of water left in cell after used by agent
	public void setWater(float mw) {
		water = mw;
	}

	// JAC 1/05 tells the cell what kind of water is in the cell. 
	// Does not include spring information.  If water can be used from non spring water source the function sets cells available water to a positive number,
	// so agent water collection can find cell
	public synchronized void setWaterType(int w) {
		water_type = w;

		if (water_type >= Village.H2O_TYPE) {
			max_water = 3;           
		}
		else {
			max_water = 0;            
		}

	}

	//JAC 12/04 tells cell how much water has been used from cell
	public void setWaterUse(int waterused) {
		wateruse += waterused;

	}

	// initialization stuff
	public void setWorld(VillageSpace w) { // 1.2
		world = w;
	}

	public synchronized void setWorldTime(int t) {
		worldTime = t;
		timed = worldTime - 600;
	}

	public void setX(int val) {
		this.x = val;
	}

	public void setXY(int inx, int iny) {
		x = inx;
		y = iny;
		//12/04 JAC
		UTM_N = (Village.UTMY - (200 * y));
		UTM_E = (Village.UTMX + (200 * x));
		deer_X = (int) (x / 5);
		deer_Y = (int) (y / 5);
		world.putObject(this, x, y); // add cell to the world
	}

	public void setY(int val) {
		this.y = val;
	}

	public void updateHares() {
		float k, r, g;
		float usedfood;

		harefood = ((veg_pot * 4) * ghare_prod) * fwprod_corr;
		tharefood = ((veg_pot * 4) * thare_prod) * fwprod_corr;
		sharefood = ((veg_pot * 4) * share_prod) * fwprod_corr;
		harefood += (sharefood + tharefood);

		//first the total hare food is divided by 1428.57 since hares only use 70% the productivity available to them and we need productivity in Megagrams
		harefood = harefood / (1000 / Village.HARE_USE);

		// Set up a discrete approximation of the logistic from Gurney and Nisbet 1998 equation 3.65
		k = harefood / Village.HARE_K; //.04453 is the default  megagrams support for one hare for 1 year

		r = Village.HARE_R; //1.75 is the default intrinsic rate of increase

		g = (float) Math.exp(-r);

		int hares = animalTracker.getIntAmount(Hare.class);		
		if (hares != 0)
			hares = (int) ((k * hares) / (hares + g * (k - hares)));
		animalTracker.setAmount(Hare.class, hares);

		//JAC 10/31/05
		//hares = 0;

		//2/05 JAC  Now that we know the number of hares on the landscape this
		//equation calculates the amount of rabbitfood that those rabbits eat

		usedfood = hares * Village.HARE_K;

		if (hares == 0)
			usedfood = 0;
		else
			usedfood = usedfood / harefood;

		sharefood = sharefood * Village.HARE_USE;
		sharefood = (usedfood * sharefood);
		tharefood = tharefood * Village.HARE_USE;
		tharefood = (usedfood * tharefood);

		//	hares = 10;

	}

	// JAC 10/01/04
	// This code modifies the maximum amount of standing crop and fuelwood
	// productivity that each cell can comtain based on the number of planted
	// plots in that cell
	public synchronized void updateMaxFuel() {
		maxfuel = (SCshrub + SCtree) * .1f * (10 - farming_plots);
		fwprod_corr = .1f * (10 - farming_plots);
		if (farming_plots > lastplot) {
			if (fireWood > maxfuel)
				fireWood = maxfuel;

		}
		lastplot = farming_plots;
	}

	public synchronized void updateOccupied() throws Exception {
		int wtime = mySwarm.getWorldTime();

		//This code tracks the number and types of households and
		//total population in the cell
		HHnumber = settlerSet.size();
		if (HHnumber > 8)
			comcenter = 1;
		else
			comcenter = 0;

		//11/12/04 JAC

		if (HHnumber > 0) {
			Iterator<Agent> index;
			int local_familysize = 0;

			index = settlerSet.iterator();
			Agent agentToProbe;

			while (index.hasNext() && (agentToProbe = index.next()) != null) {
				// DC: This entire section was just redundant
				// We already know the agent we're searching for, it's agentToProbe
				/*
				if (agentToProbe == null) {
					local_familysize = 0;
				} else {
					if (settlerSet.size() > 0) {
						Agent myAgent = null;						
						myAgent = searchAgentList(agentToProbe.getTag());

						if (myAgent != null) {
							local_familysize = agentToProbe.getFamilySize(); 
						}
					} else {
						local_familysize = 0;
					}
				} */
				local_familysize = agentToProbe.getFamilySize();

				if (Village.DEBUG)
					System.out.println("updateOccupied - local_familysize = "
							+ local_familysize);

				population += local_familysize;
				if (HHnumber > 8)
					communpop += local_familysize;
				else if (HHnumber > 2)
					bighampop += local_familysize;
				else
					hampop += local_familysize;
			}
		}

		if (x == world_x_size - 1) {
			if (y == world_y_size - 1) {
				//Calculate population totals per settlement type
				hamper = ((float) hampop / (float) population);
				bigper = ((float) bighampop / (float) population);
				commper = ((float) communpop / (float) population);

//				if(Village.OUTPUT){
//					//Calculate average productivity for model
//					int j;
//					float yearly_prod = 0.0f;
//					for (j = 0; j < 193; j++) {
//						yearly_prod += prod_by_soil[j][timed];
//					}
//					yearly_prod = (yearly_prod / (world_x_size*world_y_size));
////					out = new FileWriter(new File("output/productivity.data"));
////
////					out.write(worldTime + "\t " + yearly_prod + "\n");
////					out.close();
//				}
				if (Village.OUTPUT){
					int j;
					//Calculate productivity based on soil type
					for (j = 0; j < 193; j++) {
						if (numsoil[j] > 0)
							prod_by_soil[j][timed] = prod_by_soil[j][timed]
									/ numsoil[j];
					}
					if (worldTime == 605) {
						if (x == world_x_size - 1) {
							if (y == world_y_size - 1) {
								FileWriter out = new FileWriter(new File(
										"output/newavgsoil.data"), true);
								out.write("Soil\t Number\t Mean\n");
								int k;
								int soilout;
								for (j = 0; j < 193; j++) {
									float mean = 0;
									float std = 0;
									for (k = 0; k < (timed + 1); k++) {
										mean += prod_by_soil[j][k];
									}
									mean = mean / timed;
									soilout = j;

									for (k = 0; k < (timed + 1); k++) {
										std = (prod_by_soil[j][k] - mean)
												* (prod_by_soil[j][k] - mean);
									}
									std = std / (timed - 1);
									std = (float) Math.sqrt(std);

									out.write(soilout + "\t " + numsoil[j] + "\t "
											+ mean + "\t " + std + "\n");

								}
								out.close();
							}
						}
					}
				}
			}
		}

		//this code simulates a large square forest fire on the landscape
		//at the worldTime provided
		//the first step is to reset these numbers if a modeling period has past but not until the data has been written out        
		int i;
		for (i = 0; i < 15; i++) {
			if (wtime == (output_time[i] + 1)) {
				total_occupation = 0;
				CC_years = 0;
				BH_years = 0;
				HH_years = 0;

			}
		}

		//every year the cell determines how many households are currently living on it

		if (settlerSet.size() > 0) {
			int yearsHH = 0; //number of households in cell

			yearsHH = settlerSet.size();
			total_occupation += yearsHH;

			if (yearsHH > 8)
				CC_years++;
			else if (yearsHH > 2)
				BH_years++;
			else
				HH_years++;
		}

		if (wtime > 905 && wtime < Village.PIYEAR) {
			if (settlerSet.size() > 0 && getSiteProx1() > 0) {
				occuAnnum++;
				occupied = occupied + settlerSet.size();
				occuprox = occupied / getSiteProx1();
				proxtotal = occuprox;
			}
		}
		if (wtime == Village.PIYEAR) {
			occupied = 0;
			occuAnnum = 0;
			occuprox1 = occuprox;
		}
		if (wtime > Village.PIYEAR && wtime < Village.PIIYEAR) {
			if (settlerSet.size() > 0 && getSiteProx2() > 0) {
				occuAnnum++;
				occupied = occupied + settlerSet.size();
				occuprox = occupied / getSiteProx2();
				proxtotal = occuprox1 + occuprox;
			}
		}
		if (wtime == Village.PIIYEAR) {
			occupied = 0;
			occuAnnum = 0;
			occuprox2 = occuprox;
		}
		if (wtime > Village.PIIYEAR && wtime < Village.PIIIYEAR) {
			if (settlerSet.size() > 0 && getSiteProx3() > 0) {
				occuAnnum++;
				occupied = occupied + settlerSet.size();
				occuprox = occupied / getSiteProx3();
				proxtotal = occuprox1 + occuprox2 + occuprox;
			}
		}
		if (wtime == Village.PIIIYEAR) {
			occuprox3 = occuprox;
			proxtotal = (occuprox1 + occuprox2 + occuprox3);
		}

		//updates households in cells based on MP

		if (wtime < 1300) {
			if (wtime <= Village.P6YEAR)
				currentHH = sitetype6;
			else if (wtime <= Village.P7YEAR)
				currentHH = sitetype7;
			else if (wtime <= Village.P8YEAR)
				currentHH = sitetype8;
			else if (wtime <= Village.P9YEAR)
				currentHH = sitetype9;
			else if (wtime <= Village.P10YEAR)
				currentHH = sitetype10;
			else if (wtime <= Village.P11YEAR)
				currentHH = sitetype11;
			else if (wtime <= Village.P12YEAR)
				currentHH = sitetype12;
			else if (wtime <= Village.P13YEAR)
				currentHH = sitetype13;
			else if (wtime <= Village.P14YEAR)
				currentHH = sitetype14;
			else if (wtime <= Village.P15YEAR)
				currentHH = sitetype15;
			else if (wtime <= Village.P16YEAR)
				currentHH = sitetype16;
			else if (wtime <= Village.P17YEAR)
				currentHH = sitetype17;
			else if (wtime <= Village.P18YEAR)
				currentHH = sitetype18;
			else if (wtime <= Village.P19YEAR)
				currentHH = sitetype19;

		}

		if (Village.DEBUG) {
			System.out.println("updateOccupied DONE");
		}

		//this code tallies and prints out the total number of animals, and plants on the landscape
		if (wtime > resetdata) {
			tot_deer = 0;
			tot_hare = 0;
			tot_rabbits = 0;
			tot_SC = 0;
			tot_DW = 0;
			tot_pop = 0;
			resetdata += 1;
		}

		if ((x%5) != 0 && x > (world_x_size - (world_x_size%5))) {
			tot_deer += (animalTracker.getAmount(Deer.class) / (5 * (world_x_size%5)));
		}
		else {
			tot_deer += (animalTracker.getAmount(Deer.class) / 25);
		}
		tot_hare += animalTracker.getAmount(Hare.class);
		tot_rabbits += animalTracker.getAmount(Rabbit.class);
		tot_SC += fireWood;
		tot_DW += DWWood;
		tot_pop += HHnumber;

		int eligibleMales = mySwarm.getNumMales();
		int eligibleFemales = mySwarm.getNumFemales();

		int allMales = 0;
		int allFemales = 0;
		for(Agent hh : mySwarm.getAgentList()){
			for(Individual ind : hh.getFamilyUnit().getAllIndividuals()){
				if(ind.getGender()==0){
					allFemales++;
				}else{
					allMales++;
				}
			}
		}

		if (Village.PRINT_SYSTEM_STATS){
			if (x == world_x_size - 1) {
				if (y == world_y_size - 1) {

					int run_number = mySwarm.getFile_ID();

					String txt = ".csv";
					String pop = "output/system_stats_run_";
					String combined = pop + run_number + txt;

					if (wtime == 601) {
						FileWriter out = new FileWriter(new File(combined));
						out.write("Year," +
								"Deer," +
								"Hare," +
								"Rabbit," +
								"Standing_Crop," +
								"Deadwood," +
								"EligibleMales," +
								"EligibleFemales," +
								"AllMales," +
								"AllFemales," +
								"Agents\n");
						out.close();
					}

					FileWriter out = new FileWriter(new File(combined), true);
					out.write(worldTime + "," + 
							tot_deer + "," + 
							tot_hare + "," + 
							tot_rabbits + "," + 
							tot_SC + "," + 
							tot_DW + "," + 
							eligibleMales + "," + 
							eligibleFemales + "," + 
							allMales + "," + 
							allFemales + "," + 
							tot_pop + "\n");
					out.close();
				}
			}
		}
	}

	public void updateRabbits() {

		float k, r, g;
		float usedfood;

		// combine the three plant groups into food available
		rabbitfood = ((veg_pot * 4) * grabbit_prod) * fwprod_corr;
		trabbitfood = ((veg_pot * 4) * trabbit_prod) * fwprod_corr;
		srabbitfood = ((veg_pot * 4) * srabbit_prod) * fwprod_corr;
		rabbitfood += (srabbitfood + trabbitfood);

		//first the total rabbit food is divided by 1428.57 since rabbits only use 70% the productivity available to them and we need productivity in Megagrams
		rabbitfood = rabbitfood / (1000 / Village.RABBIT_USE);

		// Set up a discrete approximation of the logistic from Gurney and Nisbet world_y_size - 18 equation 3.65
		k = rabbitfood / Village.RABBIT_K; //.06935 megagrams is the default support for one rabbit for 1 year

		r = Village.RABBIT_R; //2.3 is the default intrinsic rate of increase

		g = (float) Math.exp(-r);

		int rabbits = animalTracker.getIntAmount(Rabbit.class);

		if (rabbits != 0)
			rabbits = (int) ((k * rabbits) / (rabbits + g * (k - rabbits)));
		animalTracker.setAmount(Rabbit.class, rabbits);

		//2/05 JAC  Now that we know the number of rabbits on the landscape this

		//equation calculates the amount of rabbitfood that those rabbits eat for trees and shrubs

		//used food is the percentage of total food consumed
		usedfood = rabbits * Village.RABBIT_K;

		if (rabbits == 0)
			usedfood = 0;
		else
			usedfood = usedfood / rabbitfood;

		//take
		srabbitfood = srabbitfood * Village.RABBIT_USE;
		srabbitfood = (usedfood * srabbitfood);
		trabbitfood = trabbitfood * Village.RABBIT_USE;
		trabbitfood = (usedfood * trabbitfood);

		//rabbits = 10;

	}

	public void lockDeerCell() {
		getDeerCell().getLock().lock();
	}

	public void unlockDeerCell() {
		getDeerCell().getLock().unlock();
	}

	public DeerCell getDeerCell() {
		if (deerCell == null)
			deerCell = (DeerCell) deerworld.getObjectAt(x / 5, y / 5);

		return deerCell;
	}

	public Lock getWoodLock() {		
		return woodLock;
	}

	public Lock getWaterLock() {		
		return waterLock;
	}

	private void createLocks() {
		if (Village.ENABLE_MULTITHREADING) {
			huntLock = new ReentrantLock();
			farmLock = new ReentrantLock();
			waterLock = new ReentrantLock();
			woodLock = new ReentrantLock();
		}
	}
	
	public Lock getHuntLock() {		
		return huntLock;
	}

	public Lock getFarmLock() {		
		return farmLock;
	}

	public int getLineageID() {
		return lineageID;
	}

	public void setLineageID(int lineageID) {
		this.lineageID = lineageID;
	}
}
