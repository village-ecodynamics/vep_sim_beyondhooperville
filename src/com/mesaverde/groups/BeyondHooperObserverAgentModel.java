package com.mesaverde.groups;

//import com.mesaverde.groups.HooperGame;
import com.mesaverde.hunting.*;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.util.Iterator;

import com.mesaverde.village.Cell;
import com.mesaverde.village.EligibleRecord;
import com.mesaverde.village.Logger;
import com.mesaverde.village.Village;
//import com.mesaverde.groups.Constants; //[for warDeadGraph, eventually]
import com.mesaverde.village.Eligible;

import uchicago.src.reflector.RangePropertyDescriptor;
import uchicago.src.sim.analysis.Histogram;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.AbstractGUIController;
import uchicago.src.sim.engine.ActionGroup;
import uchicago.src.sim.engine.Controller;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.DisplayConstants;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.TextDisplay;

/** Run this class to get a simulation with GUI elements.  You can provide a parameter file
 *  similar to that found in v8.pf that will allow you to set the amount of runs, as well as some
 *  other parameters that are adjustable in the Repast panel.  You can then change those parameters
 *  in the panel itself.
 */
public class BeyondHooperObserverAgentModel extends BeyondHooperAgentModelSwarm {
	int display;
	ActionGroup displayActions;
	private int displayFrequency;

	private int zoomFactor; // display resolution

	private static ColorMap colorMap; // tracks colors

	protected boolean Landscape;
	protected boolean Protein;
	protected boolean Exchange;
	protected boolean Agriculture;
	protected boolean Agents;
	protected boolean Resources;
	protected boolean Residential;
	protected boolean Domestication;
	protected boolean Groups;
	protected boolean WarDead;

	protected DisplaySurface displaySurface;
	protected DisplaySurface groupSurface;
	protected Object2DDisplay cellDisplay;
	protected Object2DDisplay agentDisplay;
	protected TextDisplay year;
	protected TextDisplay run;

	protected OpenSequenceGraph lagoGraph;
	protected OpenSequenceGraph proteinStrategiesGraph;
	protected OpenSequenceGraph proteinCostsGraph;
	protected OpenSequenceGraph deerGraph;
	protected OpenSequenceGraph avgGraph;
	protected OpenSequenceGraph peopleGraph;
	protected OpenSequenceGraph bdGraph;
	protected OpenSequenceGraph wdGraph;
	protected OpenSequenceGraph yieldGraph;
	protected OpenSequenceGraph popGraph;
	protected OpenSequenceGraph firewoodGraph;
	protected OpenSequenceGraph fwhappyGraph;
	protected OpenSequenceGraph rrateGraph;
	protected OpenSequenceGraph HourGraph;
	protected OpenSequenceGraph CalGraph;
	protected OpenSequenceGraph totCalGraph;
	protected OpenSequenceGraph HuntingGraph;
	protected OpenSequenceGraph popPercentGraph;
	protected OpenSequenceGraph CommunGraph;
	protected OpenSequenceGraph ex_popGraph;
	protected OpenSequenceGraph maize_exchangeGraph;
	protected OpenSequenceGraph BRN_ex_popGraph;
	protected OpenSequenceGraph BRN_maize_exchangeGraph;
	protected OpenSequenceGraph GRN_pro_popGraph;
	protected OpenSequenceGraph GRN_pro_exchangeGraph;
	protected OpenSequenceGraph BRN_pro_popGraph;
	protected OpenSequenceGraph BRN_pro_exchangeGraph;
	protected OpenSequenceGraph maize_storageGraph;
	protected OpenSequenceGraph protein_storageGraph;
	protected OpenSequenceGraph state_through_timeGraph;
	protected OpenSequenceGraph fam_wrkrsGraph;
	protected Histogram famGraph;
	protected Histogram stateGraph;

	protected OpenSequenceGraph groupGraph;
	protected OpenSequenceGraph groupTypeGraph;
	protected OpenSequenceGraph agentTypesCountGraph;  
	protected OpenSequenceGraph warDeadGraph;
	protected OpenSequenceGraph eligibleGraph;
	public BeyondHooperObserverAgentModel() {
		super();

		// Setting the order of appearance of parameters
		// to NON-alphabetic
		AbstractGUIController.ALPHA_ORDER = false;
		// Suppress output to the RePast console
		AbstractGUIController.CONSOLE_ERR = false;
		AbstractGUIController.CONSOLE_OUT = false;
		this.isGui = true;
	}

	/**
	 * DC: All the graphs from AgentObserverSwarm are going to be set up in
	 * here.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setup() {
		super.setup();

		Controller.ALPHA_ORDER = false;		

		Landscape = true;
		Agents = false;
		Exchange = false;
		Agriculture = false;
		Protein = false;
		Resources = false;
		Residential = false;
		Domestication = false;
		Groups = false;


		if (displaySurface != null)
			displaySurface.dispose();

		if (groupSurface != null)
			groupSurface.dispose();

		disposeGraphs();

		// Fill in the relevant parameters
		displayFrequency = 1; // 1
		zoomFactor = 3; // 3

		if(Village.GUI_VERSION==0){
			Agents = true;
			Exchange = true;
			Agriculture = true;
			Protein = true;
			Resources = true;
			Residential = true;

				Groups=true;
			
			if(Village.DOMESTICATION){
				Domestication = true;
			}
			params = new String[] {
					"File_ID", "allAgents",
					"experimentDuration", "economy", "p_need",
					"p_penalty", "hunting_radius", "harvest_adjustment",
					"need_meat", "state_good", "ad_plots",
					"domestication", "turkey_water", "turkey_maize_per",
					"Landscape", "Agents", "Exchange", "Resources", "Hunting", "Residential", "Agriculture",
					"Domestication", "Groups", 
					"cm_monitor_cost", "max_coop_radius_brn", "group_benefit_growth_rate", "change_rate"
			};
		}
		if(Village.GUI_VERSION==1){
			if(!(Village.AGENT_TYPE==Village.HOOPER_AGENTS)){
				JOptionPane.showMessageDialog(null, "To run the Hooper Model GUI, please set Village.AGENT_TYPE to 'HOOPER_AGENTS'.");
				System.exit(0); // Terminate the simulation.
			}
			Groups = true;
			params = new String[] {
					"File_ID", "allAgents",
					"experimentDuration", "economy",
					"harvest_adjustment",
					"state_good", "ad_plots",
					"cm_monitor_cost", "max_coop_radius_brn", 
					"group_benefit_growth_rate", "change_rate"
			};
		}
		if(Village.GUI_VERSION==2){
			Protein = true;
			Domestication = true;
			params = new String[] {
					"File_ID", "allAgents",
					"experimentDuration", "p_need",
					"p_penalty", "hunting_radius",
					"need_meat", "turkey_water", "turkey_maize_per"
			};
		}

		if(!(Village.GUI_VERSION==1)){
			RangePropertyDescriptor d = new RangePropertyDescriptor("P_need", 0, 25, 5);
			RangePropertyDescriptor e = new RangePropertyDescriptor("Hunting_radius", 0, 40, 5);
			descriptors.put("P_need", d);
			descriptors.put("Hunting_radius", e);
		}

		// DC: set up custom actions, equivalent of message probes in Swarm
		buildCustomActions();
	}

	/**
	 * Dispose of old graphs, if RePast is refreshed.
	 */
	public void disposeGraphs(){
		if (lagoGraph != null)
			lagoGraph.dispose();
		if (deerGraph != null)
			deerGraph.dispose();
		if (avgGraph != null)
			avgGraph.dispose();
		if (peopleGraph != null)
			peopleGraph.dispose();
		if (deerGraph != null)
			deerGraph.dispose();
		if (bdGraph != null)
			bdGraph.dispose();
		if (wdGraph != null)
			wdGraph.dispose();
		if (yieldGraph != null)
			yieldGraph.dispose();
		if (popGraph != null)
			popGraph.dispose();
		if (firewoodGraph != null)
			firewoodGraph.dispose();
		if (fwhappyGraph != null)
			fwhappyGraph.dispose();
		if (rrateGraph != null)
			rrateGraph.dispose();
		if (HourGraph != null)
			HourGraph.dispose();
		if (CalGraph != null)
			CalGraph.dispose();
		if (totCalGraph != null)
			totCalGraph.dispose();
		if (proteinStrategiesGraph != null)
			proteinStrategiesGraph.dispose();
		if (proteinCostsGraph != null)
			proteinCostsGraph.dispose();
		if (HuntingGraph != null)
			HuntingGraph.dispose();
		if (popPercentGraph != null)
			popPercentGraph.dispose();
		if (CommunGraph != null)
			CommunGraph.dispose();
		if (ex_popGraph != null)
			ex_popGraph.dispose();
		if (maize_exchangeGraph != null)
			maize_exchangeGraph.dispose();
		if (BRN_ex_popGraph != null)
			BRN_ex_popGraph.dispose();
		if (BRN_maize_exchangeGraph != null)
			BRN_maize_exchangeGraph.dispose();
		if (GRN_pro_popGraph != null)
			GRN_pro_popGraph.dispose();
		if (GRN_pro_exchangeGraph != null)
			GRN_pro_exchangeGraph.dispose();
		if (BRN_pro_popGraph != null)
			BRN_pro_popGraph.dispose();
		if (BRN_pro_exchangeGraph != null)
			BRN_pro_exchangeGraph.dispose();
		if (maize_storageGraph != null)
			maize_storageGraph.dispose();
		if (protein_storageGraph != null)
			protein_storageGraph.dispose();
		if (state_through_timeGraph != null)
			state_through_timeGraph.dispose();
		if (fam_wrkrsGraph != null)
			fam_wrkrsGraph.dispose();
		if (famGraph != null)
			famGraph.dispose();
		if (stateGraph != null)
			stateGraph.dispose();
		if (warDeadGraph != null)
			warDeadGraph.dispose();
		if (eligibleGraph != null)
			eligibleGraph.dispose();
	}

