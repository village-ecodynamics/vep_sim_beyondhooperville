package com.mesaverde.specialization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;

import com.mesaverde.specialization.allocation.AllocationStrategy;
import com.mesaverde.specialization.resources.*;
import com.mesaverde.specialization.tasks.*;
import com.mesaverde.specialization.threads.*;
import com.mesaverde.village.Agent;
import com.mesaverde.village.Cell;
import com.mesaverde.village.Individual;
import com.mesaverde.village.Village;

public class SpecializedAgentModelSwarm extends com.mesaverde.model.ObserverAgentModel {
    // the weight level at which an agent is considered to be specializing in a task

    private static final double SPECIALIZATION_THRESHOLD = 0.90d;
    public static boolean SHOW_PARENT_GRAPHS = false;
    public static boolean SHOW_GRAPH;
    public static int BATCH;

    // variables that determine what we do with specialization
    // you need to ensure that these do not conflict
    private boolean enableSpecialization = true;
    
    // choose only one
    private boolean useEcon = false;
    private boolean useEconAndSocial = false;
    private boolean useAllSocialNoEcon = false;

    private double maxYearsStorageSpec = 10;

    // we need some additional graphs
    private OpenSequenceGraph timeAllocationGraph;
    private OpenSequenceGraph storedProductivityGraph;
    private OpenSequenceGraph resourceStorageGraph;
    private OpenSequenceGraph maizeTradeGraph;
    private OpenSequenceGraph meatTradeGraph;
    private OpenSequenceGraph woodTradeGraph;
    private OpenSequenceGraph waterTradeGraph;
    private OpenSequenceGraph specializedGraph;  // The % of agents spending at least 95% on one task
    private OpenSequenceGraph agentSpecializationsGraph; // The tasks that have agents spending at least 95% of their time
    private OpenSequenceGraph divisionOfLabourGraph;
    private HashMap<Class<? extends Task>, Integer> specializedTasks = new HashMap<Class<? extends Task>, Integer>();
    private ArrayList<SpecializedAgent> allocatingAgents = new ArrayList<SpecializedAgent>();  // agents using weights to allocate
    private ExecutorService execService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    private CountDownLatch startSignal, doneSignal;
    // for calculating the amount of specialization
    // also hardcoding the number of tasks for this
    private Class[] taskTypes = new Class[]{
        Hunter.class, Farmer.class, WaterCarrier.class, Woodsman.class
    };
    @SuppressWarnings("unchecked")
    private DOLCalculator dolCalculator;

    /* We always want this branch to be multithreaded, as it's written for it */
    public SpecializedAgentModelSwarm() {
        Village.ENABLE_MULTITHREADING = true;      
    }

    /** Additional model building items */
    @Override
    public void buildModel() {    	 
        super.buildModel();
        dolCalculator = new DOLCalculator(allocatingAgents, taskTypes);           
    }

    public boolean getEnableSpecialization() {
        return enableSpecialization;
    }

    public void setEnableSpecialization(boolean val) {
        enableSpecialization = val;

        if (!enableSpecialization) {
            setUseEcon(false);
            setUseEconAndSocial(false);
            setUseAllSocialNoEcon(false);
        } 

        SpecializedAgent.ENABLE_SPECIALIZATION = val;
    }