	/**
	 * DC: Create Message probes for the interface. This allows customizing what
	 * visual elements to view.
	 */
	public void buildCustomActions() {		
		modelManipulator.init();

		// DC: Need to restructure the ModelManipulator and its layout
		JPanel pan = modelManipulator.getPanel();
		JPanel newPanel = new JPanel();
		newPanel.setLayout(new GridLayout(36, 1));
		JScrollPane scrollPane = new JScrollPane(newPanel);
		scrollPane
		.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane
		.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		if(Village.GUI_VERSION==0 || Village.GUI_VERSION==2){
			newPanel.add(createButton("seeMaizePotential", "seeMaizePotential"));
			newPanel.add(createButton("seeDeer", "seeDeer"));
			newPanel.add(createButton("seeRabbits", "seeRabbits"));
			newPanel.add(createButton("seeHares", "seeHares"));
		}
		if(Village.GUI_VERSION==0 || Village.GUI_VERSION==1){

		}
		if(Village.GUI_VERSION==0){
			newPanel.add(createButton("seeElevation", "seeElevation"));
			newPanel.add(createButton("seeSoil", "seeSoil"));
			newPanel.add(createButton("seeSoilDegrade", "seeSoilDegrade"));
			newPanel.add(createButton("seeWater", "seeWater"));
			newPanel.add(createButton("seeMaxWater", "seeMaxWater"));
			newPanel.add(createButton("seeMaizeWater", "seeMaizeWater"));
			newPanel.add(createButton("seeElevationWater", "seeElevationWater"));
			newPanel.add(createButton("seeSoilWater", "seeSoilWater"));
			newPanel.add(createButton("seeMaizePI", "seeMaizePI"));
			newPanel.add(createButton("seeMaizePII", "seeMaizePII"));
			newPanel.add(createButton("seeMaizePIII", "seeMaizePIII"));
			newPanel.add(createButton("seeWaterPI", "seeWaterPI"));
			newPanel.add(createButton("seeWaterPII", "seeWaterPII"));
			newPanel.add(createButton("seeWaterPIII", "seeWaterPIII"));
			newPanel.add(createButton("seeElevationPI", "seeElevationPI"));
			newPanel.add(createButton("seeElevationPII", "seeElevationPII"));
			newPanel.add(createButton("seeElevationPIII", "seeElevationPIII"));
			newPanel.add(createButton("seeOccupyHH", "seeOccupyHH"));
			newPanel.add(createButton("seeOccupyYears", "seeOccupyYears"));
			newPanel.add(createButton("seeProximityPI", "seeProximityPI"));
			newPanel.add(createButton("seeProximityPII", "seeProximityPII"));
			newPanel.add(createButton("seeProximityPIII", "seeProximityPIII"));
			newPanel.add(createButton("seeShrubs", "seeShrubs"));
			newPanel.add(createButton("seeTrees", "seeTrees"));
			newPanel.add(createButton("seeFirewood", "seeFirewood"));
			newPanel.add(createButton("seeweatherStation", "seeweatherStation"));
			newPanel.add(createButton("hideAgents", "hideAgents"));
			newPanel.add(createButton("seeAgents", "seeAgents"));
			newPanel.add(createButton("seeDeadAgents", "seeDeadAgents"));
			newPanel.add(createButton("seeCommunities", "seeCommunities"));
		}

		pan.remove(0);
		pan.remove(1);
		pan.add(scrollPane);
		modelManipulator.setEnabled(true);
	}

	// Now it's time to build the model objects. We use various parameters
	// inside ourselves to choose how to create things.
	public void buildModel() {
		super.buildModel();
		buildGraphs();
		if(Landscape){
			buildDisplay();
			if(Village.GUI_VERSION==2){
				seeAgentsDomestication();
			}else{
				seeAgents();	
			}
		}
		displayGraphs();
	}

	/**
	 * DC: creates the graphs to be displayed. Made the decision to force two
	 * options. I've ignored setGraphics, such that all updates will be
	 * displayed when the graph is updated. Secondly, I've setFileOutput, such
	 * that the graphs will output to file when its writeToFile method is
	 * called.
	 */
	protected void buildGraphs() {

		if(Constants.MERGE_AND_FIGHT) {
			//Created by Stefani Crabtree 11/16/14
			// graph war deaths through time
			warDeadGraph = new OpenSequenceGraph("Total Annual War Deaths", this);
			warDeadGraph.setAxisTitles("Time", "Deaths from Conflict");
			warDeadGraph.createSequence("War Dead", Color.red, this, "getWarDead");
			
			// * JAC 11/04 Create the graph widget to display births / deaths
						wdGraph = new OpenSequenceGraph(
								"Total Number of War Deaths vs Regular Deaths Per Year", this);
						wdGraph.setAxisTitles("Time", "#");

						// graph number of warDead
					
						wdGraph.createSequence("War Dead", Color.red, this, "getWarDead");

						// graph number in deaths
						createTotalSequenceFor(wdGraph, "Deaths", getAgentList(),
								"getNumDeaths", 0);
		} 

		if(Domestication){
			// Protein Acquisition Strategies graph
			proteinStrategiesGraph = new OpenSequenceGraph("Average Proportion of Protein Acquired by Various Strategies", this);
			proteinStrategiesGraph.setAxisTitles("Time", "Proportion of Protein");
			proteinStrategiesGraph.setYRange(-0.1,1.1);

			createAverageSequenceFor(proteinStrategiesGraph, "Hunting", getAgentList(),
					"getHunting_protein_proportion", 0);

			createAverageSequenceFor(proteinStrategiesGraph, "Domestication", getAgentList(),
					"getDomestication_protein_proportion", 0);

			createAverageSequenceFor(proteinStrategiesGraph, "Trading - GRN", getAgentList(),
					"getTrading_protein_GRN_proportion", 0);

			createAverageSequenceFor(proteinStrategiesGraph, "Trading - BRN", getAgentList(),
					"getTrading_protein_BRN_proportion", 0);

			// Protein Acquisition Strategies graph
			proteinCostsGraph = new OpenSequenceGraph("Average Proportion of Costs Acquiring Protein", this);
			proteinCostsGraph.setAxisTitles("Time", "Proportion of Protein Costs");
			proteinCostsGraph.setYRange(-0.1,1.1);

			createAverageSequenceFor(proteinCostsGraph, "Hunting", getAgentList(),
					"getHunting_protein_costs_proportion", 0);

			createAverageSequenceFor(proteinCostsGraph, "Domestication", getAgentList(),
					"getDomestication_protein_costs_proportion", 0);
		}

		//		if (Groups) {
		//			// displays group stats
		//			buildGroupTypeGraph();
		//			agentTypesCountGraph = new OpenSequenceGraph("Agent type counts", this);
		//			agentTypesCountGraph.setAxisTitles("Time", "Number");
		//
		//			groupGraph = new OpenSequenceGraph("Group statistics", this);
		//			groupGraph.setAxisTitles("Time", "Number");
		//			groupGraph.addSequence("# groups", new Sequence() {
		//				@Override
		//				public double getSValue() {
		//					double res = 0;
		//
		//					if (getGroupStats() != null)
		//						res = getGroupStats().numGroups;
		//
		//					return res;
		//				}
		//			});
		//
		//			groupGraph.addSequence("# groups - H", new Sequence() {
		//				@Override
		//				public double getSValue() {
		//					double res = 0;
		//
		//					if (getGroupStats() != null)
		//						res = getGroupStats().numHierarchicalGroups;
		//
		//					return res;
		//				}
		//			});
		//
		//			groupGraph.addSequence("# groups - NH/HnoL", new Sequence() {
		//				@Override
		//				public double getSValue() {
		//					double res = 0;
		//
		//					if (getGroupStats() != null)
		//						res = getGroupStats().numNonHierarchicalGroups;
		//
		//					return res;
		//				}
		//			});
		//
		//			groupGraph.addSequence("avg group size", new Sequence() {
		//				@Override
		//				public double getSValue() {
		//					double res = 0;
		//
		//					if (getGroupStats() != null)
		//						res = getGroupStats().averageGroupSize;
		//
		//					return res;
		//				}
		//			});
		//
		//
		//			groupGraph.addSequence("largest group (x10)", new Sequence() {
		//				@Override
		//				public double getSValue() {
		//					double res = 0;
		//
		//					if (getGroupStats() != null)
		//						res = getGroupStats().largestGroupSize / 10.0;
		//
		//					return res;
		//				}
		//			});
		//
		//			agentTypesCountGraph.addSequence("# of H agents (x10)", new Sequence() {
		//				@Override
		//				public double getSValue() {
		//					double res = 0;
		//
		//					if (getGroupStats() != null)
		//						res = getGroupStats().numH / 10.0;
		//
		//					return res;
		//				}
		//			});
		//
		//			agentTypesCountGraph.addSequence("# of NH agents (x10)", new Sequence() {
		//				@Override
		//				public double getSValue() {
		//					double res = 0;
		//
		//					if (getGroupStats() != null)
		//						res = getGroupStats().numNH / 10.0;
		//
		//					return res;
		//				}
		//			});
		//
		//			/*
		//            agentTypesCountGraph.addSequence("# of HnoL agents (x10)", new Sequence() {
		//                @Override
		//                public double getSValue() {
		//                    double res = 0;
		//
		//                    if (groupStats != null)
		//                        res = groupStats.numHnoL / 10.0;
		//
		//                    return res;
		//                }
		//            });*/
		//
		//			agentTypesCountGraph.addSequence("avg # agents per H group", new Sequence() {
		//				@Override
		//				public double getSValue() {
		//					double res = 0;
		//
		//					if (getGroupStats() != null)
		//						res = getGroupStats().avgHSize;
		//
		//					return res;
		//				}
		//			});
		//
		//			agentTypesCountGraph.addSequence("avg # agents per NH group", new Sequence() {
		//				@Override
		//				public double getSValue() {
		//					double res = 0;
		//
		//					if (getGroupStats() != null)
		//						res = getGroupStats().avgNHSize;
		//
		//					return res;
		//				}
		//			});
		//		}

		if(Agents){
			// * Create the graph widget to display household numbers 
			popGraph = new OpenSequenceGraph(
					"Number Households Simulated/Estimated vs. time", this);
			popGraph.setAxisTitles("Time", "Households");
			popGraph.createSequence("Pop_simulated", Color.red, this,
					"getNumAgents");

			// * Create the graph widget to display family Size
			famGraph = new Histogram("Family Size", 10, 0, 10.0);
			famGraph.setAxisTitles("Size", "#");
			famGraph.createHistogramItem("Family Size", getAgentList(),
					"getFamilySize");

			// * Create the graph widget to display family Size
			stateGraph = new Histogram("Agent State", 3, 0, 3.0);
			stateGraph.setAxisTitles("State", "#");
			stateGraph.createHistogramItem("Agent State", getAgentList(),
					"getState");

			// * JAC 11/04 Create the graph widget to display total individual
			// population
			peopleGraph = new OpenSequenceGraph("Total Individual Population",
					this);
			peopleGraph.setAxisTitles("Time", "#");

			// graph avg number of plots
			createMaxSequenceFor(peopleGraph, "Population", getCellList(),
					"getPopulation", 0);

			// * JAC 11/04 Create the graph widget to display births / deaths
			bdGraph = new OpenSequenceGraph(
					"Total Number of Births/Deaths Per Year", this);
			bdGraph.setAxisTitles("Time", "#");

			// graph number of births
			createTotalSequenceFor(bdGraph, "Births", getAgentList(),
					"getNumBirths", 0);

			// graph number in deaths
			createTotalSequenceFor(bdGraph, "Deaths", getAgentList(),
					"getNumDeaths", 0);

			ArrayList<Integer> eligibleGenders = new ArrayList<Integer>();
			for (EligibleRecord person : Eligible.getEligibleList()){
				eligibleGenders.add(person.getGender());
			}
//			System.out.println("Eligible List Graph being printed" + eligibleGenders);



			eligibleGraph = new OpenSequenceGraph (
					"Number of Eligible Males and Females through time", this);
			eligibleGraph.setAxisTitles("Time", "Population Numbers");
			eligibleGraph.createSequence("MALE", Color.red, this, "getNumMales");
			eligibleGraph.createSequence("FEMALE", Color.blue, this, "getNumFemales");


			// * Create the graph widget to display household numbers
			popGraph = new OpenSequenceGraph(
					"Number Households Simulated/Estimated vs. Time", this);
			popGraph.setAxisTitles("Time", "Households");
			popGraph.setLocation(100, 100);

			// graph num of agents simulated
			popGraph.createSequence("Simulated", 0, this, "getNumAgents");

			// graph num of agents estimated
			popGraph.createSequence("Method 1", 0, this, "getPopEstAgentsMethod1");

			// Updated: JAC 12/04
			popGraph.createSequence("Method 2", 0, this, "getPopEstAgentsMethod2");

			// graph Minimum num of agents estimated
			popGraph.createSequence("Method 3", 0, this, "getPopEstAgentsMethod3");

			// * Create the graph widget to display mean household state through
			// time
			state_through_timeGraph = new OpenSequenceGraph(
					"Mean Household State", this);
			state_through_timeGraph.setAxisTitles("Year", "state");
			state_through_timeGraph.setYRange(0, 2);

			// graph mean current household state
			createAverageSequenceFor(state_through_timeGraph, "State",
					getAgentList(), "getState", 0);

			// * Create the graph widget to display mean household numbers through
			// time
			fam_wrkrsGraph = new OpenSequenceGraph("Family Sizes", this);
			fam_wrkrsGraph.setAxisTitles("Year", "N");

			// graph mean current household size
			createAverageSequenceFor(fam_wrkrsGraph, "Total", getAgentList(),
					"getFamilySize", 0);

			// graph mean current N workers per family (household members > 7
			// yrs old)
			createAverageSequenceFor(fam_wrkrsGraph, "Workers", getAgentList(),
					"getWorkerSize", 0);
		}

		if(Residential){
			// * community center stats
			popPercentGraph = new OpenSequenceGraph(
					"Proportion of Population in Each Settlement Type", this);
			popPercentGraph.setAxisTitles("Time", "#");
			popPercentGraph.setYRange(0,1);
			popPercentGraph.setYViewPolicy(OpenSequenceGraph.SHOW_FIRST);

			createMaxSequenceFor(popPercentGraph, "Community Centers", getCellList(), 
					"getCommunityPercent", 0);

			createMaxSequenceFor(popPercentGraph, "Big Hamlets", getCellList(),
					"getBigHamlet", 0);

			createMaxSequenceFor(popPercentGraph, "Hamlets", getCellList(),
					"getHamlet", 0);

			// * JAC 11/04 community center stats
			CommunGraph = new OpenSequenceGraph("Community Center Statistics",
					this);
			CommunGraph.setAxisTitles("Time", "#");

			// graph avg number of trees
			createTotalSequenceFor(CommunGraph, "Number of CC", getCellList(),
					"getCommunity", 0);

			// graph avg number of shrubs
			createMaxSequenceFor(CommunGraph, "# Households of Largest CC",
					getCellList(), "getNumHouses", 0);

		}

		if(Protein){
			// * Create the graph widget to show number of deer on landscape
			deerGraph = new OpenSequenceGraph(
					"Number of Deer on Landscape", this);
			deerGraph.setAxisTitles("Time", "Individuals/Year");

			// graph total number of deer
			createTotalSequenceFor(deerGraph, "Deer", getDeerCellList(),
					"getDeer", 0);

			// * Create the graph widget to display total number of rabbits
			// and hares on landscape
			lagoGraph = new OpenSequenceGraph(
					"Number of Rabbits and Hares on Landscape", this);
			lagoGraph.setAxisTitles("Time", "Individuals/Year");

			// graph num of rabbits simulated
			lagoGraph.addSequence("Rabbits", new Sequence() {                
				public double getSValue() {
					double total = 0;

					for (Cell c : getCellList()) {
						total += c.getAnimalTracker().getAmount(Rabbit.class);
					}                        

					return total;
				}
			});			

			// graph number of hares simulated
			lagoGraph.addSequence("Hares", new Sequence() {
				public double getSValue() {
					double total = 0;

					for (Cell c : getCellList()) {
						total += c.getAnimalTracker().getAmount(Hare.class);
					}

					return total;
				}
			});

			// * Hunting return rate graph
			rrateGraph = new OpenSequenceGraph("Hunting Return Rates", this);
			rrateGraph.setAxisTitles("Time", "grams of protein / Cal expended");
			rrateGraph.setYRange(-0.1,10);
			rrateGraph.setYAutoExpand(false);

			// graph avg return rate for deer
			createNonNullAverageSequenceFor(rrateGraph, "Deer", getAgentList(),
					"getDeerRR", 0);

			// graph avg return rate for hare
			createNonNullAverageSequenceFor(rrateGraph, "Hare", getAgentList(),
					"getHareRR", 0);

			// graph avg return rate for rabbit
			createNonNullAverageSequenceFor(rrateGraph, "Rabbit", getAgentList(),
					"getRabbitRR", 0);

			// graph avg return rate for all hunting
			createNonNullAverageSequenceFor(rrateGraph, "All Hunting", getAgentList(),
					"getHunting_return_rate", 0);


			// * JAC 3/05 Hunting graph
			HuntingGraph = new OpenSequenceGraph("Total Animals Hunted", this);
			HuntingGraph.setAxisTitles("Time", "Individuals/Year");

			// graph avg number of hours worked
			HuntingGraph.addSequence("Deer", new Sequence() {
				public double getSValue() {
					double total = 0;

					for (Cell c : getCellList()) {
						total += c.getAnimalTracker().getAmountHunted(Deer.class);
					}

					return total;
				}
			});			

			// graph max number of hours worked
			HuntingGraph.addSequence("Rabbits", new Sequence() {
				public double getSValue() {
					double total = 0;

					for (Cell c : getCellList()) {
						total += c.getAnimalTracker().getAmountHunted(Rabbit.class);
					}

					return total;
				}
			});

			// graph max number of hours worked
			HuntingGraph.addSequence("Hares", new Sequence() {
				public double getSValue() {
					double total = 0;

					for (Cell c : getCellList()) {
						total += c.getAnimalTracker().getAmountHunted(Hare.class);
					}

					return total;
				}
			});

			// * Create the graph widget to display mean & maximum protein storage
			// per household
			protein_storageGraph = new OpenSequenceGraph(
					"Mean Household Current & Maximum Protein Storage", this);
			protein_storageGraph.setAxisTitles("Year", "g");

			// graph mean current protein storage
			createAverageSequenceFor(protein_storageGraph, "Current",
					getAgentList(), "getProteinStorage", 0);

			// graph mean maximum protein storage
			createMaxSequenceFor(protein_storageGraph, "Maximum",
					getAgentList(), "getProteinMaxStore", 0);
		}

		if(Resources){
			// * Create the graph widget to display average number trees and
			// shrubs per cell
			firewoodGraph = new OpenSequenceGraph(
					"Avg Number of Trees and Shrubs Per Cell", this);
			firewoodGraph.setAxisTitles("time", "#");

			// graph avg number of trees
			createAverageSequenceFor(firewoodGraph, "Trees", getCellList(),
					"getTrees", 0);

			// graph avg number of shrubs
			createAverageSequenceFor(firewoodGraph, "Shrubs",
					getCellList(), "getShrubs", 0);

			// * Create the graph widget to display percentage of agents
			// traveling over 5 and 10 km
			fwhappyGraph = new OpenSequenceGraph(
					"% Households Traveling > 5 and 10 Km for Fuels", this);
			fwhappyGraph.setAxisTitles("Time", "%");

			// graph percentage traveling over 5 km
			createAverageSequenceFor(fwhappyGraph, "Over 5 Km",
					getAgentList(), "getFWhappy", 0);

			// graph percentage traveling over 10 km
			createAverageSequenceFor(fwhappyGraph, "Over 10 Km",
					getAgentList(), "getFWout", 0);

			// * JAC 1/05 Hours agent workers
			HourGraph = new OpenSequenceGraph(
					"Average/Maximum Hours Worked Per Worker", this);
			HourGraph.setAxisTitles("Time", "# of Hours/Worker");

			// graph avg number of hours worked
			createAverageSequenceFor(HourGraph, "Average Hours/Worker",
					getAgentList(), "getHrs", 0);

			// graph max number of hours worked
			createMaxSequenceFor(HourGraph, "Max hours By a Worker",
					getAgentList(), "getHrs", 0);

			// * JAC 3/05 Calorie graph
			CalGraph = new OpenSequenceGraph("Average Calories Per Activity",
					this);
			CalGraph.setAxisTitles("Time", "# of Calories");

			// graph avg number of calories used to farm
			createAverageSequenceFor(CalGraph, "Agriculture", getAgentList(),
					"getFarmCal", 0);

			// graph avg number of calories used to gather fuel
			createAverageSequenceFor(CalGraph, "Fuel", getAgentList(),
					"getFuelCal", 0);

			// graph avg number of calories used to hunt
			createAverageSequenceFor(CalGraph, "Hunting", getAgentList(),
					"getHuntCal", 0);

			// graph avg number of calories used to gather water
			createAverageSequenceFor(CalGraph, "Water", getAgentList(),
					"getWaterCal", 0);



			// * JAC 5/05 Total Calorie graph
			totCalGraph = new OpenSequenceGraph(
					"Average Calories Gathered vs Spent", this);
			totCalGraph.setAxisTitles("Time", "# of Calories");

			// graph avg number of calories consumed as food
			createAverageSequenceFor(totCalGraph, "Gathered", getAgentList(),
					"getGatheredCal", 0);

			// graph avg number of calories used to farm
			createAverageSequenceFor(totCalGraph, "Spent", getAgentList(),
					"getSpentCal", 0);

		}

		if(Agriculture){
			// * Create the graph widget to display mean & maximum maize storage
			// per household
			maize_storageGraph = new OpenSequenceGraph(
					"Mean Household Current & Maximum Maize Storage", this);
			maize_storageGraph.setAxisTitles("Year", "Kg");

			// graph mean current maize storage
			createAverageSequenceFor(maize_storageGraph, "Current",
					getAgentList(), "getMaizeStorage", 0);

			// graph mean maximum maize storage
			createMaxSequenceFor(maize_storageGraph, "Maximum", getAgentList(),
					"getMaxStore", 0);




			// * Create the graph widget to display num plots
			avgGraph = new OpenSequenceGraph(
					"Mean # of Home and Away Plots Per Family", this);
			avgGraph.setAxisTitles("Time", "#");

			// graph avg number of plots
			createAverageSequenceFor(avgGraph, "A-plots", getAgentList(),
					"getAplots", 0);

			// graph avg number in household
			createAverageSequenceFor(avgGraph, "H-plots", getAgentList(),
					"getHplots", 0);


			// * Create the graph widget to display yield
			yieldGraph = new OpenSequenceGraph(
					"Yields Landscape/Farmed vs. Time", this);
			yieldGraph.setAxisTitles("Time", "Yield kg/ha");

			// graph average yield realized by agents
			createAverageSequenceFor(yieldGraph, "Average Agent Yield",
					getAgentList(), "getYield", 0);

			// graph avg yield over all cells
			createAverageSequenceFor(yieldGraph, "Average Landscape Yield", getCellList(),
					"getMaize_prod", 0);

		}

		if (Exchange && this.economy != 0)
		{
			// * Create the graph widget to display agents involved in
			// exchange
			ex_popGraph = new OpenSequenceGraph(
					"Agents Requesting Through GRN", this);
			ex_popGraph.setAxisTitles("Time", "Number of Agents");

			// graph total number of agents who request and survive the year
			createTotalSequenceFor(ex_popGraph, "Donating", getAgentList(),
					"getDonater", 0);

			// graph total number of agents requesting maize
			createTotalSequenceFor(ex_popGraph, "Requesting",
					getAgentList(), "getNumExchange", 0);

			// graph total number of agents who request and survive the year
			createTotalSequenceFor(ex_popGraph, "Successful",
					getAgentList(), "getSucExchange", 0);



			// * Create the graph widget to display amount of maize requested
			// + exchanged
			maize_exchangeGraph = new OpenSequenceGraph(
					"Maize Exchanged Through GRN", this);
			maize_exchangeGraph.setAxisTitles("Time", "Kg/Year)");

			// graph amount of maize requested by agents
			createTotalSequenceFor(maize_exchangeGraph, "Kg Requested",
					getAgentList(), "getMRequest", 0);

			// graph amount of maize exchanged by agents
			createTotalSequenceFor(maize_exchangeGraph, "Kg Received",
					getAgentList(), "getMExchange", 0);

			// graph amount of maize exchanged to agents that die anyway
			createTotalSequenceFor(maize_exchangeGraph, "Kg Wasted",
					getAgentList(), "getMWasted", 0);

			// graph amount of maize exchanged to agents that die anyway
			createTotalSequenceFor(maize_exchangeGraph, "Kg Donated",
					getAgentList(), "getMGiven", 0);



			// * Create the graph widget to display agents involved in
			// exchange
			BRN_ex_popGraph = new OpenSequenceGraph(
					"Agents Requesting Through BRN", this);
			BRN_ex_popGraph.setAxisTitles("Time", "Number of Agents");

			// graph total number of agents requesting maize
			createTotalSequenceFor(BRN_ex_popGraph, "Requesting",
					getAgentList(), "getBRNNumExchange", 0);

			// graph total number of agents who request and survive the year
			createTotalSequenceFor(BRN_ex_popGraph, "Receiving",
					getAgentList(), "getBRNSucExchange", 0);

			// graph total number of paybacks to agents
			createTotalSequenceFor(BRN_ex_popGraph, "Paybacks",
					getAgentList(), "getBRNMPayback", 0);



			// * Create the graph widget to display amount of maize requested
			// + exchanged in BRN
			BRN_maize_exchangeGraph = new OpenSequenceGraph(
					"Maize Exchanged Through BRN", this);
			BRN_maize_exchangeGraph.setAxisTitles("Time", "Kg/Year");

			// graph amount of maize requested by agents
			createTotalSequenceFor(BRN_maize_exchangeGraph, "Kg Requested",
					getAgentList(), "getBRNRequest", 0);

			// graph amount of maize exchanged by agents
			createTotalSequenceFor(BRN_maize_exchangeGraph, "Kg Received",
					getAgentList(), "getBRNExchange", 0);

			// graph amount of maize exchanged to agents that die anyway
			createTotalSequenceFor(BRN_maize_exchangeGraph, "Kg Wasted",
					getAgentList(), "getBRNWasted", 0);

			// graph amount of maize paid back to agents
			createTotalSequenceFor(BRN_maize_exchangeGraph, "Kg Paid Back",
					getAgentList(), "getBRNMPaidBack", 0);



			// * Create the graph widget to display agents involved in
			// exchange
			GRN_pro_popGraph = new OpenSequenceGraph(
					"Agents Requesting Protein Through GRN", this);
			GRN_pro_popGraph.setAxisTitles("Time", "Number of Agents");

			// graph total number of agents requesting protein
			createTotalSequenceFor(GRN_pro_popGraph, "Requesting",
					getAgentList(), "getGRN_PNumExchange", 0);

			// graph total number of agents who request and survive the year
			createTotalSequenceFor(GRN_pro_popGraph, "Receiving",
					getAgentList(), "getGRN_PSucExchange", 0);

			// graph total number of agents who donate during the year
			createTotalSequenceFor(GRN_pro_popGraph, "Donating",
					getAgentList(), "getProDonater", 0);



			// * Create the graph widget to display amount of maize requested
			// + exchanged in BRN
			GRN_pro_exchangeGraph = new OpenSequenceGraph(
					"Protein Exchanged Through GRN", this);
			GRN_pro_exchangeGraph.setAxisTitles("Time", "Grams/Year");

			// graph amount of maize requested by agents
			createTotalSequenceFor(GRN_pro_exchangeGraph, "g Requested",
					getAgentList(), "getGRN_PRequest", 0);

			// graph amount of maize exchanged by agents
			createTotalSequenceFor(GRN_pro_exchangeGraph, "g Received",
					getAgentList(), "getGRN_PExchange", 0);

			// graph amount of protein exchanged to agents who despite
			// exchange do not overcome protein deficit
			// this is a MaxSequence because GRN_proteinWasted accumulates g
			// protein exchanged, where these do not
			// overcome deficits, across agents within a year
			createTotalSequenceFor(GRN_pro_exchangeGraph, "g Wasted",
					getAgentList(), "getGRN_PWasted", 0);

			// graph amount of protein donated by agents
			createTotalSequenceFor(GRN_pro_exchangeGraph, "g Donated",
					getAgentList(), "getPGiven", 0);



			// * Create the graph widget to display agents involved in
			// exchange
			BRN_pro_popGraph = new OpenSequenceGraph(
					"Agents Requesting Protein Through BRN", this);
			BRN_pro_popGraph.setAxisTitles("Time", "Number of Agents");

			// graph total number of agents requesting protein
			createTotalSequenceFor(BRN_pro_popGraph, "Requesting",
					getAgentList(), "getBRN_PNumExchange", 0);

			// graph total number of agents who request and survive the year
			createTotalSequenceFor(BRN_pro_popGraph, "Receiving",
					getAgentList(), "getBRN_PSucExchange", 0);

			// graph total number of paybacks to agents
			createTotalSequenceFor(BRN_pro_popGraph, "Paybacks",
					getAgentList(), "getBRN_PPayback", 0);



			// * Create the graph widget to display amount of maize requested
			// + exchanged in BRN
			BRN_pro_exchangeGraph = new OpenSequenceGraph(
					"Protein Exchanged Through BRN", this);
			BRN_pro_exchangeGraph.setAxisTitles("Time", "Grams/Year");

			// graph amount of maize requested by agents
			createTotalSequenceFor(BRN_pro_exchangeGraph, "g Requested",
					getAgentList(), "getBRN_PRequest", 0);

			// graph amount of maize exchanged by agents
			createTotalSequenceFor(BRN_pro_exchangeGraph, "g Received",
					getAgentList(), "getBRN_PExchange", 0);

			// graph amount of maize exchanged to agents that die anyway
			createTotalSequenceFor(BRN_pro_exchangeGraph, "g Wasted",
					getAgentList(), "getBRN_PWasted", 0);

			// graph amount of protein paid back to agents
			createTotalSequenceFor(BRN_pro_exchangeGraph, "g Paid Back",
					getAgentList(), "getBRN_PPaidBack", 0);
		}		
	}