    /** Build our additional graphs */
    @Override
    protected void buildGraphs() {
        super.buildGraphs();

        if (SHOW_GRAPH) {
            ArrayList<Agent> agents = getAgentList();            
            
            if (Agents) {
                 storedProductivityGraph = new OpenSequenceGraph("Average Amount of Stored Productivity", this);
                 storedProductivityGraph.setAxisTitles("Time", "Avg. Productivity stored");
                 createAverageSequenceFor(storedProductivityGraph, "Cals", agents, "getStoredProductivity");

                if (SpecializedAgent.ENABLE_SPECIALIZATION) {
                    // time allocation based on tasks over time
                    timeAllocationGraph = new OpenSequenceGraph("Time Allocation Among Tasks", this);
                    timeAllocationGraph.setAxisTitles("Time", "% Spent");
                    createAverageSequenceFor(timeAllocationGraph, "Farming", allocatingAgents, "getFarmWeight");
                    createAverageSequenceFor(timeAllocationGraph, "Hunting", allocatingAgents, "getHuntingWeight");
                    createAverageSequenceFor(timeAllocationGraph, "Water", allocatingAgents, "getWaterCollectingWeight");
                    createAverageSequenceFor(timeAllocationGraph, "Wood", allocatingAgents, "getWoodGatheringWeight");                   
                    
                    divisionOfLabourGraph = new OpenSequenceGraph("Level Of Task Specialization", this);
                    divisionOfLabourGraph.setAxisTitles("Time", "Level");
                    divisionOfLabourGraph.addSequence("Specialization Level", new Sequence() {
                        @Override
                        public double getSValue() {
                            return dolCalculator.calcDivisionOfLabour();
                        }
                    });
                    divisionOfLabourGraph.setYRange(0.0d, 1.0d);
                    divisionOfLabourGraph.setYAutoExpand(false); // never goes outside this range

                    specializedGraph = new OpenSequenceGraph("Specialized Agents", this);
                    specializedGraph.setAxisTitles("Time", "%");

                    // those performing more than 95%
                    specializedGraph.addSequence("Specialized %", new Sequence() {

                        @Override
                        public double getSValue() {
                            specializedTasks.clear(); // we also record the specialized tasks, so clear them now
                            double total = 0.0;
                            int numAgents = getAgentList().size();

                            for (SpecializedAgent sa : allocatingAgents) {
                                AllocationStrategy aStrategy = sa.getAllocationStrategy();
                                HashMap<Task, Double> weights = aStrategy.getWeights();

                                // now find those spending more than SPECIALIZATION_THRESHOLD of their time on one task
                                for (Task task : weights.keySet()) {
                                    Double d = weights.get(task);

                                    if (d != null && !d.isNaN() && d >= SPECIALIZATION_THRESHOLD) {
                                        recordSpecializedAgent(task);
                                        total++;
                                        break;
                                    }
                                }
                            }

                            return total * 100 / numAgents;
                        }
                    });

                    // those performing less than 1%
                    specializedGraph.addSequence("Abandoned %", new Sequence() {

                        @Override
                        public double getSValue() {
                            double total = 0.0;

                            for (SpecializedAgent sa : allocatingAgents) {
                                AllocationStrategy aStrategy = sa.getAllocationStrategy();
                                HashMap<Task, Double> weights = aStrategy.getWeights();

                                // now find those spending at most 1% of their time on any one task
                                for (Double d : weights.values()) {
                                    if (d != null && !d.isNaN() && d <= 0.02) {
                                        total++;
                                        break;
                                    }
                                }
                            }

                            return total * 100 / getAgentList().size();
                        }
                    });
                    specializedGraph.setYRange(0, 100);

                    agentSpecializationsGraph = new OpenSequenceGraph("Specialized Tasks", this);
                    agentSpecializationsGraph.setAxisTitles("Time", "Amount");
                    agentSpecializationsGraph.addSequence("Farmer", new Sequence() {

                        @Override
                        public double getSValue() {
                            Integer res = specializedTasks.get(Farmer.class);
                            if (res == null) {
                                res = 0;
                            }

                            return (double) res;
                        }
                    });
                    agentSpecializationsGraph.addSequence("Hunter", new Sequence() {

                        @Override
                        public double getSValue() {
                            Integer res = specializedTasks.get(Hunter.class);
                            if (res == null) {
                                res = 0;
                            }

                            return (double) res;
                        }
                    });
                    agentSpecializationsGraph.addSequence("Woodsman", new Sequence() {

                        @Override
                        public double getSValue() {
                            Integer res = specializedTasks.get(Woodsman.class);
                            if (res == null) {
                                res = 0;
                            }

                            return (double) res;
                        }
                    });
                    agentSpecializationsGraph.addSequence("WaterCarrier", new Sequence() {

                        @Override
                        public double getSValue() {
                            Integer res = specializedTasks.get(WaterCarrier.class);
                            if (res == null) {
                                res = 0;
                            }

                            return (double) res;
                        }
                    });

                }
            }

            if (Resources) {
                // resource storage levels
                resourceStorageGraph = new OpenSequenceGraph("Average # Years Resource Storage", this);
                resourceStorageGraph.setAxisTitles("Time", "Storage");
                createAverageSequenceFor(resourceStorageGraph, "Maize", agents, "getNumYearsMaizeStorage");
                createAverageSequenceFor(resourceStorageGraph, "Meat", agents, "getNumYearsMeatStorage");
                createAverageSequenceFor(resourceStorageGraph, "Water", agents, "getNumYearsWaterStorage");
                createAverageSequenceFor(resourceStorageGraph, "Wood", agents, "getNumYearsWoodStorage");

                // resource requests/trades
                maizeTradeGraph = new OpenSequenceGraph("Maize Trading via Barter Exchange", this);
                maizeTradeGraph.setAxisTitles("Time", "Amount");
                createTradeManagerRequestSequenceFor(maizeTradeGraph, "Maize Requested", Maize.class);
                createTradeManagerExchangeSequenceFor(maizeTradeGraph, "Maize Exchanged", Maize.class);

                meatTradeGraph = new OpenSequenceGraph("Meat Trading via Barter Exchange", this);
                meatTradeGraph.setAxisTitles("Time", "Amount");
                createTradeManagerRequestSequenceFor(meatTradeGraph, "Meat Requested", Meat.class);
                createTradeManagerExchangeSequenceFor(meatTradeGraph, "Meat Exchanged", Meat.class);

                woodTradeGraph = new OpenSequenceGraph("Wood Trading via Barter Exchange", this);
                woodTradeGraph.setAxisTitles("Time", "Amount");
                createTradeManagerRequestSequenceFor(woodTradeGraph, "Wood Requested", Wood.class);
                createTradeManagerExchangeSequenceFor(woodTradeGraph, "Wood Exchanged", Wood.class);

                waterTradeGraph = new OpenSequenceGraph("Water Trading via Barter Exchange", this);
                waterTradeGraph.setAxisTitles("Time", "Amount");
                createTradeManagerRequestSequenceFor(waterTradeGraph, "Water Requested", Water.class);
                createTradeManagerExchangeSequenceFor(waterTradeGraph, "Water Exchanged", Water.class);
            }
        }
    }