	public void buildDisplay() {
		if (Landscape) {
			DisplayConstants.CELL_HEIGHT = zoomFactor;
			DisplayConstants.CELL_WIDTH = zoomFactor;

			Controller.UPDATE_PROBES = true;

			cellDisplay = new Object2DDisplay(getWorld());
			cellDisplay.setObjectList(cellList);

			agentDisplay = new Object2DDisplay(getWorld());
			agentDisplay.setObjectList(agentList);

			year = new TextDisplay(1300, 20, Color.white);
			year.setBoxVisible(false);
			year.setFontSize(20);

			run = new TextDisplay(4, 20, Color.white);
			run.setBoxVisible(false);
			run.setFontSize(20);

			displaySurface = new DisplaySurface(cellDisplay.getSize(), this,
					"Ancestral Puebloan World");
			super.registerDisplaySurface("Ancestral Puebloan World",
					displaySurface);
			displaySurface.addDisplayableProbeable(cellDisplay, "Village Space");
			displaySurface.addDisplayableProbeable(year, "text");
			displaySurface.addDisplayableProbeable(run, "text2");
			if(Domestication){
				displaySurface.createLegend("Agent and Their Protein Resources");
				displaySurface.addLegendLabel("All Hunting", 1,new Color(0,8,255),false);
				displaySurface.addLegendLabel("All Domestication", 1,new Color(255,8,0),false);
				displaySurface.addLegendLabel("Observed Site Locations", 1,new Color(255,255,255),false);
			}

			displaySurface.updateUI();

			if (Groups) {
				groupSurface = new DisplaySurface(agentDisplay.getSize(), this,
						"Agent groups");
				super.registerDisplaySurface("Agent groups",groupSurface);
				groupSurface.setBackground(Color.white);
				groupSurface.addDisplayable(agentDisplay, "Group Display");
				groupSurface.updateUI();
			}
		}
	}

	public void displayGraphs() {
		if(Domestication){
			proteinStrategiesGraph.display();
			proteinCostsGraph.display();
		}

		//		if (Groups) {
		//			groupGraph.display();
		//			groupTypeGraph.display();
		//			agentTypesCountGraph.display();
		//		}
		if(Constants.MERGE_AND_FIGHT){
			warDeadGraph.display();
			wdGraph.display();
		}

		if(Agents){
			peopleGraph.display();
			bdGraph.display();
			famGraph.display();
			stateGraph.display();
			popGraph.display();
			state_through_timeGraph.display();
			fam_wrkrsGraph.display();
			eligibleGraph.display();
		}

		if(Resources){
			firewoodGraph.display();
			HourGraph.display();
			CalGraph.display();
			totCalGraph.display();
			fwhappyGraph.display();
		}

		if(Protein){
			lagoGraph.display();
			deerGraph.display();
			rrateGraph.display();
			protein_storageGraph.display();
			HuntingGraph.display();
		}

		if(Agriculture){
			yieldGraph.display();
			maize_storageGraph.display();
			avgGraph.display();
		}

		if(Residential){
			popPercentGraph.display();
			CommunGraph.display();
		}

		if (Exchange && this.economy != 0) {
			ex_popGraph.display();
			maize_exchangeGraph.display();
			BRN_ex_popGraph.display();
			BRN_maize_exchangeGraph.display();
			GRN_pro_popGraph.display();
			GRN_pro_exchangeGraph.display();
			BRN_pro_popGraph.display();
			BRN_pro_exchangeGraph.display();
		}

		if(Landscape){
			displaySurface.display();

			if (Groups) {
				groupSurface.display();
			}
		}
	}