    /** Finds which agents are allocating their time using weights */
    private ArrayList<SpecializedAgent> getAllocatingAgents(
            ArrayList<Agent> agents) {
        ArrayList<SpecializedAgent> res = new ArrayList<SpecializedAgent>();

        for (Agent a : agents) {
            SpecializedAgent sa = (SpecializedAgent) a;

            // if the agent's weights are already created, then they are using allocation
            if (sa.isAllocating()) {
                res.add(sa);
            }
        }

        return res;
    }

    // records that an agent is specialized on a specified task (>= 95% of time is considered specialized)
    private void recordSpecializedAgent(Task task) {
        Integer current = specializedTasks.get(task.getClass());

        if (current == null) {
            current = 0;
        }

        specializedTasks.put(task.getClass(), current + 1);
    }

    private void createTradeManagerRequestSequenceFor(
            OpenSequenceGraph graph, String title,
            final Class<? extends Resource> resourceType) {

        graph.addSequence(title, new Sequence() {

            @Override
            public double getSValue() {
                HashMap<Integer, HashMap<Class<? extends Resource>, Double>> record = TradeManager.getRequestRecord();
                HashMap<Class<? extends Resource>, Double> history = record.get(getWorldTime() - 1);

                if (history == null) {
                    return 0;
                }

                Double val = history.get(resourceType);
                if (val == null) {
                    val = 0.0;
                }

                return val;
            }
        });

    }