	public void step() {
		super.step();		

		try {
			updateGraphs();
			if(Landscape){
				displaySurface.updateDisplay();
				year.clearLines();
				year.addLine("" + this.getWorldTime());
				run.clearLines();
				run.addLine("Run: " + this.getFile_ID());

				if (groupSurface != null)
					groupSurface.updateDisplay();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void updateGraphs() {
		if(Domestication){
			proteinStrategiesGraph.step();
			proteinCostsGraph.step();
		}


		//        if (Groups) {                
		//            groupGraph.step();
		//            groupTypeGraph.step();
		//            agentTypesCountGraph.step();
		//        }
		if(Constants.MERGE_AND_FIGHT){
			warDeadGraph.step();
			wdGraph.step();
		}

		if(Agents){
			popGraph.step();
			peopleGraph.step();
			bdGraph.step();
			famGraph.getHistogram().reset();
			famGraph.record();
			famGraph.updateGraph();
			stateGraph.getHistogram().reset();
			stateGraph.record();
			stateGraph.updateGraph();
			state_through_timeGraph.step();
			fam_wrkrsGraph.step();
			eligibleGraph.step();

		}

		if(Resources){
			firewoodGraph.step();
			HourGraph.step();
			CalGraph.step();
			totCalGraph.step();
			fwhappyGraph.step();
		}

		if(Protein){
			lagoGraph.step();
			deerGraph.step();
			rrateGraph.step();
			protein_storageGraph.step();
			HuntingGraph.step();
		}

		if(Agriculture){
			avgGraph.step();
			maize_storageGraph.step();
			yieldGraph.step();
		}

		if(Residential){
			popPercentGraph.step();
			CommunGraph.step();
		}

		if (Exchange && this.economy != 0) {
			ex_popGraph.step();
			maize_exchangeGraph.step();
			BRN_ex_popGraph.step();
			BRN_maize_exchangeGraph.step();
			GRN_pro_popGraph.step();
			GRN_pro_exchangeGraph.step();
			BRN_pro_popGraph.step();
			BRN_pro_exchangeGraph.step();
		}
	}

	protected void createAverageSequenceFor(OpenSequenceGraph graph,
			String title, final ArrayList<?> list, final String method, int markStyle) {
		graph.addSequence(title, new Sequence() {
			public double getSValue() {
				// return 0 if our list is empty or non-existent
				if (list == null || list.isEmpty()) return 0;

				double avg = 0;
				Class<?> execClass = list.get(0).getClass();

				try {
					Method m = execClass.getMethod(method);

					for (Object a : list) {
						avg = avg + Double.valueOf(String.valueOf(m.invoke(a)));
					}
					avg /= list.size();
				} catch (Exception e) {
					System.err.println("Can't invoke method: " + method
							+ " for average sequence");
					System.exit(0);
				}
				return avg;
			}
		}, markStyle);
	}

	/* DC: Creating a version without markstyle as it isn't used in my graphs */
	protected void createAverageSequenceFor(OpenSequenceGraph graph,
			String title, final ArrayList<?> list, final String method) {
		graph.addSequence(title, new Sequence() {
			public double getSValue() {
				// return 0 if our list is empty or non-existent
				if (list == null || list.isEmpty()) return 0;

				double avg = 0;
				Class<?> execClass = list.get(0).getClass();

				try {
					Method m = execClass.getMethod(method);

					for (Object a : list) {
						avg = avg + Double.valueOf(String.valueOf(m.invoke(a)));
					}
					avg /= list.size();
				} catch (Exception e) {
					System.err.println("Can't invoke method: " + method
							+ " for average sequence");
					System.exit(0);
				}
				return avg;
			}
		});
	}

	/* DC: Creating a version without markstyle as it isn't used in my graphs */
	protected void createNonNullAverageSequenceFor(OpenSequenceGraph graph,
			String title, final ArrayList<?> list, final String method, int markStyle) {
		graph.addSequence(title, new Sequence() {
			public double getSValue() {
				// return 0 if our list is empty or non-existent
				if (list == null || list.isEmpty()) return 0;

				double avg = 0;
				Class<?> execClass = list.get(0).getClass();

				try {
					Method m = execClass.getMethod(method);
					int len = 0;
					for (Object a : list) {
						double val = Double.valueOf(String.valueOf(m.invoke(a)));
						if(val != -999) {
							len+=1;
							avg = avg + val;
						}
					}
					avg /= len;
				} catch (Exception e) {
					System.err.println("Can't invoke method: " + method
							+ " for average sequence");
					System.exit(0);
				}
				return avg;
			}
		}, markStyle);
	}

	/**
	 * DC: adds a button to the ModelManipulator instance. When the button is
	 * pressed, calls method.
	 */
	private JButton createButton(String label, final String method) {
		final BeyondHooperObserverAgentModel me = this;
		JButton button = new JButton(label);
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ae) {
				try {
					Method m = BeyondHooperObserverAgentModel.class.getMethod(method);
					m.invoke(me);
					// DC: Have this update on the next step -
					// displaySurface.updateDisplay();

				} catch (Exception e) {
					e.printStackTrace();
					System.err
					.println("Can't add button to Manipulator for method: "
							+ method);
					System.exit(0);
				}
			}
		});

		return button;
	}

	private void createMaxSequenceFor(OpenSequenceGraph graph, String title,
			final ArrayList<?> list, final String method, int markStyle) {
		graph.addSequence(title, new Sequence() {

			public double getSValue() {
				if (list.size() == 0)
					return 0;

				double max = Double.NEGATIVE_INFINITY;
				Class<?> execClass = list.get(0).getClass();

				try {
					Method m = execClass.getMethod(method);

					for (Object a : list) {
						max = Math.max(max, Double.valueOf(String.valueOf(m
								.invoke(a))));
					}
				} catch (Exception e) {
					System.err.println("Can't invoke method: " + method
							+ " for max sequence");
					System.exit(0);
				}
				return max;
			}
		}, markStyle);
	}

	private void createTotalSequenceFor(OpenSequenceGraph graph, String title,
			final ArrayList<?> list, final String method, int markStyle) {
		graph.addSequence(title, new Sequence() {

			public double getSValue() {
				if (list.size() == 0)
					return 0;

				double sum = 0;
				Class<?> execClass = list.get(0).getClass();

				try {
					Method m = execClass.getMethod(method);

					for (Object a : list) {
						sum = sum + Double.valueOf(String.valueOf(m.invoke(a)));
					}
				} catch (Exception e) {
					System.err.println("Can't invoke method: " + method
							+ " for Total sequence");
					System.exit(0);
				}
				return sum;
			}
		}, markStyle);
	}

	public int getDisplayFrequency() {
		return displayFrequency;
	}

	public boolean isLandscape() {
		return Landscape;
	}

	public void setLandscape(boolean landscape) {
		Landscape = landscape;
	}

	public boolean isGroups() {
		return Groups;
	}

	public void setGroups(boolean groups) {
		Groups = groups;
	}

	public boolean isProtein() {
		return Protein;
	}

	public void setProtein(boolean hunting) {
		Protein = hunting;
	}

	public boolean isExchange() {
		return Exchange;
	}

	public void setExchange(boolean exchange) {
		Exchange = exchange;
	}

	public boolean isAgriculture() {
		return Agriculture;
	}

	public void setAgriculture(boolean agriculture) {
		Agriculture = agriculture;
	}

	public boolean isAgents() {
		return Agents;
	}

	public void setAgents(boolean agents) {
		Agents = agents;
	}

	public boolean isWarDead(){
		return WarDead;
	}

	public void setWarDead(boolean warDead) {
		WarDead = warDead;
	}

	public boolean isResources() {
		return Resources;
	}

	public void setResources(boolean resources) {
		Resources = resources;
	}

	public boolean isResidential() {
		return Residential;
	}

	public void setResidential(boolean residential) {
		Residential = residential;
	}

	// one parameter: update freq
	public int getZoomFactor() {
		return zoomFactor;
	}