    private void createTradeManagerExchangeSequenceFor(
            OpenSequenceGraph graph, String title,
            final Class<? extends Resource> resourceType) {

        graph.addSequence(title, new Sequence() {

            @Override
            public double getSValue() {
                HashMap<Class<? extends Resource>, Double> history = TradeManager.getTradeRecord().get(getWorldTime() - 1);
                Double val = 0.0;

                if (history == null) {
                    return 0;
                }

                val = history.get(resourceType);

                if (val == null) {
                    val = 0.0;
                }

                return val;
            }
        });

    }

    @Override
    public void displayGraphs() {
        if (SHOW_PARENT_GRAPHS) {
            super.displayGraphs();
        }

        if (BATCH == 1) {
            displaySurface.hide();
        }

        if (SHOW_GRAPH) {
            if (Resources) {
                resourceStorageGraph.display();
                maizeTradeGraph.display();
                meatTradeGraph.display();
                woodTradeGraph.display();
                waterTradeGraph.display();
            }

            if (Agents) {
                storedProductivityGraph.display();
                
                if (SpecializedAgent.ENABLE_SPECIALIZATION) {
                    specializedGraph.display();
                    divisionOfLabourGraph.display();
                    agentSpecializationsGraph.display();
                    timeAllocationGraph.display();                    
                }
            }

            if (Protein) {
                deerGraph.display();
                lagoGraph.display();
            }
        }
    }

    @Override
    public void updateGraphs() {
        if (SHOW_PARENT_GRAPHS) {
            super.updateGraphs();
        }

        if (SHOW_GRAPH) {
            if (Agents) {
                storedProductivityGraph.step();
                
                if (SpecializedAgent.ENABLE_SPECIALIZATION) {
                    // update the time allocation graph, making sure to filter out non-allocating agents
                    allocatingAgents.clear();
                    allocatingAgents.addAll(getAllocatingAgents(getAgentList()));
                    timeAllocationGraph.step();
                    //popGraph.step();
                    //peopleGraph.step();
                    specializedGraph.step();
                    divisionOfLabourGraph.step();
                    agentSpecializationsGraph.step();                                          
                }
            }

            if (Resources) {
                resourceStorageGraph.step();
                maizeTradeGraph.step();
                meatTradeGraph.step();
                woodTradeGraph.step();
                waterTradeGraph.step();
            }

            if (Protein) {
                deerGraph.step();
                lagoGraph.step();
            }
        }
    }
    
    /**
     * After each season, we do trades
     */
    @Override
    @SuppressWarnings("unchecked")
    public void step() {
        try {
            if (Village.DEBUG) {
                System.out.printf("$$setDeathCounter\n");
            }

            for (Agent a : getAgentList()) {
                a.setDeathCounter();
            }

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

            database.updateDeerCells();
            if (Village.DEBUG) {
                System.out.printf("$$setFuelProduction\n");
            }

            for (Cell c : getCellList()) {
                c.setFuelProduction();
            }

            if (Village.DEBUG) {
                System.out.printf("$$step_procure\n");
            }
            
            TradeManager tradeManager = ResourceManager.getTradeManager();

            ArrayList<SpecializedAgent> cloneList = (ArrayList<SpecializedAgent>) getAgentList().clone();
            shuffleAgents(cloneList);

            startSignal = new CountDownLatch(1);
            doneSignal = new CountDownLatch(cloneList.size());
            for (final Agent a : cloneList) {
                execService.execute(new ProcureInitThread(a, startSignal, doneSignal));
            }
            startSignal.countDown();
            doneSignal.await();

            cloneList = (ArrayList<SpecializedAgent>) getAgentList().clone();
            shuffleAgents(cloneList);

            startSignal = new CountDownLatch(1);
            doneSignal = new CountDownLatch(cloneList.size());
            for (final Agent a : cloneList) {
                execService.execute(new SpringThread(a, startSignal, doneSignal));
            }
            startSignal.countDown();
            doneSignal.await();
            tradeManager.processTrades();


            cloneList = (ArrayList<SpecializedAgent>) getAgentList().clone();
            shuffleAgents(cloneList);
            for (Agent a : cloneList) {
                a.step_interact();
            }
            removeAgents();

            cloneList = (ArrayList<SpecializedAgent>) getAgentList().clone();
            shuffleAgents(cloneList);

            startSignal = new CountDownLatch(1);
            doneSignal = new CountDownLatch(cloneList.size());
            for (final Agent a : cloneList) {
                execService.execute(new SummerThread(a, startSignal, doneSignal));
            }
            startSignal.countDown();
            doneSignal.await();
            tradeManager.processTrades();

            cloneList = (ArrayList<SpecializedAgent>) getAgentList().clone();
            shuffleAgents(cloneList);
            for (Agent a : cloneList) {
                a.step_interact();
            }
            removeAgents();

            cloneList = (ArrayList<SpecializedAgent>) getAgentList().clone();
            shuffleAgents(cloneList);

            startSignal = new CountDownLatch(1);
            doneSignal = new CountDownLatch(cloneList.size());
            for (final Agent a : cloneList) {
                execService.execute(new SpecializedFallWorkThread(a, startSignal, doneSignal));
            }
            startSignal.countDown();
            doneSignal.await();
            tradeManager.processTrades();

            cloneList = (ArrayList<SpecializedAgent>) getAgentList().clone();
            shuffleAgents(cloneList);
            for (Agent a : cloneList) {
                a.step_interact();
            }
            removeAgents();

            cloneList = (ArrayList<SpecializedAgent>) getAgentList().clone();
            shuffleAgents(cloneList);

            startSignal = new CountDownLatch(1);
            doneSignal = new CountDownLatch(cloneList.size());
            for (final Agent a : cloneList) {
                execService.execute(new WinterThread(a, startSignal, doneSignal));
            }
            startSignal.countDown();
            doneSignal.await();

            tradeManager.processTrades();
            removeAgents();

            cloneList = (ArrayList<SpecializedAgent>) getAgentList().clone();
            shuffleAgents(cloneList);

            // this method is the slowest part of the simulation, so let's try to thread it
            // we group them all to make sure we wait for them
            startSignal = new CountDownLatch(1);
            doneSignal = new CountDownLatch(cloneList.size());
            for (final Agent a : cloneList) {
                execService.execute(new ConcludeThread(a, startSignal, doneSignal));
            }
            startSignal.countDown();


            if (Village.DEBUG) {
                System.out.printf("$$diffuseDeer\n");
            }
            System.out.flush();

            if (Village.DEBUG) {
                System.out.printf("$$removeAgents\n");
            }

            System.out.flush();
            //removeAgents();

            // make sure all agents have finished updating before continuing
            doneSignal.await();

            if (SpecializedAgent.USE_SOCIAL_INFLUENCE_FOR_SPECIALIZATION) {
                startSignal = new CountDownLatch(1);
                doneSignal = new CountDownLatch(cloneList.size());
                for (final Agent a : cloneList) {
                    execService.execute(new SocialPressureThread(a, startSignal, doneSignal));
                }
                startSignal.countDown();
                doneSignal.await();
            }
            database.diffuseDeer();
            removeAgents();

            // JAC 6/05 print statistics
            for (Agent a : getAgentList()) {
                a.printagentstats();
            }

            if (Village.DEBUG) {
                System.out.printf("$$updateYear\n");
            }

            System.out.flush();
            updateYear();

            if (Village.DEBUG) {
                System.out.printf("$$updateOccupied\n");
            }

            System.out.flush();
            for (Cell c : getCellList()) {
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

            // DC: update visual elements
            updateGraphs();

            if (SHOW_GRAPH) {
                if (displaySurface != null) {
                    displaySurface.updateDisplay();
                }

                if (groupSurface != null) {
                    groupSurface.updateDisplay();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    /** Just want it to print off the death statistics as well */
    public void updateYear() {
        SpecializedAgent.printDeathStats();
        //printGenderNumbers();
        
        if (getTickCount() == 700)
        	outputGraphs();
        
        super.updateYear();
    }

    private void outputGraphs() {
    	writeGraph(divisionOfLabourGraph);
    	writeGraph(popGraph);
    	writeGraph(popPercentGraph);
        writeGraph(woodTradeGraph);
        writeGraph(resourceStorageGraph);
        writeGraph(storedProductivityGraph);
	}

	private void writeGraph(OpenSequenceGraph graph) {
		if (graph != null)
			graph.writeToFile();
	}

	private void printGenderNumbers() {
        int totalMales = 0, totalFemales = 0;

        for (Agent a : getAgentList()) {
            for (Individual ind : a.getFamilyUnit().getAllIndividuals()) {
                if (ind.getGender() == Individual.MALE) {
                    totalMales++;
                } else {
                    totalFemales++;
                }
            }
        }

        System.out.println("There are " + totalMales + " males and " + totalFemales + " females.");
    }    
    
    @Override
    public String[] getInitParam()
    {
    	return new String[] { "allAgents", "File_ID",
		  "experimentDuration", "economy", "enableSpecialization", "useEcon",
          "useEconAndSocial", "maxYearsStorageSpec", "hunting_radius",
        "harvest_adjustment", "need_meat", "ad_plots", "state_good"};
    }

    /**
     * @return the maxYearsStorageSpec
     */
    public double getMaxYearsStorageSpec() {
        return maxYearsStorageSpec;
    }

    /**
     * @param maxYearsStorageSpec The number of years of resources agents will be
     * allowed to store
     *
     */
    public void setMaxYearsStorageSpec(double maxYearsStorageSpec) {
        this.maxYearsStorageSpec = maxYearsStorageSpec;
        SpecializedAgent.REAL_MAX_FACTOR = maxYearsStorageSpec / 2;
    }

    /**
     * @return the useEconAndSocial
     */
    public boolean getUseEconAndSocial() {
        return useEconAndSocial;
    }

    /**
     * @param useEconAndSocial the useEconAndSocial to set
     */
    public void setUseEconAndSocial(boolean useEconAndSocial) {
        this.useEconAndSocial = useEconAndSocial;

        if (useEconAndSocial) {
            setEnableSpecialization(true);
            setUseEcon(false);
            setUseAllSocialNoEcon(false);

            SpecializedAgent.ENABLE_SPECIALIZATION = true;
            SpecializedAgent.USE_SOCIAL_INFLUENCE_FOR_SPECIALIZATION = true;
            SpecializedAgent.APPLY_DEMAND_PRESSURE = false;
        } 
    }

    /**
     * @return the useAllSocialNoEcon
     */
    public boolean getUseAllSocialNoEcon() {
        return useAllSocialNoEcon;
    }

    /**
     * @param useAllSocialNoEcon the useAllSocialNoEcon to set
     */
    public void setUseAllSocialNoEcon(boolean useAllSocialNoEcon) {
        this.useAllSocialNoEcon = useAllSocialNoEcon;

        if (useAllSocialNoEcon) {
            setEnableSpecialization(true);
            setUseEcon(false);
            setUseEconAndSocial(false);

            SpecializedAgent.ENABLE_SPECIALIZATION = true;
            SpecializedAgent.USE_SOCIAL_INFLUENCE_FOR_SPECIALIZATION = true;
            SpecializedAgent.APPLY_DEMAND_PRESSURE = true;
        }
    }

    /**
     * @return the useEcon
     */
    public boolean getUseEcon() {
        return useEcon;
    }

    /**
     * @param useEcon the useEcon to set
     */
    public void setUseEcon(boolean useEcon) {
        this.useEcon = useEcon;

        if (useEcon) {
            setEnableSpecialization(true);
            setUseEconAndSocial(false);
            setUseAllSocialNoEcon(false);

            SpecializedAgent.ENABLE_SPECIALIZATION = true;
            SpecializedAgent.USE_SOCIAL_INFLUENCE_FOR_SPECIALIZATION = false;
            SpecializedAgent.APPLY_DEMAND_PRESSURE = false;
        } 
    }
}