	public int hideAgents() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setADisplay(0);
			}
		}
		return 32;
	}

	public int seeAgents() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setADisplay(1);
			}
		}
		return 33;
	}

	public void seeAgentsDomestication() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setADisplay(4);
			}
		}
	}

	public int seeCommunities() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setADisplay(2);
			}
		}
		return 34;
	}

	public int seeDeadAgents() {

		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setADisplay(3);
			}
		}
		return 35;
	}

	public int seeDeer() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setDisplay(23);
			}
		}
		return 23;
	}

	public int seeElevation() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setDisplay(1);
			}
		}
		return 1;
	}

	public int seeElevationPI() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setDisplay(13);
			}
		}
		return 13;
	}

	public int seeElevationPII() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setDisplay(14);
			}
		}
		return 14;
	}

	public int seeElevationPIII() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setDisplay(15);
			}
		}
		return 15;
	}

	public int seeElevationWater() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setDisplay(5);
			}
		}
		return 5;
	}

	public int seeFirewood() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setDisplay(28);
			}
		}
		return 28;
	}

	public int seeHares() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setDisplay(25);
			}
		}
		return 25;
	}

	public int seeMaizePI() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setDisplay(7);
			}
		}
		return 7;
	}

	public int seeMaizePII() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setDisplay(8);
			}
		}
		return 8;
	}

	public int seeMaizePIII() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setDisplay(9);
			}
		}
		return 9;
	}

	public int seeMaizePotential() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setDisplay(0);
			}
		}
		return 0;
	}

	public int seeMaizeWater() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setDisplay(4);
			}
		}
		return 4;
	}

	public int seeMaxWater() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setDisplay(31);
			}
		}
		return 31;
	}

	public int seeOccuProx() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setDisplay(21);
			}
		}
		return 21;
	}

	public int seeOccupyHH() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setDisplay(16);
			}
		}
		return 16;
	}

	public int seeOccupyYears() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setDisplay(17);
			}
		}
		return 17;
	}

	public int seeProximityPI() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setDisplay(18);
			}
		}
		return 18;
	}

	public int seeProximityPII() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setDisplay(19);
			}
		}
		return 19;
	}

	public int seeProximityPIII() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setDisplay(20);
			}
		}
		return 20;
	}

	public int seeRabbits() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setDisplay(24);
			}
		}
		return 24;
	}

	public int seeShrubs() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setDisplay(26);
			}
		}
		return 26;
	}

	public int seeSoil() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setDisplay(2);
			}
		}
		return 2;
	}

	public int seeSoilDegrade() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setDisplay(22);
			}
		}
		return 22;
	}

	public int seeSoilWater() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setDisplay(6);
			}
		}
		return 6;
	}

	public int seeTrees() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setDisplay(27);
			}
		}
		return 27;
	}

	public int seeWater() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setDisplay(3);
			}
		}
		return 3;
	}

	public int seeWaterPI() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setDisplay(10);
			}
		}
		return 10;
	}

	public int seeWaterPII() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setDisplay(11);
			}
		}
		return 11;
	}

	public int seeWaterPIII() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setDisplay(12);
			}
		}
		return 12;
	}

	public int seeweatherStation() {
		if (Landscape) {
			for (Cell c : getCellList()) {
				c.setDisplay(30);
			}
		}
		return 30;
	}

	public void setDisplayFrequency(int displayFrequency) {
		this.displayFrequency = displayFrequency;
	}



	public void setZoomFactor(int zoomFactor) {
		this.zoomFactor = zoomFactor;

		// DC: 5/30 - zoomFactor is the equivalent of changing the size per cell
		DisplayConstants.CELL_WIDTH = zoomFactor;
		DisplayConstants.CELL_HEIGHT = zoomFactor;
	}

	public static void main(String[] args) {
		final SimInit init = new SimInit();
		final BeyondHooperObserverAgentModel agentModelSwarm = new BeyondHooperObserverAgentModel();
		init.loadModel(agentModelSwarm, null, false);
	}

	//   private void buildGroupTypeGraph() {
	//        groupTypeGraph = new OpenSequenceGraph("Hooper Agent Types (Leaders include Potential)", this);
	//        groupTypeGraph.setAxisTitles("Time", "Number");
	//        groupTypeGraph.addSequence("H.ALLC.T.L", new Sequence() {
	//            @Override
	//            public double getSValue() {
	//                double res = 0;
	//
	//                if (getGroupStats() != null)
	//                    res = getGroupStats().numH_ALLC_T_L;
	//
	//                return res;
	//            }
	//        });
	//
	//        groupTypeGraph.addSequence("H.ALLC.T.UL", new Sequence() {
	//            @Override
	//            public double getSValue() {
	//                double res = 0;
	//
	//                if (getGroupStats() != null)
	//                    res = getGroupStats().numH_ALLC_T_UL;
	//
	//                return res;
	//            }
	//        });
	//
	//        groupTypeGraph.addSequence("H.ALLC.RT.L", new Sequence() {
	//            @Override
	//            public double getSValue() {
	//                double res = 0;
	//
	//                if (getGroupStats() != null)
	//                    res = getGroupStats().numH_ALLC_RT_L;
	//
	//                return res;
	//            }
	//        });
	//
	//        groupTypeGraph.addSequence("H.ALLC.RT.UL", new Sequence() {
	//            @Override
	//            public double getSValue() {
	//                double res = 0;
	//
	//                if (getGroupStats() != null)
	//                    res = getGroupStats().numH_ALLC_RT_UL;
	//
	//                return res;
	//            }
	//        });
	//
	//        groupTypeGraph.addSequence("H.RC.T.L", new Sequence() {
	//            @Override
	//            public double getSValue() {
	//                double res = 0;
	//
	//                if (getGroupStats() != null)
	//                    res = getGroupStats().numH_RC_T_L;
	//
	//                return res;
	//            }
	//        });
	//
	//        groupTypeGraph.addSequence("H.RC.T.UL", new Sequence() {
	//            @Override
	//            public double getSValue() {
	//                double res = 0;
	//
	//                if (getGroupStats() != null)
	//                    res = getGroupStats().numH_RC_T_UL;
	//
	//                return res;
	//            }
	//        });
	//
	//        groupTypeGraph.addSequence("H.RC.RT.L", new Sequence() {
	//            @Override
	//            public double getSValue() {
	//                double res = 0;
	//
	//                if (getGroupStats() != null)
	//                    res = getGroupStats().numH_RC_RT_L;
	//
	//                return res;
	//            }
	//        });
	//
	//        groupTypeGraph.addSequence("H.RC.RT.UL", new Sequence() {
	//            @Override
	//            public double getSValue() {
	//                double res = 0;
	//
	//                if (getGroupStats() != null)
	//                    res = getGroupStats().numH_RC_RT_UL;
	//
	//                return res;
	//            }
	//        });
	//
	//        groupTypeGraph.addSequence("NH.ALLC", new Sequence() {
	//            @Override
	//            public double getSValue() {
	//                double res = 0;
	//
	//                if (getGroupStats() != null)
	//                    res = getGroupStats().numNH_ALLC;
	//
	//                return res;
	//            }
	//        });
	//
	//        groupTypeGraph.addSequence("NH.MM", new Sequence() {
	//            @Override
	//            public double getSValue() {
	//                double res = 0;
	//
	//                if (getGroupStats() != null)
	//                    res = getGroupStats().numNH_MM;
	//
	//                return res;
	//            }
	//        });
	//
	//        groupTypeGraph.addSequence("NH.RC", new Sequence() {
	//            @Override
	//            public double getSValue() {
	//                double res = 0;
	//
	//                if (getGroupStats() != null)
	//                    res = getGroupStats().numNH_RC;
	//
	//                return res;
	//            }
	//        });
	//    }

	//    /**
	//     * @return the groupStats
	//     */
	//    public HooperGame.GroupStats getGroupStats() {
	//        return hooper2.getGroupStats();
	//    }

	public static ColorMap buildColorMap() {
		int i = 0;



		ColorMap cMap = new ColorMap();

		//**********************************************************************
		// *
		// in Village.WATER data, 0 indicates Village.NODATA or no known water
		// source.
		// 1 indicates Intermittent Stream from USGS 7.5' Quads.
		// 2 indicates Perennial Stream from USGS 7.5' Quads.
		// 3 indicates Rivers (i.e. The Dolores River) from USGS 7.5' Quads, and
		// Springs from USGS 7.5' Quads and Colo. Div. of Water filings.
		// Allocate 1 color for NULL display: 0
		cMap.mapColor(0, Color.BLACK);

		// Allocate 3 shades for Village.WATER availability: 1-3
		for (i = 1; i < 4; i++) {
			cMap.mapColor(i, 0.2, 0.2, (double) (i * i / 9.0));
		}
		// Allocate 50 shades for Village.MAIZE fertility: 4-53
		for (i = 0; i < 50; i++) {
			cMap.mapColor(4 + i, 0.1, (double) (i + 10.0) / 60.0, 0.2);
		}
		// Allocate 50 shades for SOIL types: 54-103
		cMap.mapColor(54, Color.BLACK);
		for (i = 1; i < 50; i++) {
			cMap.mapColor(54 + i, (double) (i + 10.0) / 60.0,
					(double) (i + 5.0) / 60.0, 0.0);
		}

		// Allocate 50 shades for Village.ELEVATION levels: 104-153
		for (i = 0; i < 50; i++) {
			double grayVal = (double) (i + 10.0) / 60.0;
			cMap.mapColor(104 + i, grayVal, grayVal, grayVal);
		}

		// colors for the agents
		cMap.mapColor(154, Color.RED);
		cMap.mapColor(155, new Color(178, 34, 34)); // firebrick
		cMap.mapColor(156, Color.white);
		cMap.mapColor(157, Color.yellow);

		//cMap.mapColor(155, con.getColorFor("firebrick"));
		//cMap.mapColor(156, con.getColorFor("white"));
		//cMap.mapColor(157, con.getColorFor("yellow"));

		// colors for known habitation sites
		cMap.mapColor(158, new Color(104, 34, 139)); // darkorchid4 - PI

		cMap.mapColor(159, new Color(178, 58, 238)); // darkorchid2 - PII

		cMap.mapColor(160, new Color(221, 160, 221)); // plum - PIII

		// Allocate 25 shades for modelling period habitation site sizes based
		// on number of households 161-185

		for (i = 1; i < 26; i++) {
			cMap.mapColor(160 + i, (double) (.58 + .016 * i), .2 + .02 * i,
					(double) .78 + .008 * i);

			// Allocate 25 shades for modelling period habitation site sizes
			// based on number of households 186-210
		}
		for (i = 1; i < 26; i++) {
			cMap.mapColor(185 + i, (double) (.285 + .02 * i), .41 + .015 * i,
					(double) .285 + .02 * i);

			// now set the agents color to red for easy viewing
		}

		return cMap;
	}

	/**
	 * @return the colorMap
	 */
	public static ColorMap getColorMap() {
		if (colorMap == null)
			colorMap = buildColorMap();
		return colorMap;
	}
}
